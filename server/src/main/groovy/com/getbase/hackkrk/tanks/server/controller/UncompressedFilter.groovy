package com.getbase.hackkrk.tanks.server.controller

import groovy.util.logging.Slf4j
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Slf4j
@Component
@ConfigurationProperties(prefix = "filter.uncompressed")
class UncompressedFilter implements Filter {

    boolean warn

    boolean fail

    @Override
    void init(FilterConfig filterConfig) throws ServletException {
        // intentionally left blank
    }

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            if (request.getHeader("Accept-Encoding") == null) {
                if (warn) {
                    log.warn "User {} from {} uses uncompressed transfer!", request.getHeader('Authorization'), request.remoteHost
                }

                if (fail) {
                    // FIXME no message
                    response.sendError(HttpStatus.BAD_REQUEST.value())
                    return
                }
            }
        }

        chain.doFilter(request, response)
    }

    @Override
    void destroy() {
        // intentionally left blank
    }

}

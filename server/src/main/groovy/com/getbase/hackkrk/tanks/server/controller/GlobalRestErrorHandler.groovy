package com.getbase.hackkrk.tanks.server.controller

import groovy.transform.Immutable
import org.springframework.boot.autoconfigure.web.ErrorController
import org.springframework.web.bind.annotation.*

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation

@RestController
@ControllerAdvice
class GlobalRestErrorHandler implements ErrorController {

    @Immutable
    static class Error {

        int status

        String message

    }

    @RequestMapping(value = '/error')
    @ExceptionHandler(Exception)
    Error error(HttpServletRequest request, HttpServletResponse response, Exception e) {
        def responseStatus = findAnnotation(e.class, ResponseStatus)
        if (responseStatus) {
            response.status = responseStatus.value().value()
        } else if (response.status == 200) {
            response.status = 500
        }

        [response.status, e.getMessage()]
    }

    @Override
    String getErrorPath() {
        '/error'
    }

}

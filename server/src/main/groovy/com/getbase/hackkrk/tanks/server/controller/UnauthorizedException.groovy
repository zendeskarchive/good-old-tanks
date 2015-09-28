package com.getbase.hackkrk.tanks.server.controller

import org.springframework.web.bind.annotation.ResponseStatus

import static org.springframework.http.HttpStatus.UNAUTHORIZED

@ResponseStatus(UNAUTHORIZED)
class UnauthorizedException extends RuntimeException {

    UnauthorizedException(String message) {
        super(message)
    }

}

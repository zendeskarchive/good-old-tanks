package com.getbase.hackkrk.tanks.server.controller

import org.springframework.web.bind.annotation.ResponseStatus

import static org.springframework.http.HttpStatus.NOT_FOUND

@ResponseStatus(NOT_FOUND)
class NotFoundException extends RuntimeException {

    NotFoundException(String message) {
        super(message)
    }

}

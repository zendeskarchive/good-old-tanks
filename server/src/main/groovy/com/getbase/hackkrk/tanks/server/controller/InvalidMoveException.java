package com.getbase.hackkrk.tanks.server.controller;

import com.getbase.hackkrk.tanks.server.model.game.Move;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ResponseStatus(BAD_REQUEST)
public class InvalidMoveException extends Exception {
    public InvalidMoveException(Move move) {
        super("Provide either shotPower or moveDistance, not both.");
    }
}

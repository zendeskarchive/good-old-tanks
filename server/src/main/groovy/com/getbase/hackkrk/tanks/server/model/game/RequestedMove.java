package com.getbase.hackkrk.tanks.server.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.getbase.hackkrk.tanks.server.model.tournament.Player;

import lombok.Data;

@Data
public class RequestedMove {
    @JsonUnwrapped
    private final Player player;
    @JsonUnwrapped
    private final Move move;
    
    @JsonCreator
    public RequestedMove(
            @JsonProperty("player") Player player, 
            @JsonProperty("move") Move move) {
        this.player = player;
        this.move = move;
    }
}

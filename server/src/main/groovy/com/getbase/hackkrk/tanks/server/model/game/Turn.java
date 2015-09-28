package com.getbase.hackkrk.tanks.server.model.game;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import com.getbase.hackkrk.tanks.server.model.scene.View;

import lombok.Data;

@JsonPropertyOrder({"number","requestedMoves","tanks","outcome","last"})
@Data
public class Turn {
    private final int number;
    
    @JsonView(View.Internal.class)
    private final List<RequestedMove> requestedMoves;
    private final List<Tank> tanks;
    private final List<MoveOutcome> outcome;
    private final boolean last;
    
    @JsonCreator
    protected Turn(
            @JsonProperty("number") int number,
            @JsonProperty("requestedMoves") List<RequestedMove> requestedMoves, 
            @JsonProperty("tanks") List<Tank> tanks, 
            @JsonProperty("moves") List<MoveOutcome> outcome,
            @JsonProperty("last") boolean last) {
        this.number = number;
        this.requestedMoves = requestedMoves;
        this.tanks = tanks;
        this.outcome = outcome;
        this.last = last;
    }
}

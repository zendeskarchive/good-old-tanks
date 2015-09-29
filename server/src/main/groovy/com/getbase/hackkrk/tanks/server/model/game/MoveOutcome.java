package com.getbase.hackkrk.tanks.server.model.game;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.getbase.hackkrk.tanks.server.model.tournament.Player;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MoveOutcome {
    public enum Type{
        miss,
        ground_hit,
        tank_hit
    }
    
    @JsonUnwrapped
    private final Player player;
    private final Type type;
    private final Point hitCoordinates;
    private final Tank target;
    private final boolean targetDestroyed;
    private final List<Point> bulletTrajectory;
    private final List<Point> tankMovement;
    
    @JsonCreator
    protected MoveOutcome(
            @JsonProperty("player") Player player, 
            @JsonProperty("type") Type type, 
            @JsonProperty("hitCoordinates") Point hitCoordinates, 
            @JsonProperty("target") Tank target, 
            @JsonProperty("targetDestroyed") boolean targetDestroyed,
            @JsonProperty("bulletTrajectory") List<Point> bulletTrajectory,
            @JsonProperty("tankMovement") List<Point> tankMovement) {
        this.player = player;
        this.type = type;
        this.hitCoordinates = hitCoordinates;
        this.target = target;
        this.targetDestroyed = targetDestroyed;
        this.bulletTrajectory = bulletTrajectory;
        this.tankMovement = tankMovement;
    }
    
    
}

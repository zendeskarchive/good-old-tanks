package com.getbase.hackkrk.tanks.server.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.getbase.hackkrk.tanks.server.model.tournament.Player;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;

import lombok.Data;

@Data
public class Tank {
    @JsonUnwrapped
    private final Player player;

    private final int hp;

    private final Point position;

    @JsonCreator
    public Tank(
            @JsonProperty("player") Player player, 
            @JsonProperty("hp") int hp, 
            @JsonProperty("position") Point position) {
        this.player = player;
        this.hp = hp;
        this.position = position;
    }

    public boolean isAlive() {
        return hp > 0;
    }
}

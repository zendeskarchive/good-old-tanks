package com.getbase.hackkrk.tanks.server.model.game;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Move {
    private Double shotAngle;
    private Double shotPower;
    private Double moveDistance;

    public Move(int shotAngle, int shotPower, int moveDistance) {
        this((double) shotAngle, (double) shotPower, (double) moveDistance);
    }
    
    @JsonIgnore
    public boolean isValid(){
        return shotPower == null || shotPower == 0 || moveDistance == 0 || moveDistance == null;
    }
}

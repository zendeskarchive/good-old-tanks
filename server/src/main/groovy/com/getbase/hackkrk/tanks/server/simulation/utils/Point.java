package com.getbase.hackkrk.tanks.server.simulation.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import mikera.vectorz.Vector2;

@Data
public class Point {
    private final double x;
    private final double y;

    public static Point of(double x, double y) {
        return new Point(x, y);
    }

    @JsonCreator
    public Point(@JsonProperty("x") double x, @JsonProperty("y") double y) {
        this.x = x;
        this.y = y;
    }

    public Point(Vector2 vector) {
        this(vector.x, vector.y);
    }
    
    public Vector2 toVector(){
        return Vector2.of(x, y);
    }
}
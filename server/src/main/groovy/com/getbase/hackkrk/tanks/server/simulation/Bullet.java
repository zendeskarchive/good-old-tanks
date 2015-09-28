package com.getbase.hackkrk.tanks.server.simulation;

import lombok.ToString;
import mikera.vectorz.Vector2;

@ToString
public class Bullet {
    Vector2 position;
    Vector2 velocity;
    TankState owner;

    public Bullet(Vector2 position, Vector2 velocity, TankState owner) {
        this.position = position;
        this.velocity = velocity;
        this.owner = owner;
    }
}
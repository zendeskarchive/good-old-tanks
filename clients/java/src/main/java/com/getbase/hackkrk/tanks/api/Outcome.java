package com.getbase.hackkrk.tanks.api;

import mikera.vectorz.Vector2;

public class Outcome {
    public String name;
    public HitType type;
    public Vector2 hitCoordinates;
    public Tank target;
    public boolean targetDestroyed;
    
    public static enum HitType{
        miss,
        tank_hit,
        ground_hit
    }
}

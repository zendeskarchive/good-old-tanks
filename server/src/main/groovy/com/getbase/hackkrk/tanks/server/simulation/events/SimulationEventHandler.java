package com.getbase.hackkrk.tanks.server.simulation.events;

public interface SimulationEventHandler {
    default void onEvent(TankHit event){}
    default void onEvent(BulletGroundHit event){}
    default void onBulletPositionChange(BulletPositionChange h){}
    default void onBulletGroundHit(BulletGroundHit bulletGroundHit){}
    default void onTankHealthChange(TankHealthChange tankHealthChange){}
    default void onTankHit(TankHit tankHit){}
    default void onTankPositionChange(TankPositionChange tankPositionChange){}
}

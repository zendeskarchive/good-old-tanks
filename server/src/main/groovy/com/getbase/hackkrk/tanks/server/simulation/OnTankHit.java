package com.getbase.hackkrk.tanks.server.simulation;

public interface OnTankHit {
    void onHit(TankState tank, Bullet bullet);
}

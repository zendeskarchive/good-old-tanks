package com.getbase.hackkrk.tanks.server.simulation;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.stream.Stream;

import com.getbase.hackkrk.tanks.server.model.game.Move;
import com.getbase.hackkrk.tanks.server.model.game.Tank;
import com.getbase.hackkrk.tanks.server.model.scene.Physics;
import com.getbase.hackkrk.tanks.server.simulation.Simulation.SeriousEnterpriseEventBus;
import com.getbase.hackkrk.tanks.server.simulation.events.SimulationEvent;
import com.getbase.hackkrk.tanks.server.simulation.events.TankHealthChange;
import com.getbase.hackkrk.tanks.server.simulation.events.TankHit;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;
import com.google.common.base.Preconditions;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import mikera.vectorz.Vector2;

@Slf4j
@ToString
public class TankState {
    private final SeriousEnterpriseEventBus<SimulationEvent> events;
    private final Physics physics;

    private final double shotAngle;
    private final double shotPower;

    private double distanceToMoveHoriz = 0;
    private double distanceMovedHoriz = 0;
    private double distanceMovedAlongTerrain = 0;

    final Tank tank;

    double health;
    Vector2 position;

    public TankState(Move move, Tank tank, Physics physics, SeriousEnterpriseEventBus<SimulationEvent> events) {
        this.physics = physics;
        this.events = events;
        this.tank = tank;
        health = tank.getHp(); // FIXME int or double?

        position = Vector2.of(tank.getPosition().getX(), tank.getPosition().getY());

        if (move == null) {
            shotAngle = 0;
            shotPower = 0;
            distanceToMoveHoriz = 0;
        } else {
            shotAngle = move.getShotAngle() != null ? move.getShotAngle() : 0; // FIXME unit? degrees? // FIXME move can be null

            double power = Math.min(move.getShotPower() != null ? move.getShotPower() : 0, physics.getMaxShotPower());
            shotPower = Math.max(power, 0);

            distanceToMoveHoriz = move.getMoveDistance() != null ? move.getMoveDistance() : 0;
        }
    }

    /** Returns 0 or 1 bullets, depending if the tank is firing or not. */
    public Stream<Bullet> spawnBullet() {
        if(!isAlive() || shotPower <= 0){
            return Stream.empty();
        }else{
            Vector2 newPosition = position.clone();
            newPosition.add(Vector2.of(0, physics.getTankTurretHeight()));
            return Stream.of(new Bullet(newPosition, firingVector(), this));
        }
    }

    private Vector2 firingVector() {
        double angleRad = Math.toRadians(shotAngle);

        return Vector2.of(sin(angleRad) * shotPower * physics.getPowerMultiplier(), cos(angleRad) * shotPower * physics.getPowerMultiplier());
    }

    public boolean isAlive() {
        return health > 0;
    }

    public boolean canBeHitBy(Bullet b) {
        return b.owner != this && isAlive();
    }

    public void hit(Bullet bullet, double time) {
        Preconditions.checkArgument(canBeHitBy(bullet));

        health -= physics.getBulletDamage();
        health = Math.max(0, health);

        events.add(new TankHit(time, 
                tank.getPlayer(), 
                bullet.owner.tank.getPlayer(), 
                health, 
                new Point(bullet.position),
                !isAlive()));
        
        events.add(new TankHealthChange(time, tank.getPlayer(), health));

        log.info("'{}' hit by bullet fired by '{}', remaining health: {}", 
                tank.getPlayer().getName(), 
                bullet.owner.tank.getPlayer().getName(), 
                health);
    }

    public boolean canMove() {
        if(!isAlive()){
            return false;
        }
        if (abs(distanceMovedHoriz) >= abs(distanceToMoveHoriz)) {
            return false;
        }
        if (abs(distanceMovedAlongTerrain) >= abs(physics.getMaxMovePerTurn())) {
            return false;
        }
        return true;
    }

    public void movedBy(double horizontalDistance, double distanceAlongTerrain) {
        Preconditions.checkArgument(horizontalDistance >= 0);
        Preconditions.checkArgument(distanceAlongTerrain >= 0);

        distanceMovedHoriz += horizontalDistance;
        distanceMovedAlongTerrain += distanceAlongTerrain;
    }

    public double getMoveSpeed() {
        return Math.signum(distanceToMoveHoriz) * physics.getMaxMoveSpeed();
    }
}

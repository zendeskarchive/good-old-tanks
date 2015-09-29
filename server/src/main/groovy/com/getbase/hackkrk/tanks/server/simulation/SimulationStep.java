package com.getbase.hackkrk.tanks.server.simulation;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.getbase.hackkrk.tanks.server.model.scene.Physics;
import com.getbase.hackkrk.tanks.server.model.scene.Scene;
import com.getbase.hackkrk.tanks.server.model.scene.topology.MapTopology;
import com.getbase.hackkrk.tanks.server.simulation.Simulation.SeriousEnterpriseEventBus;
import com.getbase.hackkrk.tanks.server.simulation.events.BulletGroundHit;
import com.getbase.hackkrk.tanks.server.simulation.events.BulletPositionChange;
import com.getbase.hackkrk.tanks.server.simulation.events.SimulationEvent;
import com.getbase.hackkrk.tanks.server.simulation.events.TankPositionChange;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;
import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

import mikera.vectorz.Vector2;

public class SimulationStep {
    private final double time;
    private final double dt;
    private List<Bullet> remainingBullets;
    private SeriousEnterpriseEventBus<SimulationEvent> events;

    public SimulationStep(double time, double dt, SeriousEnterpriseEventBus<SimulationEvent> events) {
        this.dt = dt;
        this.time = time;
        this.events = events;
    }
    
    public void perform(List<TankState> tanks, List<Bullet> bullets, Scene scene) {
        for(TankState tank : tanks){
            simulateTankMove(tank, scene);
        }
        simulateBullets(tanks, bullets, scene);
    }

    private void simulateTankMove(TankState tank, Scene scene) {
        Physics physics = scene.getPhysics();
        if(!tank.canMove()){
            return;
        }
        
        LineString terrain = scene.getLandscape().toGeometry();
        
        double moveBy = dt * tank.getMoveSpeed();
        
        LengthIndexedLine indexedTerrain = new LengthIndexedLine(terrain);
        double distanceFromLeft = indexedTerrain.project(new Coordinate(tank.position.x, tank.position.y));
        
        MapTopology topology = physics.getMapTopology();
        
        
        double newDistanceFromLeft = distanceFromLeft + moveBy;
        newDistanceFromLeft = topology.adjustOutsidePosition(newDistanceFromLeft, 
                indexedTerrain.getStartIndex(), 
                indexedTerrain.getEndIndex());

        
        Coordinate newPosition = indexedTerrain.extractPoint(newDistanceFromLeft);
        double horizontalDistance = topology.horizontalDistance(tank.position, newPosition, scene.getLandscape());
        tank.movedBy(horizontalDistance, abs(moveBy));
        
        tank.position.set(Vector2.of(newPosition.x, newPosition.y));
        
        events.add(new TankPositionChange(time, tank.tank.getPlayer(), new Point(tank.position)));
    }

    private void simulateBullets(List<TankState> tanks, List<Bullet> bullets, Scene scene) {
        MapTopology topology = scene.getPhysics().getMapTopology();
        Map<Bullet, Vector2> originalBulletPositions = bullets.stream().collect(Collectors.toMap(b -> b, b -> b.position.clone()));
        
        double wind = scene.getPhysics().getWind();
        double gravity = scene.getPhysics().getGravity();
        double airDragCoefficient = scene.getPhysics().getAirDragCoefficient();
        for(Bullet b : bullets){
            b.velocity.add(wind * dt, gravity * dt);
            b.velocity = applyAirDrag(b.velocity, airDragCoefficient, dt);
            
            b.position.addMultiple(b.velocity, dt);
            double correctedX = topology.adjustOutsideBulletPosition(b.position.x, scene.getLandscape().getMinX(), scene.getLandscape().getMaxX());
            
            // changing original position so that ground collision check works if bullet flew through the cylindric map wall
            // FIXME collision check does not work for tanks if bullet hit a tank and then appeared on the other side of the 
            //   map because of this "adjustment". it should contain real bullet paths, not just a point
            originalBulletPositions.get(b).x += correctedX-b.position.x;
            
            
            b.position.x = correctedX;
        }
        
        CollisionCheck cc = new CollisionCheck(bullets, tanks, originalBulletPositions, scene);
        cc.check((tank, bullet) -> onTankHit(tank, bullet, time));
        cc.adjustFinalBulletPositions();
        
        remainingBullets = new ArrayList<>(bullets);
        remainingBullets.removeAll(cc.getBulletsWhichHit());

        for(Bullet b : bullets){
            events.add(new BulletPositionChange(time, b.owner.tank.getPlayer(), new Point(b.position)));
        }
        for(Bullet b : cc.getBulletsWhichHitGround()){
            events.add(new BulletGroundHit(time, b.owner.tank.getPlayer(), new Point(b.position)));
        }
    }
    
    private void onTankHit(TankState tank, Bullet bullet, double time) {
        tank.hit(bullet, time);
    }

    public List<Bullet> getRemainingBullets() {
        Preconditions.checkNotNull(remainingBullets, "simulation step not performed yet");
        return remainingBullets;
    }
    
    private Vector2 applyAirDrag(Vector2 velocity, double airDragCoefficient, double dt) {
        double speed = velocity.magnitude();
        
        double x = applyAirDrag1D(velocity.x, airDragCoefficient, speed, dt);
        double y = applyAirDrag1D(velocity.y, airDragCoefficient, speed, dt);
        
        return Vector2.of(x, y);
    }

    private double applyAirDrag1D(double velocity, double airDragCoefficient, double speed, double dt) {
        double newVelocity = velocity - velocity * airDragCoefficient * speed * dt;
        
        // because drag cannot cause the object to move in opposite direction, but this could happen due to inaccuracies of the simulation (dt >> 0)
        if (differentSigns(velocity, newVelocity)) {
            newVelocity = 0;
        }
        return newVelocity;
    }

    private boolean differentSigns(double a, double b) {
        return signum(a) * signum(b) < 0;
    }
    
    public boolean isEnded(){
        // we could end it early once tanks stop moving and there are no more bullets in-flight
        return false;
    }
}

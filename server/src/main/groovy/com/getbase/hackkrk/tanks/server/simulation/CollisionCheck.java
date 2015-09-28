package com.getbase.hackkrk.tanks.server.simulation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.getbase.hackkrk.tanks.server.model.scene.Landscape;
import com.getbase.hackkrk.tanks.server.model.scene.Scene;
import com.google.common.collect.ImmutableSet;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import lombok.AllArgsConstructor;
import mikera.vectorz.Vector2;

@AllArgsConstructor
public class CollisionCheck {
    private static final GeometryFactory geom = new GeometryFactory();
    private static final int TANK_BOUNDING_CIRCLE_PRECISION = 16;
    
    private final List<Bullet> bullets;
    private final List<TankState> tanks;
    private final Map<Bullet, Vector2> originalBulletPositions;
    private final Scene scene;
    
    
    private Set<Bullet> bulletsWhichHitGround = new HashSet<>();
    private Set<Bullet> bulletsNonGroundHits = new HashSet<>();
    private Map<Bullet, Vector2> bulletHitCoords = new HashMap<>();

    public CollisionCheck(List<Bullet> bullets, List<TankState> tanks, Map<Bullet, Vector2> originalBulletPositions, Scene scene) {
        this.bullets = bullets;
        this.tanks = tanks;
        this.originalBulletPositions = originalBulletPositions;
        this.scene = scene;
    }
    
    public void check(OnTankHit onTankHit){
        Map<Bullet, Geometry> lastTrajectorySegment = buildLastTrajectorySegments();
        Map<TankState, Geometry> tankHitboxes = buildTankHitboxes(tanks);
        
        for(Bullet b : bullets){
            Geometry bulletShift = lastTrajectorySegment.get(b);
            checkTankHits(onTankHit, tankHitboxes, b, bulletShift);
        }
        
        for(Bullet b : bullets){
            Geometry bulletShift = lastTrajectorySegment.get(b);
            checkGroundHits(scene.getLandscape(), b, bulletShift);
        }
    }

    private void checkGroundHits(Landscape landscape, Bullet b, Geometry bulletShift) {
        if(getBulletsWhichHit().contains(b)){
            return;
        }
        Geometry intersection = bulletShift.intersection(landscape.getCollisionShape());
        if(!intersection.isEmpty()){
            adjustBulletPosition(b, intersection.getCoordinate());
            bulletsWhichHitGround.add(b);
        }else if(b.position.y < 0){
            bulletsWhichHitGround.add(b);
        }
    }

    private void checkTankHits(OnTankHit tankHit, Map<TankState, Geometry> tankHitboxes, Bullet b, Geometry bulletShift) {
        for(TankState tank : tankHitboxes.keySet()){
            if(getBulletsWhichHit().contains(b)){
                return;
            }
            if(!tank.canBeHitBy(b)){
                continue;
            }
            Geometry hitbox = tankHitboxes.get(tank);
            Geometry intersection = bulletShift.intersection(hitbox);
            if(!intersection.isEmpty()){
                adjustBulletPosition(b, intersection.getCoordinate());
                tankHit.onHit(tank, b);
                bulletsNonGroundHits.add(b);
            }
        }
    }
    
    private void adjustBulletPosition(Bullet b, Coordinate coordinate) {
        Vector2 newPos = Vector2.of(coordinate.x, coordinate.y);
        b.position.set(newPos);
    }

    public Set<Bullet> getBulletsWhichHitGround(){
        return bulletsWhichHitGround;
    }
    
    public Set<Bullet> getBulletsWhichHit() {
        return ImmutableSet.<Bullet>builder()
                .addAll(bulletsNonGroundHits)
                .addAll(bulletsWhichHitGround)
                .build();
    }
    
    private Map<TankState, Geometry> buildTankHitboxes(List<TankState> tanks) {
        Map<TankState, Geometry> result = new HashMap<>();
        GeometricShapeFactory shapes = new GeometricShapeFactory(geom);
        shapes.setNumPoints(TANK_BOUNDING_CIRCLE_PRECISION);
        
        for(TankState tank : tanks){
            shapes.setCentre(new Coordinate(tank.position.x, tank.position.y));
            shapes.setSize(scene.getPhysics().getTankDiameter());
            result.put(tank, shapes.createCircle());
        }
        
        return result;
    }

    private Map<Bullet, Geometry> buildLastTrajectorySegments() {
        Map<Bullet, Geometry> lastTrajectorySegment = new HashMap<>();
        for(Bullet b: bullets){
            LineString line = line(originalBulletPositions.get(b), b.position);
            lastTrajectorySegment.put(b, line);
        }
        return lastTrajectorySegment;
    }

    private LineString line(Vector2 from, Vector2 to) {
        Coordinate[] coords = new Coordinate[]{
                new Coordinate(from.x, from.y),
                new Coordinate(to.x, to.y)
        };
        return geom.createLineString(coords);
    }

    public void adjustFinalBulletPositions() {
        // TODO fix bullet position so that it stops at the thing it hit, without crossing to the other side
    }
}

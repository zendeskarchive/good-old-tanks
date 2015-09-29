package com.getbase.hackkrk.tanks.server.model.scene.topology;

import com.getbase.hackkrk.tanks.server.model.scene.Landscape;
import com.vividsolutions.jts.geom.Coordinate;

import mikera.vectorz.Vector2;

public class BoundedMapTopology implements MapTopology {
    public double adjustOutsidePosition(double position, double start, double end) {
        if (position > end) {
            return end;
        } else if (position < start) {
            return start;
        } else {
            return position;
        }
    }
    
    public double adjustOutsideBulletPosition(double position, double start, double end) {
        return position;
    }

    @Override
    public double horizontalDistance(Vector2 position, Coordinate newPosition, Landscape landscape) {
        return Math.abs(position.x - newPosition.x);
    }
}

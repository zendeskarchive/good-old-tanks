package com.getbase.hackkrk.tanks.server.model.scene.topology;

import static java.lang.Math.abs;

import com.getbase.hackkrk.tanks.server.model.scene.Landscape;
import com.vividsolutions.jts.geom.Coordinate;

import mikera.vectorz.Vector2;

public class CylindricMapTopology implements MapTopology {
    public double adjustOutsidePosition(double position, double start, double end) {
        while (position > end) {
            position -= Math.abs(end - start);
        }
        while (position < start) {
            position += Math.abs(end - start);
        }
        return position;
    }
    
    public double adjustOutsideBulletPosition(double position, double start, double end) {
        return adjustOutsidePosition(position, start, end);
    }

    // calculate distance depending if it's closer directly or via the 'cylinder' (map edges)
    @Override
    public double horizontalDistance(Vector2 position, Coordinate newPosition, Landscape landscape) {
        double a = position.x;
        double b = newPosition.x;

        double min = landscape.getMinX();
        double max = landscape.getMaxX();

        if (abs(a - b) < abs(min - max) / 2) {
            return abs(a - b);
        } else {
            double distanceToLeft = Math.min(abs(a - min), abs(b - min));
            double distanceToRight = Math.min(abs(a - max), abs(b - max));
            return distanceToLeft + distanceToRight;
        }
    }
}

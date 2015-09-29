package com.getbase.hackkrk.tanks.server.model.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;
import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import lombok.Data;

@Data
/**
 * Terrain (area under the line described by this class) is assumed to be a convex polygon (no caves, bridges or holes).
 */
public class Landscape {
    private static final GeometryFactory GEOMETRY = new GeometryFactory();
    
    @JsonUnwrapped
    private final List<Point> points;
    @JsonIgnore
    private final LineString geometry;
    @JsonIgnore
    private final double minX;
    @JsonIgnore
    private final double maxX;
    @JsonIgnore
    private final LineString collisionShape;

    @JsonCreator
    public Landscape(@JsonProperty("points") List<Point> points){
        this.points = points;
        geometry = initGeometry(points);
        collisionShape = initCollisionShape(points);
        minX = points.stream()
                .min((a,b) -> Double.compare(a.getX(), b.getX()))
                .get().getX();
        maxX = points.stream()
                .max((a,b) -> Double.compare(a.getX(), b.getX()))
                .get().getX();
    }
    
    public double getMinX(){
        return minX;
    }
    
    public double getMaxX(){
        return maxX;
    }

    @JsonIgnore
    public double getWidth() {
        return maxX - minX;
    }

    private static LineString initGeometry(List<Point> points){
        List<Coordinate> coords = coordinatesList(points);
        return GEOMETRY.createLineString(coords.toArray(new Coordinate[coords.size()]));
    }

    private static List<Coordinate> coordinatesList(List<Point> points) {
        return points.stream().map(p -> new Coordinate(p.getX(), p.getY())).collect(Collectors.toList());
    }

    public LineString toGeometry() {
        return geometry;
    }
    
    private static  LineString initCollisionShape(List<Point> points) {
        List<Coordinate> coords = coordinatesList(points);
        List<Coordinate> geometryCoords = new ArrayList<>();
        geometryCoords.add(new Coordinate(coords.get(0).x, 0));
        geometryCoords.addAll(coords);
        geometryCoords.add(new Coordinate(Iterables.getLast(coords).x, 0));
        return GEOMETRY.createLineString(geometryCoords.toArray(new Coordinate[geometryCoords.size()]));
    }
    
    /**
     * Returns terrain with additional elements. This shape can be safely used for testing for collisions against the ground. 
     */
    public LineString getCollisionShape(){
        return collisionShape;
    }
    
    /**
     * Calculates terrain height given horizontal coordinate. Algorithm:<ol>
     * <li>place the initial guess y0 above the ground</li>
     * <li>cast a ray downwards from (x,y0), down to the y=0</li>
     * <li>find the intersection of the ray and the ground</li>
     * <li>the intersection point has the correct y coordinate</li></ol>
     */
    public double findHeight(double x) {
        if(x <= minX){
            return points.get(0).getY();
        }else if (x >= maxX){
            return Iterables.getLast(points).getY();
        }
        
        double y = 10000;
        double minY = -10000;
        
        LineString ray = GEOMETRY.createLineString(new Coordinate[]{
                new Coordinate(x, y),
                new Coordinate(x, minY)
        });
        
        LineString terrain = toGeometry();
        
        Geometry intersection = ray.intersection(terrain);
        
        return intersection.getCoordinate().y;
    }
}

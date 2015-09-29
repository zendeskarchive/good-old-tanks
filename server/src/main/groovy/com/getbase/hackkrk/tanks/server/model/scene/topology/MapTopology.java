package com.getbase.hackkrk.tanks.server.model.scene.topology;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.getbase.hackkrk.tanks.server.model.scene.Landscape;
import com.vividsolutions.jts.geom.Coordinate;

import mikera.vectorz.Vector2;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BoundedMapTopology.class, name = "bounded"),
        @JsonSubTypes.Type(value = CylindricMapTopology.class, name = "cylindric"),
})
public interface MapTopology {
    double adjustOutsidePosition(double position, double start, double end);
    double adjustOutsideBulletPosition(double position, double start, double end);

    double horizontalDistance(Vector2 position, Coordinate newPosition, Landscape landscape);
}

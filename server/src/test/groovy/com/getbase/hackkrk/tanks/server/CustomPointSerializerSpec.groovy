package com.getbase.hackkrk.tanks.server;

import spock.lang.Specification

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.getbase.hackkrk.tanks.server.model.scene.Physics
import com.getbase.hackkrk.tanks.server.simulation.utils.Point

class CustomPointSerializerSpec extends Specification {
    ObjectMapper defaultMapper = new ObjectMapper()
    ObjectMapper mapper
    
    def setup(){
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule("MyDoubleModule", new Version(1, 0, 0, null));
        module.addSerializer(Point.class, new CustomPointSerializer());
        mapper.registerModule(module)
    }
    def "deserializes propertly"(){
        when:
        def p = Point.of(0, 0)
        def serialized = mapper.writeValueAsString(p)
        
        then:
        defaultMapper.readValue(serialized, Point) == p
    }
    
    def "serializes properly #x #y"(x,y,expected){
        when:
        def p = Point.of(x, y)
        def serialized = mapper.writeValueAsString(p)
        
        then:
        serialized == expected
        
        where:
        x           | y             | expected
        12.123123d  | 121233.13543  | '{"x":12.12,"y":121233.14}'
        1.000d      | 1.1001        | '{"x":1,"y":1.1}'
    }
    
    def "asd"(){
        when:
        ObjectMapper m = new ObjectMapper();
        Physics p = new Physics()
        byte[] bytes = m.writeValueAsBytes(p)
        Physics p2 = m.readValue(bytes, Physics)
        p2.setWind(10000)
        
        then:
        p.wind != p2.wind
    }
}

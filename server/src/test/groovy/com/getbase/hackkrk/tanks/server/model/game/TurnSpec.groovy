package com.getbase.hackkrk.tanks.server.model.game

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.getbase.hackkrk.tanks.server.model.tournament.Player
import com.getbase.hackkrk.tanks.server.simulation.utils.Point
import spock.lang.Specification

class TurnSpec extends Specification{
    def mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
    
    def 'serializes and deserializes empty turn'(){
        when:
        def s = mapper.writeValueAsString(new Turn(0, null, null, null, false))
        def d = mapper.readValue(s, Turn)
        
        then:
        mapper.writeValueAsString(d) == s
    }
    
    def 'serializes turn with requested moves'(){
        def requestedMoves = [
            new RequestedMove(new Player("a", "x"), new Move(11, 22, 33)),
            new RequestedMove(new Player("b", "y"), new Move(22, 45, 32)),
            new RequestedMove(new Player("c", "z"), new Move(33, 34, 12)),
            ]
        
        when:
        def s = mapper.writeValueAsString(new Turn(0, requestedMoves, null, null, false))
        def d = mapper.readValue(s, Turn)
        
        then:
        mapper.writeValueAsString(d) == s
    }
    
    def 'serializes turn with requested moves and tanks'(){
        def requestedMoves = [
            new RequestedMove(new Player("a", "x"), new Move(11, 22, 33)),
            new RequestedMove(new Player("b", "y"), new Move(22, 45, 32)),
            new RequestedMove(new Player("c", "z"), new Move(33, 34, 12)),
            ]
        def tanks = [
            new Tank(new Player("a", "x"), 123, Point.of(12.34, 56))]
        when:
        def s = mapper.writeValueAsString(new Turn(0, requestedMoves, tanks, null, false))
        def d = mapper.readValue(s, Turn)
        
        then:
        mapper.writeValueAsString(d) == s
    }
    
    def 'serializes turn with move outcomes'(){
        
        def p1 = new Player("a", "b")
        def m1 = MoveOutcome.builder()
            .player(p1) 
            .type(MoveOutcome.Type.miss);
        
        def m2 = MoveOutcome.builder()
            .player(new Player("boris", "yellow"))
            .type(MoveOutcome.Type.ground_hit)
            .hitCoordinates(Point.of(4, 5))
        
        def t1 = new Tank(p1, 0, Point.of(1,2))
        def m3 = MoveOutcome.builder()
            .player(new Player("alex", "green"))
            .type(MoveOutcome.Type.tank_hit)
            .hitCoordinates(Point.of(10,20.3))
            .target(t1)
            .targetDestroyed(t1.hp <= 0)
        
        when:
        println new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValueAsString(new Turn(0, null, null, [m1.build(),m2.build(),m3.build()], false))
        
        then:
        notThrown(Exception)
    }
    
    def 'can serialize and deserialize'(){
        def t = sampleTurn();
        
        def m = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
        
        when:
        def serialized = m.writeValueAsString(t)
        def d = mapper.readValue(serialized, Turn)
        
        then:
        mapper.writeValueAsString(d) == serialized
    }
    
    def sampleTurn(){
        def p1 = new Player("john", "red")
        
        def tanks = [new Tank(p1, 90, Point.of(34, 50.3))]
        
        def m1 = MoveOutcome.builder()
            .player(p1) 
            .type(MoveOutcome.Type.miss);
        
        def m2 = MoveOutcome.builder()
            .player(new Player("boris", "yellow"))
            .type(MoveOutcome.Type.ground_hit)
            .hitCoordinates(Point.of(4, 5))
        
        def m3 = MoveOutcome.builder()
            .player(new Player("alex", "green"))
            .type(MoveOutcome.Type.tank_hit)
            .hitCoordinates(Point.of(10,20.3))
            .target(tanks[0])
            .targetDestroyed(tanks[0].hp <= 0)
        
        def mo1 = m1.build()
        def mo2 = m2.build()
        def mo3 = m3.build()
        def moves = [mo1,mo2,mo3]
        
        def requestedMoves = [
            new RequestedMove(mo1.player, new Move(11, 22, 33)),
            new RequestedMove(mo2.player, new Move(22, 45, 32)),
            new RequestedMove(mo3.player, new Move(33, 34, 12)),
            ]
        
        new Turn(0, requestedMoves, tanks, moves, false)
    }
}

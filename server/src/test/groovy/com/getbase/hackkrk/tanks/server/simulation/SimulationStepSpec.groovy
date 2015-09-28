package com.getbase.hackkrk.tanks.server.simulation;

import static java.lang.Math.*

import java.util.stream.Collectors

import mikera.vectorz.Vector2
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import com.getbase.hackkrk.tanks.server.model.game.Move
import com.getbase.hackkrk.tanks.server.model.game.Tank
import com.getbase.hackkrk.tanks.server.model.scene.Landscape
import com.getbase.hackkrk.tanks.server.model.scene.LandscapeFactory
import com.getbase.hackkrk.tanks.server.model.scene.Physics
import com.getbase.hackkrk.tanks.server.model.scene.PhysicsProvider
import com.getbase.hackkrk.tanks.server.model.scene.Scene
import com.getbase.hackkrk.tanks.server.model.scene.topology.BoundedMapTopology
import com.getbase.hackkrk.tanks.server.model.scene.topology.CylindricMapTopology
import com.getbase.hackkrk.tanks.server.model.tournament.Player
import com.getbase.hackkrk.tanks.server.model.tournament.TournamentConfiguration
import com.getbase.hackkrk.tanks.server.simulation.Simulation.SeriousEnterpriseEventBus
import com.getbase.hackkrk.tanks.server.simulation.events.BulletGroundHit
import com.getbase.hackkrk.tanks.server.simulation.utils.Point


class SimulationStepSpec extends Specification {
    @Shared
    def landscape = new LandscapeFactory().create(true)
    def bus = new SeriousEnterpriseEventBus()
    
    def Physics makePhysics(int difficulty){
        def tc = TournamentConfiguration.createDefault()
        tc.physicsDifficulty = difficulty
        return new PhysicsProvider().provide(tc)
    }
    
    @Unroll
    def "test tank movement through the map edge when topologu=#mapTopology.class.simpleName, from #startX by #moveDist"(startX, moveDist, mapTopology, expectedXrange, expectedDistanceMovedHoriz){
        def physics = makePhysics(100)
        physics.mapTopology = mapTopology
        def scene = new Scene(landscape, physics)
        def step = new SimulationStep(0, physics.unitOfTime, bus)
        def move = new Move(100.0, 100.0, moveDist)
        def tank = new Tank(new Player("boris","red"), 5, Point.of(startX, landscape.findHeight(startX)))
        def tankstate = new TankState(move, tank, scene.physics, bus)
        
        when:
        step.perform([tankstate], [], scene)
        
        then:
        def finalX = tankstate.position.x
        finalX >= expectedXrange[0]
        finalX <= expectedXrange[1]
        expectedDistanceMovedHoriz[0] <= tankstate.distanceMovedHoriz
        tankstate.distanceMovedHoriz <= expectedDistanceMovedHoriz[1]
        
        where:
        startX         | moveDist | mapTopology                | expectedXrange                             | expectedDistanceMovedHoriz
        landscape.minX |  -10     | new CylindricMapTopology() | [landscape.maxX-5,    landscape.maxX-0.01] | [0.1, 1]
        landscape.maxX |  +10     | new CylindricMapTopology() | [landscape.minX+0.01, landscape.minX+5]    | [0,1, 1]
        landscape.minX |  -10     | new BoundedMapTopology()   | [landscape.minX,      landscape.minX]      | [0, 0]
        landscape.maxX |  +10     | new BoundedMapTopology()   | [landscape.maxX,      landscape.maxX]      | [0, 0]
    }
    
    @Unroll
    def "test bullet movement through the map edge when cylindricMap=#mapTopology.class.simpleName, from #startX by #moveDist"(startX, moveDist, mapTopology, expectedXrange){
        def physics = makePhysics(100)
        physics.mapTopology = mapTopology
        def scene = new Scene(landscape, physics)
        def step = new SimulationStep(0, physics.unitOfTime, bus)
        def move = new Move(100.0, 100.0, moveDist)
        def tank = new Tank(new Player("boris","red"), 5, Point.of(0, 0))
        def tankstate = new TankState(move, tank, scene.physics, bus)
        def b = new Bullet(Vector2.of(startX, 1000), Vector2.of(moveDist, 0), tankstate)
        
        when:
        step.perform([], [b], scene)
        
        then:
        def finalX = b.position.x
        finalX >= expectedXrange[0]
        finalX <= expectedXrange[1]
        
        where:
        startX                  | moveDist | mapTopology                | expectedXrange
        landscape.minX          |  -10     | new CylindricMapTopology() | [landscape.maxX-5, landscape.maxX] 
        landscape.minX+0.001    |  -10     | new CylindricMapTopology() | [landscape.maxX-5, landscape.maxX]
        landscape.maxX          |  +10     | new CylindricMapTopology() | [landscape.minX,   landscape.minX+5]
        landscape.maxX-0.001    |  +10     | new CylindricMapTopology() | [landscape.minX,   landscape.minX+5]
        
        landscape.minX          |  -10     | new BoundedMapTopology()   | [landscape.minX-5,    landscape.minX-0.001]
        landscape.minX-0.001    |  -10     | new BoundedMapTopology()   | [landscape.minX-5,    landscape.minX-0.001]
        landscape.minX+0.001    |  -10     | new BoundedMapTopology()   | [landscape.minX-5,    landscape.minX-0.001]
        landscape.minX-0.001    |  +10     | new BoundedMapTopology()   | [landscape.minX+0.001,landscape.minX+5]
        
        landscape.maxX          |  +10     | new BoundedMapTopology()   | [landscape.maxX+0.001,landscape.maxX+5]
        landscape.maxX+0.001    |  +10     | new BoundedMapTopology()   | [landscape.maxX+0.001,landscape.maxX+5]
        landscape.maxX-0.001    |  +10     | new BoundedMapTopology()   | [landscape.maxX+0.001,landscape.maxX+5]
        landscape.maxX+0.001    |  -10     | new BoundedMapTopology()   | [landscape.maxX-5,    landscape.maxX-0.001]
    }
    
    @Ignore // FIXME
    def "test hits the target even if travels through map edge"(){
        def physics = makePhysics(1)
        physics.airDragCoefficient = 0
        physics.gravity = 0
        physics.wind = 0
        physics.mapTopology = new CylindricMapTopology()
        def landscape = new Landscape([Point.of(-500, 10), Point.of(500, 10)])
        def scene = new Scene(landscape, physics)
        def step = new SimulationStep(0, physics.unitOfTime, bus)
        def p1 = new Player("boris","r")
        def p2 = new Player("alex","g")
        def p3 = new Player("alex","g")
        
        def t1 = new Tank(p1, 5, Point.of(0, 10))
        def t2 = new Tank(p2, 5, Point.of(10, 10))
        def t3 = new Tank(p3, 5, Point.of(20, 10))
        
        def move = new Move(100.0, 100.0, 0)
        
        def tankstate1 = new TankState(move, t1, scene.physics, bus)
        def tankstate2 = new TankState(move, t2, scene.physics, bus)
        def tankstate3 = new TankState(move, t3, scene.physics, bus)
        def b = new Bullet(Vector2.of(0, 10), Vector2.of(10000, 0), tankstate1)
        
        when:
        step.perform([tankstate1,tankstate2,tankstate3], [b], scene)
        println tankstate1
        println tankstate2
        println tankstate3
        
        then:
        tankstate1.health <= 0
    }
    
    def "test does not pierce through targets"(){
        def physics = makePhysics(1)
        physics.airDragCoefficient = 0
        physics.gravity = 0
        physics.wind = 0
        physics.mapTopology = new CylindricMapTopology()
        def landscape = new Landscape([Point.of(-500, 10), Point.of(500, 10)])
        def scene = new Scene(landscape, physics)
        def step = new SimulationStep(0, physics.unitOfTime, bus)
        def p1 = new Player("boris","r")
        def p2 = new Player("alex","g")
        def p3 = new Player("john","b")
        
        def t1 = new Tank(p1, 5, Point.of(0, 10))
        def t2 = new Tank(p2, 5, Point.of(10, 10))
        def t3 = new Tank(p3, 5, Point.of(20, 10))
        
        def move = new Move(100.0, 100.0, 0)
        
        def tankstate1 = new TankState(move, t1, scene.physics, bus)
        def tankstate2 = new TankState(move, t2, scene.physics, bus)
        def tankstate3 = new TankState(move, t3, scene.physics, bus)
        def b = new Bullet(Vector2.of(0, 10), Vector2.of(100/physics.unitOfTime, -20), tankstate1)
        
        when:
        step.perform([tankstate1,tankstate2,tankstate3], [b], scene)
        println tankstate1
        println tankstate2
        println tankstate3
        
        then:
        tankstate1.health == 5
        tankstate2.health == 0 || tankstate3.health == 0
        tankstate2.health == 5 || tankstate3.health == 5
        
        // a tank gets hit, so the bullet does not hit the ground behind it
        def events = bus.allEvents.collect(Collectors.toList())
        events.every { e -> ! (e instanceof BulletGroundHit) }
    }
}

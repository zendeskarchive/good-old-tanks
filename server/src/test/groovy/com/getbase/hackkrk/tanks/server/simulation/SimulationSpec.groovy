package com.getbase.hackkrk.tanks.server.simulation

import spock.lang.Ignore
import spock.lang.Specification

import com.getbase.hackkrk.tanks.server.model.game.Tank
import com.getbase.hackkrk.tanks.server.model.scene.LandscapeFactory
import com.getbase.hackkrk.tanks.server.model.scene.PhysicsProvider
import com.getbase.hackkrk.tanks.server.model.scene.Scene
import com.getbase.hackkrk.tanks.server.model.tournament.Player
import com.getbase.hackkrk.tanks.server.model.tournament.TournamentConfiguration
import com.getbase.hackkrk.tanks.server.simulation.utils.Point


class SimulationSpec extends Specification{
    def landscape = new LandscapeFactory().create(true)
    def physics = new PhysicsProvider().provide(TournamentConfiguration.createDefault())
    def scene = new Scene(landscape, physics)
    def p1 = new Player("boris","red")
    
    def "move for a tank is not required"(){
        Tank t = new Tank(p1, 100, Point.of(0 , landscape.findHeight(0)))
        
        when:
        new Simulation(scene, [:], [t]).simulate()
        
        then:
        notThrown(Exception)
    }
}

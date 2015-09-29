package com.getbase.hackkrk.tanks.server.model.tournament

import com.getbase.hackkrk.tanks.server.model.scene.LandscapeFactory

import spock.lang.Ignore;
import spock.lang.Specification

class PositionProviderSpec extends Specification {
    def "should assign correct position"() {
        given:
        def provider = new PositionProvider()
        def factory = new LandscapeFactory()
        factory.minX = -500.0
        factory.maxX = 500.0
        def landscape = factory.create(true)
        def players = [new Player('john', 'black'), new Player('keving', 'red'), new Player('marry', 'yellow')]
        def maxDeviation = 1000 /4 / 3

        when:
        println landscape.getWidth()
        def positions = provider.provide(landscape, players)
        def xs = positions.collect { it.value.getX() }.sort()

        then:
        /*
                   1          2           3
            250        250         250         250

         */
        xs[0] >= -250 - maxDeviation
        xs[0] <= -250 + maxDeviation
        xs[1] >= -maxDeviation
        xs[1] <= maxDeviation
        xs[2] >= 250 - maxDeviation
        xs[2] <= 250 + maxDeviation
    }
}

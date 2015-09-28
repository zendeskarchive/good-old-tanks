package com.getbase.hackkrk.tanks.server.model.scene

import spock.lang.Specification
import spock.lang.Unroll

class LandscapeFactorySpec extends Specification {

    @Unroll
    def "points should be in correct range for difficulty #difficulty"() {
        given:
        def factory = new LandscapeFactory()

        when:
        def points = factory.create(false, difficulty).getPoints()
        def ys = points.collect { it.y }
        def minY = ys.min()
        def maxY = ys.max()
        def height = factory.maxY - factory.minY
        println difficulty
        println points
        println minY
        println maxY
        println maxY - minY

        then:
        points.size() == factory.numberOfPoints
        points.each { assert it.y.round() >= factory.minY && it.y.round() <= factory.maxY && it.x >= factory.minX && it.x <= factory.maxX }
        points.first().x == factory.minX
        points.last().x == factory.maxX
        minY.round() == factory.minY
        (maxY - minY).round() <= difficulty / 100.0 * height + 1
        (maxY - minY).round() > difficulty / 100.0 * height - 1

        where:
        difficulty << (1..100)
    }

    @Unroll
    def "points should be in correct range for equal edge height and difficulty #difficulty"() {
        given:
        def factory = new LandscapeFactory()

        when:
        def points = factory.create(true, difficulty).getPoints()
        def ys = points.collect { it.y }
        def minY = ys.min()
        def maxY = ys.max()
        def height = factory.maxY - factory.minY
        println difficulty
        println points
        println minY
        println maxY
        println maxY - minY

        then:
        points.size() == factory.numberOfPoints
        points.each { assert it.y.round() >= factory.minY && it.y.round() <= factory.maxY && it.x >= factory.minX && it.x <= factory.maxX }
        points.first().x == factory.minX
        points.last().x == factory.maxX
        minY.round() == factory.minY
        (maxY - minY).round() <= difficulty / 100.0 * height + 1
        (maxY - minY).round() > difficulty / 100.0 * height - 1

        where:
        difficulty << (1..100)
    }
}

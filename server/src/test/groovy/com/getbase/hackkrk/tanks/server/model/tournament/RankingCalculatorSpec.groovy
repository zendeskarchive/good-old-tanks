package com.getbase.hackkrk.tanks.server.model.tournament

import com.getbase.hackkrk.tanks.server.model.game.Game
import com.getbase.hackkrk.tanks.server.model.game.MoveOutcome
import com.getbase.hackkrk.tanks.server.model.game.Tank
import com.getbase.hackkrk.tanks.server.model.game.Turn
import com.getbase.hackkrk.tanks.server.simulation.utils.Point
import spock.lang.Specification

class RankingCalculatorSpec extends Specification {

    def "should calculate score for health points"() {
        given:
        Tournament t = new Tournament(UUID.randomUUID().toString().substring(0, 8), [])
        def setup = new Game.Setup('aaa', null, [], 1, [:])
        def game = t.addGame(setup)
        def tank1 = new Tank(new Player('john', 'black'), 10, new Point(1,1))
        def tank2 = new Tank(new Player('kevin', 'red'), 0, new Point(1,1))
        def tank3 = new Tank(new Player('mary', 'green'), 20, new Point(1,1))
        def turn = new Turn(0, [], [tank1, tank2, tank3], [], false)
        game.addTurn(turn)
        def calculator = new RankingCalculator()
        def h = t.getConfiguration().pointsForHp

        when:
        calculator.calculateForLastGameAndUpdate(t)

        then:
        t.getRanking().getScore() == ['john': 10 * h, 'mary': 20 * h]

        when:
        calculator.calculateForLastGameAndUpdate(t)

        then:
        t.getRanking().getScore() == ['john': 20 * h, 'mary': 40 * h]
    }

    def "should calculate score for successful shots"() {
        given:
        Tournament t = new Tournament(UUID.randomUUID().toString().substring(0, 8), [])
        def setup = new Game.Setup('aaa', null, [], 1, [:])
        def game = t.addGame(setup)

        def john = new Player('john', 'black')
        def kevin = new Player('kevin', 'red')
        def mary = new Player('mary', 'green')

        def tank1 = new Tank(john, 0, new Point(1,1))
        def tank2 = new Tank(kevin, 0, new Point(1,1))
        def tank3 = new Tank(mary, 0, new Point(1,1))

        def outcomes1 = []
        outcomes1 << MoveOutcome.builder().player(mary).type(MoveOutcome.Type.tank_hit).build()
        outcomes1 << MoveOutcome.builder().player(john).type(MoveOutcome.Type.tank_hit).build()
        def turn1 = new Turn(0, [], [tank1, tank2, tank3], outcomes1, false)

        def outcomes2 = []
        outcomes2 << MoveOutcome.builder().player(mary).type(MoveOutcome.Type.tank_hit).build()
        def turn2 = new Turn(0, [], [tank1, tank2, tank3], outcomes2, false)

        game.addTurn(turn1)
        game.addTurn(turn2)

        def calculator = new RankingCalculator()

        def s = t.getConfiguration().pointsForSuccessfulShot

        when:
        calculator.calculateForLastGameAndUpdate(t)

        then:
        t.getRanking().getScore() == ['mary': 2 * s, 'john': 1 * s]
    }

    def "should calculate score for successful shots and remaining health points"() {
        given:
        Tournament t = new Tournament(UUID.randomUUID().toString().substring(0, 8), [])
        def setup = new Game.Setup('aaa', null, [], 1, [:])
        def game = t.addGame(setup)

        def john = new Player('john', 'black')
        def kevin = new Player('kevin', 'red')
        def mary = new Player('mary', 'green')

        def tank1 = new Tank(john, 10, new Point(1,1))
        def tank2 = new Tank(kevin, 5, new Point(1,1))
        def tank3 = new Tank(mary, 20, new Point(1,1))

        def outcomes1 = []
        outcomes1 << MoveOutcome.builder().player(mary).type(MoveOutcome.Type.tank_hit).build()
        outcomes1 << MoveOutcome.builder().player(john).type(MoveOutcome.Type.tank_hit).build()
        def turn1 = new Turn(0, [], [tank1, tank2, tank3], outcomes1, false)

        def outcomes2 = []
        outcomes2 << MoveOutcome.builder().player(mary).type(MoveOutcome.Type.tank_hit).build()
        def turn2 = new Turn(0, [], [tank1, tank2, tank3], outcomes2, false)

        game.addTurn(turn1)
        game.addTurn(turn2)

        def calculator = new RankingCalculator()
        def h = t.getConfiguration().pointsForHp
        def s = t.getConfiguration().pointsForSuccessfulShot

        when:
        calculator.calculateForLastGameAndUpdate(t)

        then:
        t.getRanking().getScore() == ['mary': 20 * h + 2 * s, 'john': 10 * h + 1 * s, 'kevin': 5 * h]
    }
}

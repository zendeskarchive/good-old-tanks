package com.getbase.hackkrk.tanks.server.model.tournament

import com.getbase.hackkrk.tanks.server.model.game.Game
import spock.lang.Specification

class PlayerProviderSpec extends Specification {

    def "provides players"() {
        given:
        Tournament t = new Tournament(UUID.randomUUID().toString().substring(0, 8), (1..100).toList()*.toString())
        t.addPlayer('1', "john")
        t.addPlayer('2', "james")
        t.addPlayer('3', "marry")
        t.addPlayer('4', "jason")
        t.addPlayer('5', "sarah")
        t.addPlayer('6', "kevin")
        PlayerProvider provider = new PlayerProvider()

        when: 'getting first set'
        PlayerSet set = provider.provide(t, 3)
        println set

        then: 'max players provided'
        set.round == 1
        set.players.size() == 3

        when: 'getting more players'
        t.addGame(new Game.Setup("aaa", null, set.getPlayers(), set.round, [:]))
        PlayerSet set2 = provider.provide(t, 3)
        println set2

        then: 'remaining players provided'
        set2.round == 1
        set2.players.size() == 3

        when: 'getting next set'
        t.addGame(new Game.Setup("bbb", null, set2.getPlayers(), set2.round, [:]))
        PlayerSet set3 = provider.provide(t, 3)
        println set3

        then: 'we choose players from entire pool again'
        set3.round == 2
        set3.players.size() == 3
    }

    def "if number remaining players < maxplayers then new round "() {
        given:
        Tournament t = new Tournament(UUID.randomUUID().toString().substring(0, 8), (1..100).toList()*.toString())
        t.addPlayer("1", "john")
        t.addPlayer("2", "james")
        t.addPlayer("3", "marry")
        t.addPlayer("4", "jason")
        t.addPlayer("5", "sarah")
        PlayerProvider provider = new PlayerProvider()

        when: 'getting first set'
        PlayerSet set = provider.provide(t, 3)
        println set

        then: 'max players provided'
        set.round == 1
        set.players.size() == 3

        when: 'getting more players'
        t.addGame(new Game.Setup("aaa", null, set.getPlayers(), set.round, [:]))
        PlayerSet set2 = provider.provide(t, 3)
        println set2

        then: 'new round and all players played'
        set2.round == 2
        set2.players.size() == 3
        t.getPlayers().each { assert set.players.contains(it) || set2.players.contains(it) }
    }


    def "if number of all players < max players provides correctly "() {
        given:
        Tournament t = new Tournament(UUID.randomUUID().toString().substring(0, 8), (1..100).toList()*.toString())
        t.addPlayer("1", "john")
        t.addPlayer("2", "james")
        PlayerProvider provider = new PlayerProvider()

        when: 'getting first set'
        PlayerSet set = provider.provide(t, 3)
        println set

        then: 'max players provided'
        set.round == 1
        set.players.size() == 2

        when: 'getting more players'
        t.addGame(new Game.Setup("aaa", null, set.getPlayers(), set.round, [:]))
        PlayerSet set2 = provider.provide(t, 3)
        println set2

        then: 'new round and all players played'
        set2.round == 2
        set2.players.size() == 2
        t.getPlayers().each { assert set.players.contains(it) || set2.players.contains(it) }
    }

    def "handles inactive players"() {
        given:
        Tournament t = new Tournament(UUID.randomUUID().toString().substring(0, 8), (1..100).toList()*.toString())
        t.addPlayer('1', "john")
        t.addPlayer('2', "james") // inactive
        t.addPlayer('3', "marry")
        t.addPlayer('4', "jason")
        t.addPlayer('5', "sarah")
        t.addPlayer('6', "kevin")
        t.getConfiguration().inactivePlayers = ['james']
        PlayerProvider provider = new PlayerProvider()

        when: 'getting first set'
        PlayerSet set = provider.provide(t, 3)
        println set

        then: 'max players provided'
        set.round == 1
        set.players.size() == 3
        !set.players.contains("james")

        when: 'getting more players'
        t.addGame(new Game.Setup("aaa", null, set.getPlayers(), set.round, [:]))
        PlayerSet set2 = provider.provide(t, 3)
        println set2

        then: 'remaining players provided'
        set2.round == 2
        set2.players.size() == 3
        !set2.players.contains("james")

        when: 'getting next set'
        t.addGame(new Game.Setup("bbb", null, set2.getPlayers(), set2.round, [:]))
        PlayerSet set3 = provider.provide(t, 3)
        println set3

        then: 'we choose players from entire pool again'
        set3.round == 3
        set3.players.size() == 3
        !set3.players.contains("james")

        when: 'getting next set while another player removed'
        t.getConfiguration().inactivePlayers = ['james', 'marry']
        t.addGame(new Game.Setup("bbb", null, set3.getPlayers(), set3.round, [:]))
        PlayerSet set4 = provider.provide(t, 3)
        println set4

        then:
        set4.round == 4
        set4.players.size() == 3
        !set4.players.contains("james")
        !set4.players.contains("marry")

        when: 'getting next set while another player removed'
        t.getConfiguration().inactivePlayers = ['james', 'marry', 'jason']
        t.addGame(new Game.Setup("bbb", null, set4.getPlayers(), set4.round, [:]))
        PlayerSet set5 = provider.provide(t, 3)
        println set5

        then:
        set5.round == 5
        set5.players.size() == 3
        !set5.players.contains("james")
        !set5.players.contains("marry")
        !set5.players.contains("jason")

        when: 'getting next set while another player removed'
        t.getConfiguration().inactivePlayers = ['james', 'marry', 'jason', 'sarah']
        t.addGame(new Game.Setup("bbb", null, set5.getPlayers(), set5.round, [:]))
        PlayerSet set6 = provider.provide(t, 3)
        println set6

        then:
        set6.round == 6
        set6.players.size() == 2
        !set6.players.contains("james")
        !set6.players.contains("marry")
        !set6.players.contains("jason")
        !set6.players.contains("sarah")

        when: 'all players removed'
        t.getConfiguration().inactivePlayers = ['james', 'marry', 'jason', 'sarah', 'kevin', 'john']
        t.addGame(new Game.Setup("bbb", null, set6.getPlayers(), set6.round, [:]))
        PlayerSet set7 = provider.provide(t, 3)
        println set7

        then:
        set7.round == 6
        set7.players.size() == 0

        when: 'players back'
        t.getConfiguration().inactivePlayers = ['james', 'marry', 'jason', 'sarah']
        t.addGame(new Game.Setup("bbb", null, set7.getPlayers(), set7.round, [:]))
        PlayerSet set8 = provider.provide(t, 3)
        println set8

        then:
        set8.round == 7
        set8.players.collect({ it.name }).sort() == ['kevin', 'john'].sort()

        when: 'single active player'
        t.getConfiguration().inactivePlayers = ['james', 'marry', 'jason', 'sarah', 'kevin']
        t.addGame(new Game.Setup("bbb", null, set8.getPlayers(), set8.round, [:]))
        PlayerSet set9 = provider.provide(t, 3)
        println set9

        then:
        set9.round == 7
        set9.players.size() == 0

        when: 'players back'
        t.getConfiguration().inactivePlayers = ['james', 'marry', 'jason', 'sarah']
        t.addGame(new Game.Setup("bbb", null, set9.getPlayers(), set9.round, [:]))
        PlayerSet set10 = provider.provide(t, 3)
        println set10

        then:
        set10.round == 8
        set10.players.collect({ it.name }).sort() == ['kevin', 'john'].sort()
    }

    def "should not provide players when 1 player"() {
        given:
        Tournament t = new Tournament(UUID.randomUUID().toString().substring(0, 8), (1..100).toList()*.toString())
        t.addPlayer('1', "john")
        PlayerProvider provider = new PlayerProvider()

        when: 'getting first set'
        PlayerSet set = provider.provide(t, 3)
        println set

        then: 'max players provided'
        set.round == 0
        set.players.size() == 0
    }
}

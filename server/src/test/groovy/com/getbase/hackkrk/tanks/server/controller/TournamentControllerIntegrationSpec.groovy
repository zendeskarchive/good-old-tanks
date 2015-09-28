package com.getbase.hackkrk.tanks.server.controller

import com.getbase.hackkrk.tanks.server.Application
import com.getbase.hackkrk.tanks.server.model.tournament.TournamentConfiguration
import com.jayway.awaitility.groovy.AwaitilityTrait
import groovyx.net.http.HttpResponseException
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.lang.Unroll

import static com.getbase.hackkrk.tanks.ApiHelper.*
import static com.jayway.awaitility.Awaitility.await

@ContextConfiguration(loader = SpringApplicationContextLoader, classes = Application)
@ActiveProfiles('development')
@WebIntegrationTest("server.port=0")
@Unroll
class TournamentControllerIntegrationSpec extends Specification implements AwaitilityTrait {
    @Value('${local.server.port}')
    int port

    def "should successfully create new Tournament"() {
        when:
        def tournament = createTournament(port)

        then:
        tournament.data.id
        tournament.data.adminToken
        tournament.data.players == []
        tournament.data.games == []
        tournament.data.state.type == "inactive"
    }

    def "should list created Tournaments"() {
        given:
        def tournaments = (1..5).collect {
            createTournament(port)
        }

        when:
        def response = client(port).get(
                path: "tournaments"
        )

        then:
        response.data.keySet().containsAll tournaments.data.id
    }

    def "should return tournament configuration"() {
        given:
        def tournament = createTournament(port)

        when:
        def configuration = client(port).get(
                path: "tournaments/$tournament.data.id/configuration",
                headers: [
                        Authorization: tournament.data.adminToken
                ],
        )

        then:
        def defaultConfiguration = TournamentConfiguration.createDefault()
        configuration.data.pauseRequested == defaultConfiguration.pauseRequested
        configuration.data.millisBetweenGames == defaultConfiguration.millisBetweenGames
        configuration.data.millisBetweenTurns == defaultConfiguration.millisBetweenTurns
    }

    def "should update tournament configuration"() {
        given:
        def tournament = createTournament(port)

        when:
        client(port).post(
                path: "tournaments/$tournament.data.id/configuration",
                headers: [
                        Authorization: tournament.data.adminToken
                ],
                body: [
                        pauseRequested    : 'true',
                        millisBetweenGames: 3000,
                        millisBetweenTurns: 2000,
                ]
        )

        then:
        def configuration = client(port).get(
                path: "tournaments/$tournament.data.id/configuration",
                headers: [
                        Authorization: tournament.data.adminToken
                ],
        )
        configuration.data.pauseRequested == true
        configuration.data.millisBetweenGames == 3000
        configuration.data.millisBetweenTurns == 2000
    }

    def "should successfully add new Player"() {
        given:
        def tournament = createTournament(port)

        when:
        def player = createPlayer(port, tournament.data.id, tournament.data.userTokens[0], 'Alex')

        then:
        player.data.name == 'Alex'
        player.data.color
    }

    def "should not add Player if user token is invalid"() {
        given:
        def tournament = createTournament(port)

        when:
        createPlayer(port, tournament.data.id, "token z D", 'Alex')

        then:
        def e = thrown(HttpResponseException)
        e.statusCode == 401
        e.response.data.message == "User token is not valid!"
    }

    def "should not add 2 Players with the smae name"() {
        given:
        def tournament = createTournament(port)
        createPlayer(port, tournament.data.id, tournament.data.userTokens[0], 'Alex')

        when:
        createPlayer(port, tournament.data.id, tournament.data.userTokens[1], 'Alex')

        then:
        def e = thrown(HttpResponseException)
        e.statusCode == 401
        e.response.data.message == "User 'Alex' already exists and belongs to another player!"
    }

    def "should not add Player named #playerName"(playerName) {
        given:
        def tournament = createTournament(port)

        when:
        createPlayer(port, tournament.data.id, tournament.data.userTokens[0], playerName)

        then:
        def e = thrown(HttpResponseException)
        e.statusCode != 200
        e.response.data.message == "Player name mustn't consist of any characters other than letters and spaces and mustn't be longer than 12 chars."

        where:
        playerName << [
                '1234567890123',
                'Żółć',
                'some -d',
                'hhh+huz',
        ]
    }

    def "should successfully start Tournament"() {
        given:
        def tournament = createTournament(port)

        when:
        def tournamentAfter = client(port).post(
                path: "tournaments/$tournament.data.id/start",
                headers: [
                        Authorization: tournament.data.adminToken
                ]
        )

        then:
        tournamentAfter.data.state.type == "beforeGame"

        and:
        await().until {
            try {
                def response = client(port).get(
                        path: "tournaments/$tournament.data.id/games/0"
                )
                response.data.setup.name
            } catch (HttpResponseException e) {
                false
            }
        }
    }

    def "should notify on game started"() {
        given:
        def tournament = createTournament(port)

        when:
        def tournamentId = tournament.data.id
        def callbacks = (1..500).collect {
            Thread.start {
                client(port).get(
                        path: "tournaments/$tournamentId/games/current",
                )
            }
        }
        sleep 1000

        then:
        callbacks.every { it.alive }

        when:
        client(port).post(
                path: "tournaments/$tournament.data.id/start",
                headers: [
                        Authorization: tournament.data.adminToken
                ]
        )

        then:
        await().until { callbacks.every { !it.alive } }
    }

    def "should return turn from game"() {
        given:
        def tournament = createTournament(port)
        createPlayer(port, tournament.data.id, tournament.data.userTokens[0], 'Alex')
        createPlayer(port, tournament.data.id, tournament.data.userTokens[1], 'Boris')

        when:
        client(port).post(
                path: "tournaments/$tournament.data.id/start",
                headers: [
                        Authorization: tournament.data.adminToken
                ]
        )

        then:
        await().until {
            try {
                client(port).get(path: "tournaments/${tournament.data.id}/games/0/turns/0")
            } catch (HttpResponseException e) {
                false
            }
        }
    }

    def "should successfully submit a Player move"() {
        given:
        def tournament = createTournament(port)
        createPlayer(port, tournament.data.id, tournament.data.userTokens[0], 'Alex')
        createPlayer(port, tournament.data.id, tournament.data.userTokens[1], 'Boris')
        client(port).post(
                path: "tournaments/$tournament.data.id/start",
                headers: [
                        Authorization: tournament.data.adminToken
                ]
        )

        when:
        def playerAccessToken = tournament.data.userTokens[0]
        client(port).get(
                path: "tournaments/$tournament.data.id/games/my/setup",
                headers: [
                        Authorization: playerAccessToken
                ]
        )
        def turn = client(port).post(
                path: "tournaments/$tournament.data.id/moves",
                headers: [
                        Authorization: playerAccessToken
                ],
                body: [
                        shotAngle   : 1.0,
                        shotPower   : 2.0,
                        moveDistance: 0
                ]
        )


        then:

        println "====== > " + turn.data
        turn.data.outcome.any { it -> it.name == 'Alex' }
        turn.data.requestedMoves == null
    }

    def "should return tournament ranking"() {
        given:
        def tournament = createTournament(port)
        createPlayer(port, tournament.data.id, tournament.data.userTokens[0], 'Alex')
        createPlayer(port, tournament.data.id, tournament.data.userTokens[1], 'Boris')
        createPlayer(port, tournament.data.id, tournament.data.userTokens[2], 'Batman')

        when:
        client(port).post(
                path: "tournaments/$tournament.data.id/configuration",
                headers: [
                        Authorization: tournament.data.adminToken
                ],
                body: [
                        inactivePlayers: ['Boris'],
                ]
        )
        def ranking = client(port).get(
                path: "tournaments/$tournament.data.id/ranking",
        )

        then:
        ranking.data.score['Alex'] == 0
        ranking.data.score['Batman'] == 0
        !ranking.data.score['Boris']
    }

}

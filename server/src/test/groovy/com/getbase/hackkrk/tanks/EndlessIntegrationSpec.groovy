package com.getbase.hackkrk.tanks

import com.getbase.hackkrk.tanks.bot.RandomBot
import com.getbase.hackkrk.tanks.server.Application
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Requires
import spock.lang.Specification
import spock.lang.Timeout

import static com.getbase.hackkrk.tanks.ApiHelper.*
import static java.util.concurrent.TimeUnit.DAYS

@Slf4j
@ContextConfiguration(loader = SpringApplicationContextLoader, classes = Application)
@ActiveProfiles('development')
@WebIntegrationTest("server.port=0")
@Requires({ System.properties.endless })
@Timeout(value = 3, unit = DAYS)
class EndlessIntegrationSpec extends Specification {

    @Value('${local.server.port}')
    int port

    def "should run endlessly... or almost endlessly"() {
        given:
        def tournament = createTournament(port)
        def userTokens = (1..8).collect {
            def userToken = tournament.data.userTokens[it]
            createPlayer(port, tournament.data.id, userToken, "player_$it")
            userToken
        }
        startTournament(port, tournament)

        when:
        def botThreads = userTokens.collect { String accessToken ->
            Thread.start(accessToken) {
                Thread.currentThread().name = "player-" + userTokens.indexOf(accessToken)
                def bot = new RandomBot("http://localhost:$port/", tournament.data.id, accessToken)
                log.info "Bot created {}", bot
                bot.run()
            }
        }

        then:
        def lastGame = -1d
        def lastRankingSum = -1L
        while (true) {
            sleep 30_000
            log.info "Checking health status"
            assert botThreads.every { it.alive }
            log.info "All bots are alive"
            log.info "Checking last game index"
            def currentGame = getLastGame(port, tournament, lastGame)
            assert currentGame > lastGame
            log.info "Current game > last game index {} > {}", currentGame, lastGame
            if ((currentGame - 0.05).intValue() > (lastGame + 0.05).intValue()) {
                log.info "Checking ranking progress"
                def currentRankingSum = getRankingSum(port, tournament)
                assert currentRankingSum > lastRankingSum
                log.info "Current ranking sum > last ranking sum {} > {}", currentRankingSum, lastRankingSum
                lastRankingSum = currentRankingSum
            }
            lastGame = currentGame
        }
    }

    def startTournament(int port, tournament) {
        client(port).post(
                path: "tournaments/${tournament.data.id}/start",
                headers: [
                        Authorization: tournament.data.adminToken
                ],
        )
    }

    private static double getLastGame(int port, tournament, double lastGame) {
        def currentGane = lastGame.intValue()
        while (true) {
            try {
                client(port).get(
                        path: "/tournaments/${tournament.data.id}/games/${currentGane + 1}"
                )
                currentGane++
            } catch (Exception e) {
                return currentGane + getLastTurn(port, tournament, currentGane) / 100
            }
        }
    }

    private static int getLastTurn(int port, tournament, int game) {
        def current = 0;
        while (true) {
            try {
                client(port).get(
                        path: "/tournaments/${tournament.data.id}/games/${game}/turns/$current"
                )
                current++
            } catch (Exception e) {
                return current
            }
        }
    }

    private static long getRankingSum(int port, tournament) {
        client(port).get(
                path: "/tournaments/${tournament.data.id}/ranking"
        ).data.score.values().sum()
    }

}

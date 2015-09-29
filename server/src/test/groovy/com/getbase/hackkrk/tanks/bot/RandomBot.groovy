package com.getbase.hackkrk.tanks.bot

import groovy.transform.ToString
import groovy.util.logging.Slf4j
import groovyx.net.http.RESTClient

import static groovyx.net.http.ContentType.JSON
import static java.util.concurrent.ThreadLocalRandom.current as random

@Slf4j
@ToString(includeFields = true, includePackage = false)
class RandomBot implements Runnable {

    private final String serverUrl

    private final String tournamentId

    private final String accessToken

    RandomBot(String serverUrl, String tournamentId, String accessToken) {
        this.serverUrl = serverUrl
        this.tournamentId = tournamentId
        this.accessToken = accessToken
    }

    @Override
    void run() {
        while (true) {
            waitForMyGameSetup()
            sleep 100
            submitMove()
        }
    }

    private waitForMyGameSetup() {
        log.info "Waiting for my game to start"
        while (true) {
            try {
                client().get(
                        path: "/tournaments/${tournamentId}/games/my/setup",
                        headers: [
                                Authorization: accessToken,
                        ]
                )

                log.info("Yep. My game is starting")
                break
            } catch (Exception e) {
                log.info "Error while waiting for current game to start", e
            }
        }
    }

    private submitMove() {
        def move = [
                shotAngle   : random().nextInt(-90, 90),
                shotPower   : random().nextInt(0, 150),
                moveDistance: random().nextInt(-30, 30)
        ]
        if(random().nextInt(0, 2) > 0){
            move.moveDistance = 0
        }else{
            move.shotPower = 0
        }
        log.info "Submitting move {}", move
        client().post(
                path: "tournaments/$tournamentId/moves",
                headers: [
                        Authorization: accessToken
                ],
                body: move
        )
    }

    private RESTClient client() {
        new RESTClient(serverUrl, JSON)
    }

}

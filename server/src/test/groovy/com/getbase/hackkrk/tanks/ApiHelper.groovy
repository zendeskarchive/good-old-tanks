package com.getbase.hackkrk.tanks

import groovyx.net.http.RESTClient

import static groovyx.net.http.ContentType.JSON

final class ApiHelper {

    static createTournament(int port, id = UUID.randomUUID().toString()[0..7]) {
        client(port).post(
                path: "tournaments/$id"
        )
    }

    static createPlayer(int port, String tournamentId, String userToken, String playerName) {
        client(port).post(
                path: "tournaments/${tournamentId}/players/${playerName}",
                headers: [
                        Authorization: userToken
                ],
        )
    }

    static RESTClient client(int port) {
        new RESTClient("http://localhost:$port/", JSON)
    }

    private ApiHelper() {
        // intentionally left blank
    }

}

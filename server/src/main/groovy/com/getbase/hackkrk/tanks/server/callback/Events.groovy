package com.getbase.hackkrk.tanks.server.callback

import com.getbase.hackkrk.tanks.server.model.game.Game
import com.getbase.hackkrk.tanks.server.model.game.Game.Setup
import com.getbase.hackkrk.tanks.server.model.game.Turn
import com.getbase.hackkrk.tanks.server.model.tournament.Player

final class Events {

    static Event<Game> gameStart(String tournamentId) {
        ["tournaments/$tournamentId/games/*/start"]
    }

    static Event<Setup> gameStart(String tournamentId, Player player) {
        ["tournaments/$tournamentId/games/my/$player/start"]
    }

    static Event<Turn> turnFinished(String tournamentId) {
        ["tournaments/$tournamentId/games/*/turns/*/finished"]
    }

    static Event<Turn> turnFinished(String tournamentId, int gameNumber, int turnNumber) {
        ["tournaments/$tournamentId/games/$gameNumber/turns/$turnNumber/finished"]
    }

    private Events() {
        // intentionally left blank
    }

}

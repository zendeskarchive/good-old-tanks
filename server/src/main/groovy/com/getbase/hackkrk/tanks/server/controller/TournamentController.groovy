package com.getbase.hackkrk.tanks.server.controller

import com.fasterxml.jackson.annotation.JsonView
import com.getbase.hackkrk.tanks.server.TournamentRepository
import com.getbase.hackkrk.tanks.server.TournamentStateMachine
import com.getbase.hackkrk.tanks.server.UserFriendlyTokenProvider
import com.getbase.hackkrk.tanks.server.callback.Callbacks
import com.getbase.hackkrk.tanks.server.model.game.Game
import com.getbase.hackkrk.tanks.server.model.game.Move
import com.getbase.hackkrk.tanks.server.model.game.Turn
import com.getbase.hackkrk.tanks.server.model.scene.View
import com.getbase.hackkrk.tanks.server.model.tournament.Player
import com.getbase.hackkrk.tanks.server.model.tournament.Tournament
import com.getbase.hackkrk.tanks.server.model.tournament.TournamentConfiguration
import com.getbase.hackkrk.tanks.server.model.tournament.state.BeforeGame
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.async.DeferredResult

import static com.getbase.hackkrk.tanks.server.callback.Events.gameStart
import static com.getbase.hackkrk.tanks.server.callback.Events.turnFinished
import static com.getbase.hackkrk.tanks.server.util.AsyncUtils.immediate
import static com.getbase.hackkrk.tanks.server.util.AsyncUtils.immediateError
import static org.springframework.web.bind.annotation.RequestMethod.GET
import static org.springframework.web.bind.annotation.RequestMethod.POST

@Slf4j
@RestController
class TournamentController {

    @Autowired
    private TournamentStateMachine stateMachine

    @Autowired
    private TournamentRepository repository

    @Autowired
    private Callbacks callbacks

    @Autowired
    private UserFriendlyTokenProvider tokenProvider

    @RequestMapping(
            method = GET,
            value = "/tournaments")
    Map listTournaments() {
        repository.list()*.id.collectEntries { tid ->
            [tid, repository.getById(tid).games*.number]
        }
    }

    @RequestMapping(
            method = POST,
            value = "/tournaments/{id}")
    Tournament createTournament(
            @PathVariable("id") String id,
            @RequestParam(value = "users", defaultValue = "30") int users) {
        if (id ==~ /[\w\d_]{1,36}/) {
            def tournament = new Tournament(id, tokenProvider.generate(users))
            log.info "Tournament created: {}", tournament
            repository.create(tournament)
        }else{
            throw new IllegalArgumentException("Tournament id '$id' is invalid. Must be alphanumeric.")
        }
    }

    @RequestMapping(
            method = GET,
            value = "/tournaments/{tournamentId}/players")
    List<Player> getPlayers(
            @PathVariable("tournamentId") String tournamentId) {
        repository
                .getById(tournamentId)
                .players
    }

    @RequestMapping(
            method = POST,
            value = "/tournaments/{tournamentId}/players/{playerName}")
    Player addPlayer(
            @PathVariable("tournamentId") String tournamentId,
            @PathVariable("playerName") String _playerName,
            @RequestHeader("Authorization") String userToken) {
        def playerName = _playerName.trim()
        log.info "Adding new {} to Tournament #{}", playerName, tournamentId
        if (playerName ==~ /\w[\w\s]{0,10}\w/) {
            Player player
            repository.withTournament(tournamentId) {
                player = addPlayer(userToken, playerName)
            }
            return player
        } else {
            throw new IllegalArgumentException("Player name mustn't consist of any characters other than letters and spaces and mustn't be longer than 12 chars.")
        }
    }

    @RequestMapping(
            method = POST,
            value = "/tournaments/{tournamentId}/start")
    Tournament startGame(
            @PathVariable("tournamentId") String tournamentId,
            @RequestHeader("Authorization") String adminToken) {
        def tournament = repository
                .getById(tournamentId)
                .checkAdmin(adminToken)
        stateMachine.toState(tournamentId, BeforeGame.create(tournament, tournament.state))
    }

    @JsonView(View.Players)
    @RequestMapping(
            method = GET,
            value = "/tournaments/{tournamentId}/games/current"
    )
    DeferredResult<Game> getCurrentGame(
            @PathVariable("tournamentId") String tournamentId) {
        repository
                .getById(tournamentId)
                .currentGame
                .map { immediate(it) }
                .orElseGet { callbacks.on(gameStart(tournamentId)) }
    }

    @JsonView(View.Players)
    @RequestMapping(
            method = GET,
            value = "/tournaments/{tournamentId}/games/my/setup"
    )
    DeferredResult<Game.Setup> getMyGameSetup(
            @PathVariable("tournamentId") String tournamentId,
            @RequestHeader("Authorization") String accessToken) {
        def tournament = repository.getById(tournamentId)
        def player = tournament.getPlayerByAccessToken(accessToken)
        tournament
                .currentGame
                .filter { g -> g.isParticipating(player) }
                .map { g -> g.setup }
                .map { s -> immediate(s) }
                .orElseGet { callbacks.on(gameStart(tournamentId, player)) }
    }

    @JsonView(View.Players)
    @RequestMapping(
            method = GET,
            value = "/tournaments/{tournamentId}/games/{gameNumber}")
    Game getGame(
            @PathVariable("tournamentId") String tournamentId,
            @PathVariable("gameNumber") int gameNumber) {
        repository
                .getById(tournamentId)
                .getGame(gameNumber)
                .orElseThrow { new NotFoundException("Game #$gameNumber not found!") }
    }
            
    @JsonView(View.Players)
    @RequestMapping(
            method = GET,
            value = "/tournaments/{tournamentId}/games/{gameNumber}/turns/{turnNumber}")
    DeferredResult<Turn> getTurn(
            @PathVariable("tournamentId") String tournamentId,
            @PathVariable("gameNumber") int gameNumber,
            @PathVariable("turnNumber") int turnNumber) {
        repository
                .getById(tournamentId)
                .getGame(gameNumber)
                .orElseThrow { new NotFoundException("Game #$gameNumber not found!") }
                .getTurn(turnNumber)
                .map { immediate(it) }
                .orElseGet { callbacks.on(turnFinished(tournamentId, gameNumber, turnNumber)) }
    }

    @JsonView(View.Players)
    @RequestMapping(
            method = POST,
            value = "/tournaments/{tournamentId}/moves")
    DeferredResult<Turn> submitMove(
            @PathVariable("tournamentId") String tournamentId,
            @RequestHeader("Authorization") String accessToken,
            @RequestBody Move move) {
        if (!move.valid) {
            return immediateError(new InvalidMoveException(move))
        }

        def tournament = repository.getById(tournamentId)
        def player = tournament.getPlayerByAccessToken(accessToken)
        // FIXME moves shall be rather submitted to Game than tournament
        // FIXME dead tank shall not be able to submit a move
//        tournament
//                .currentGame
//                .filter { g -> g.isParticipating(player) }
//                .orElseThrow { new IllegalStateException("You can only submit moves if your game is in progress!") }

        tournament.submitMove(player, move)
        callbacks.on(turnFinished(tournamentId))
    }

    @RequestMapping(
            method = GET,
            value = "/tournaments/{tournamentId}/ranking")
    Map getRanking(
            @PathVariable("tournamentId") String tournamentId) {
        def tournament = repository.getById(tournamentId)
        [score: tournament.ranking.getScoreForPlayers(tournament.activePlayers)]
    }


    @RequestMapping(
            method = GET,
            value = "/tournaments/{tournamentId}/configuration")
    TournamentConfiguration getConfiguration(
            @PathVariable("tournamentId") String tournamentId,
            @RequestHeader("Authorization") String adminToken) {
        repository
                .getById(tournamentId)
                .checkAdmin(adminToken)
                .configuration
    }

    @RequestMapping(
            method = POST,
            value = "/tournaments/{tournamentId}/configuration")
    TournamentConfiguration updateConfiguration(
            @PathVariable("tournamentId") String tournamentId,
            @RequestHeader("Authorization") String adminToken,
            @RequestBody TournamentConfiguration update) {
        repository
                .getById(tournamentId)
                .checkAdmin(adminToken)
        repository.withTournament(tournamentId, true) {
            configuration.merge(update)
        }.configuration
    }

}

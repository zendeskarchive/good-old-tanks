package com.getbase.hackkrk.tanks.server.model.tournament;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.getbase.hackkrk.tanks.server.controller.UnauthorizedException;
import com.getbase.hackkrk.tanks.server.model.game.Game;
import com.getbase.hackkrk.tanks.server.model.game.Move;
import com.getbase.hackkrk.tanks.server.model.game.Turn;
import com.getbase.hackkrk.tanks.server.model.tournament.state.Inactive;
import com.getbase.hackkrk.tanks.server.model.tournament.state.TournamentState;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC;
import static com.getbase.hackkrk.tanks.server.util.CollectionUtils.*;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PROTECTED;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@ToString
@Slf4j
@JsonAutoDetect(getterVisibility = PROTECTED_AND_PUBLIC)
public class Tournament {

    @Getter
    private final String id;

    @Getter(PROTECTED)
    private final String adminToken;

    @Getter
    private final TournamentConfiguration configuration;

    @Getter
    private final List<String> userTokens;

    private final List<Player> players;

    @Getter(PROTECTED)
    private final Map<Player, String> playerAccessTokens;

    private final List<Game> games;

    private final Map<Player, Move> submittedMoves;

    @Getter
    private final Ranking ranking;

    @Getter
    private TournamentState state;

    @JsonCreator
    protected Tournament(
            @JsonProperty("id") String id,
            @JsonProperty("adminToken") String adminToken,
            @JsonProperty("configuration") TournamentConfiguration configuration,
            @JsonProperty("userTokens") List<String> userTokens,
            @JsonProperty("players") List<Player> players,
            @JsonProperty("playerAccessTokens") Map<String, String> playerAccessTokens,
            @JsonProperty("games") List<Game> games,
            @JsonProperty("submittedMoves") Map<String, Move> submittedMoves,
            @JsonProperty("ranking") Ranking ranking,
            @JsonProperty("state") TournamentState state) {
        this.id = id;
        this.adminToken = adminToken;
        this.configuration = configuration;
        this.players = players;
        this.userTokens = unmodifiableList(userTokens);
        this.playerAccessTokens = expandKeys(playerAccessTokens, this::getPlayerByName);
        this.games = games;
        this.submittedMoves = expandKeys(submittedMoves, this::getPlayerByName);
        this.ranking = ranking;
        this.state = state;
    }

    public Tournament(String id, List<String> userTokens) {
        this(
                id,
                randomAlphanumeric(32),
                TournamentConfiguration.createDefault(),
                userTokens,
                new ArrayList<>(),
                new HashMap<>(),
                new ArrayList<>(),
                new LinkedHashMap<>(),
                new Ranking(),
                Inactive.createInitial());
    }

    public Tournament checkAdmin(String adminToken) {
        if (!Objects.equals(this.adminToken, adminToken)) {
            throw new IllegalArgumentException("Admin token does not match for Tournament #id" + this.id + "!");
        }

        return this;
    }

    public List<Player> getPlayers() {
        return unmodifiableList(this.players);
    }

    @JsonIgnore
    public List<Player> getActivePlayers() {
        return this.players
                .stream()
                .filter(p -> !configuration.getInactivePlayers().contains(p.getName()))
                .collect(toList());
    }

    public Player getPlayerByAccessToken(String accessToken) {
        return this.playerAccessTokens
                .entrySet()
                .stream()
                .filter(e -> e.getValue().equals(accessToken))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalArgumentException("Player token " + accessToken + " is not valid!"));
    }

    public List<Game> getGames() {
        return unmodifiableList(this.games);
    }

    public synchronized Player addPlayer(String userToken, String playerName) {
        if (!this.userTokens.contains(userToken)) {
            throw new UnauthorizedException("User token is not valid!");
        }

        if (this.getPlayerAccessTokens().values().contains(userToken)) {
            throw new UnauthorizedException("User token has already been used to add a Player!");
        }

        // FIXME color need to be generated here
        final Player player = new Player(playerName, "0x" + RandomStringUtils.random(6, "01234567890abcdef"));
        if (this.getPlayers().contains(player)) {
            throw new UnauthorizedException("User '" + playerName + "' already exists and belongs to another player!");
        }

        this.players.add(player);
        this.playerAccessTokens.put(player, userToken);
        // FIXME it shall be the ranking responsible for this
        this.ranking.incScore(player, 0L);
        return player;
    }

    public synchronized Game addGame(Game.Setup setup) {
        final Game game = new Game(this.games.size(), setup);
        this.games.add(game);
        return game;
    }

    public synchronized void addTurn(Turn turn) {
        getCurrentGame()
                .orElseThrow(() -> new IllegalStateException("No current game at this point for Tournament #" + this.id))
                .addTurn(turn);
    }

    public synchronized void submitMove(Player player, Move move) {
        this.submittedMoves.put(player, move);
    }

    public synchronized void setState(TournamentState newState) {
        if (!this.state.isTransitionAllowed(newState)) {
            throw new IllegalArgumentException("Transition " + this.state + " -> " + newState + " is not allowed!");
        }

        log.info("Changing tournament #{} state: {} -> {}", this.id, this.state, newState);
        this.state = newState;
    }

    public Optional<Game> getGame(int index) {
        return nth(this.games, index);
    }

    @JsonIgnore
    public Optional<Game> getLastGame() {
        return last(this.games);
    }

    @JsonIgnore
    public Optional<Game> getCurrentGame() {
        return getLastGame()
                .filter(g -> !g.isConcluded());
    }

    public Map<Player, Move> getSubmittedMoves() {
        return unmodifiableMap(this.submittedMoves);
    }

    public Player getPlayerByName(String name) {
        return this.players
                .stream()
                .filter(p -> p.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player " + name + " does not exist!"));
    }

}

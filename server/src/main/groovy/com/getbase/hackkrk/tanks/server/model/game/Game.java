package com.getbase.hackkrk.tanks.server.model.game;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.getbase.hackkrk.tanks.server.model.scene.Scene;
import com.getbase.hackkrk.tanks.server.model.tournament.Player;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.getbase.hackkrk.tanks.server.util.CollectionUtils.last;
import static com.getbase.hackkrk.tanks.server.util.CollectionUtils.nth;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

@Data
public class Game {

    private final int number;

    private final Setup setup;

    private final List<Turn> turns;

    public Game(int number, Setup setup) {
        this(number, setup, new ArrayList<>());
    }

    @JsonCreator
    protected Game(
            @JsonProperty("number") int number,
            @JsonProperty("setup") Setup setup,
            @JsonProperty("turns") List<Turn> turns) {
        this.number = number;
        this.setup = setup;
        this.turns = turns;
    }

    public List<Turn> getTurns() {
        return unmodifiableList(turns);
    }

    public void addTurn(Turn turn) {
        this.turns.add(turn);
    }

    @JsonIgnore
    public boolean isConcluded() {
        return getLastTurn()
                .map(Turn::isLast)
                .orElse(false);
    }

    public Optional<Turn> getTurn(int index) {
        return nth(this.turns, index);
    }

    @JsonIgnore
    public Optional<Turn> getLastTurn() {
        return last(this.turns);
    }

    public boolean isParticipating(Player player) {
        return this.setup.getPlayers().contains(player);
    }

    @Data
    public static class Setup {

        private final String name;

        private final Scene scene;

        private final List<Player> players;

        private final int round;

        private final Map<String, Point> initialPositions;

        public Setup(@JsonProperty("name") String name,
                     @JsonProperty("scene") Scene scene,
                     @JsonProperty("players") List<Player> players,
                     @JsonProperty("round") int round,
                     @JsonProperty("initialPositions") Map<String, Point> initialPositions) {
            this.name = name;
            this.scene = scene;
            this.players = unmodifiableList(players);
            this.round = round;
            this.initialPositions = unmodifiableMap(initialPositions);
        }

    }
}

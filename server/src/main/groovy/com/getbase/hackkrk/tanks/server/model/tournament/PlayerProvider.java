package com.getbase.hackkrk.tanks.server.model.tournament;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.toList;

@Component
public class PlayerProvider {

    public PlayerSet provide(Tournament tournament, int maxPlayersPerGame) {
        final List<Player> activePlayers = tournament.getActivePlayers();

        if (activePlayers.size() <= 1) {
            return new PlayerSet(lastRound(tournament).orElse(0), emptyList());
        }

        final List<Player> currentRoundPlayers = getCurrentRoundPlayers(tournament);
        final List<Player> unplayedPlayers = getUnplayedPlayersForCurrentRound(activePlayers, currentRoundPlayers);

        //TODO not really beautiful :/
        if (unplayedPlayers.size() == 0) {
            return new PlayerSet(
                    calculateRoundNumber(tournament, true),
                    selectPlayers(activePlayers, maxPlayersPerGame)
            );
        } else if (unplayedPlayers.size() < maxPlayersPerGame && unplayedPlayers.size() != activePlayers.size()) {
            return new PlayerSet(
                    calculateRoundNumber(tournament, true),
                    selectUnplayedAndSupplementFromAll(maxPlayersPerGame, activePlayers, unplayedPlayers)
            );
        } else {
            return new PlayerSet(
                    calculateRoundNumber(tournament, false),
                    selectPlayers(unplayedPlayers, maxPlayersPerGame)
            );
        }
    }

    private List<Player> selectUnplayedAndSupplementFromAll(int maxPlayersPerGame, List<Player> allPlayers,
                                                            List<Player> unplayedPlayers) {
        List<Player> players = new ArrayList<>();
        players.addAll(unplayedPlayers);

        final List<Player> from = allPlayers
                .stream()
                .filter(p -> !unplayedPlayers.contains(p))
                .collect(toList());
        players.addAll(selectPlayers(from, maxPlayersPerGame - unplayedPlayers.size()));
        return players;
    }

    private int calculateRoundNumber(Tournament tournament, boolean newRound) {
        int round = lastRound(tournament).orElse(1);
        return newRound ? round + 1 : round;
    }

    private Optional<Integer> lastRound(Tournament tournament) {
        return tournament.getLastGame().map(g -> g.getSetup().getRound());
    }

    private List<Player> getUnplayedPlayersForCurrentRound(List<Player> allPlayers, List<Player> currentRoundPlayers) {
        return allPlayers.stream()
                .filter(p -> !currentRoundPlayers.contains(p))
                .collect(toList());
    }

    private List<Player> getCurrentRoundPlayers(Tournament tournament) {
        return tournament
                .getLastGame()
                .map(g -> g.getSetup().getRound())
                .map(round -> tournament
                        .getGames()
                        .stream()
                        .filter(game -> game.getSetup().getRound() == round)
                        .flatMap(game -> game.getSetup().getPlayers().stream())
                        .collect(toList()))
                .orElse(emptyList());
    }

    private List<Player> selectPlayers(List<Player> from, int limit) {
        List<Player> tmpPlayers = new ArrayList<>(from);
        shuffle(tmpPlayers);
        return tmpPlayers.stream().limit(limit).collect(toList());
    }
}

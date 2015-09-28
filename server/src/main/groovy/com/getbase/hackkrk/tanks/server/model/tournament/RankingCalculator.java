package com.getbase.hackkrk.tanks.server.model.tournament;

import com.getbase.hackkrk.tanks.server.model.game.Game;
import com.getbase.hackkrk.tanks.server.model.game.MoveOutcome;
import com.getbase.hackkrk.tanks.server.model.game.Tank;
import com.getbase.hackkrk.tanks.server.model.game.Turn;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

@Component
public class RankingCalculator {

    /**
     * score = pointsForHp * number_of_player_hp_left + pointsForSuccessfulShot * number_of_player_successful_shots
     */
    public void calculateForLastGameAndUpdate(Tournament tournament) {
        calculatePointsForAliveTanks(tournament);
        calculatePointsForSuccessfulShots(tournament);
    }

    private void calculatePointsForSuccessfulShots(Tournament tournament) {
        final List<Turn> allTurns = tournament.getLastGame().map(Game::getTurns).orElse(emptyList());
        final Map<Player, Long> successfulShotsByPlayer = countSuccessfulShotsByPlayer(allTurns);

        successfulShotsByPlayer.forEach((player, shots) -> applyScore(tournament, player, shots * tournament.getConfiguration().getPointsForSuccessfulShot()));
    }

    private Map<Player, Long> countSuccessfulShotsByPlayer(List<Turn> allTurns) {
        return allTurns.stream().flatMap(t -> t.getOutcome().stream())
                .filter(o -> o.getType() == MoveOutcome.Type.tank_hit)
                .map(MoveOutcome::getPlayer)
                .collect(groupingBy(identity(), counting()));
    }

    private void calculatePointsForAliveTanks(Tournament tournament) {
        final List<Tank> aliveTanks = tournament
                .getLastGame()
                .flatMap(Game::getLastTurn)
                .map(this::aliveTanks)
                .orElse(emptyList());

        aliveTanks.forEach(t -> applyScore(tournament, t.getPlayer(), t.getHp() * tournament.getConfiguration().getPointsForHp()));
    }

    private List<Tank> aliveTanks(Turn t) {
        return t.getTanks()
                .stream()
                .filter(Tank::isAlive)
                .collect(toList());
    }

    private void applyScore(Tournament tournament, Player player, long scoreForGame) {
        tournament.getRanking().incScore(player, scoreForGame);
    }

}

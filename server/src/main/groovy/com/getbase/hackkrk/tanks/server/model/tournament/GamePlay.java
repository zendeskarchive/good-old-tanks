package com.getbase.hackkrk.tanks.server.model.tournament;

import com.getbase.hackkrk.tanks.server.model.game.Game;
import com.getbase.hackkrk.tanks.server.model.scene.*;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class GamePlay {
    @Autowired
    private LandscapeFactory landscapeFactory;

    @Autowired
    private PhysicsProvider physicsProvider;

    @Autowired
    private PlayerProvider playerProvider;

    @Autowired
    private PositionProvider positionProvider;

    public Game.Setup nextGameSetup(Tournament tournament) {
        TournamentConfiguration configuration = tournament.getConfiguration();
        Physics physics = physicsProvider.provide(configuration);
        final PlayerSet playerSet = playerProvider.provide(tournament, configuration.getMaxPlayersPerGame());
        final Landscape landscape = landscapeFactory.create(true, configuration.getLandscapeDifficulty());
        final Map<String, Point> initialPositions = positionProvider.provide(landscape, playerSet.getPlayers());

        log.info("using physics {}", physics);
        log.debug("using landscape {}", landscape);
        log.info("players for current game {}", playerSet);
        log.info("initial positions {}", initialPositions);

        return new Game.Setup(
                tournament.getId() + " game #" + tournament.getGames().size(),
                new Scene(landscape, physics),
                playerSet.getPlayers(),
                playerSet.getRound(),
                initialPositions
        );
    }

}

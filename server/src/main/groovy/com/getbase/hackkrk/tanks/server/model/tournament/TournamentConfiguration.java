package com.getbase.hackkrk.tanks.server.model.tournament;

import static java.lang.Boolean.FALSE;
import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.getbase.hackkrk.tanks.server.model.scene.Physics;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@ToString
public class TournamentConfiguration {

    private Boolean pauseRequested;

    private Long millisBetweenGames;

    private Long millisBetweenTurns;

    private Integer maxTurns;

    private Long pointsForHp;

    private Long pointsForSuccessfulShot;

    private Integer maxPlayersPerGame;

    private List<String> inactivePlayers = new ArrayList<>();

    /**
      must be in range of [1, 100]
     */
    private Integer landscapeDifficulty;

    private Physics physicsTemplate;
    
    /**
      must be in range of [1, 100]
    */
    private Integer physicsDifficulty;

    private Double physicsWindMean;

    private Double physicsWindStdev;

    private Double physicsGravityMean;

    private Double physicsGravityStdev;

    private Double physicsAirDragBase;

    private Double physicsAirDragMean;

    private Double physicsAirDragStdevMultiplier;

    public static TournamentConfiguration createDefault() {
        final TournamentConfiguration configuration = new TournamentConfiguration();
        configuration.pauseRequested = FALSE;
        configuration.millisBetweenGames = 5000L;
        configuration.millisBetweenTurns = 1000L;
        configuration.maxTurns = 200;
        configuration.pointsForHp = 50L;
        configuration.pointsForSuccessfulShot = 1000L;
        configuration.maxPlayersPerGame = 4;
        configuration.inactivePlayers = emptyList();
        configuration.landscapeDifficulty = 40;
        configuration.physicsTemplate = new Physics();
        configuration.physicsDifficulty = 1;
        configuration.physicsWindMean = 0.0;
        configuration.physicsWindStdev = 1.0;
        configuration.physicsGravityMean = -9.0;
        configuration.physicsGravityStdev = 0.5;
        configuration.physicsAirDragBase = 0.002;
        configuration.physicsAirDragMean = 0.001;
        configuration.physicsAirDragStdevMultiplier = 0.001;
        return configuration;
    }

    public TournamentConfiguration merge(TournamentConfiguration update) {
        Optional
                .ofNullable(update.getPauseRequested())
                .ifPresent(this::setPauseRequested);

        Optional
                .ofNullable(update.getMillisBetweenGames())
                .ifPresent(this::setMillisBetweenGames);

        Optional
                .ofNullable(update.getMillisBetweenTurns())
                .ifPresent(this::setMillisBetweenTurns);

        Optional
                .ofNullable(update.getMaxTurns())
                .ifPresent(this::setMaxTurns);

        Optional
                .ofNullable(update.getPointsForHp())
                .ifPresent(this::setPointsForHp);

        Optional
                .ofNullable(update.getPointsForSuccessfulShot())
                .ifPresent(this::setPointsForSuccessfulShot);

        Optional
                .ofNullable(update.getMaxPlayersPerGame())
                .ifPresent(this::setMaxPlayersPerGame);

        Optional
                .ofNullable(update.getInactivePlayers())
                .ifPresent(this::setInactivePlayers);

        Optional
                .ofNullable(update.getLandscapeDifficulty())
                .ifPresent(this::setLandscapeDifficulty);
        
        Optional.ofNullable(update.getPhysicsTemplate())
                .ifPresent(this::setPhysicsTemplate);
        Optional.ofNullable(update.getPhysicsDifficulty())
                .ifPresent(this::setPhysicsDifficulty);
        Optional.ofNullable(update.getPhysicsWindMean())
                .ifPresent(this::setPhysicsWindMean);
        Optional.ofNullable(update.getPhysicsWindStdev())
                .ifPresent(this::setPhysicsWindStdev);
        Optional.ofNullable(update.getPhysicsGravityMean())
                .ifPresent(this::setPhysicsGravityMean);
        Optional.ofNullable(update.getPhysicsGravityStdev())
                .ifPresent(this::setPhysicsGravityStdev);
        Optional.ofNullable(update.getPhysicsAirDragBase())
                .ifPresent(this::setPhysicsAirDragBase);
        Optional.ofNullable(update.getPhysicsAirDragMean())
                .ifPresent(this::setPhysicsAirDragMean);
        Optional.ofNullable(update.getPhysicsAirDragStdevMultiplier())
                .ifPresent(this::setPhysicsAirDragStdevMultiplier);

        log.info("Configuration after merging in update: {} <- {}", this, update);
        return this;
    }

}

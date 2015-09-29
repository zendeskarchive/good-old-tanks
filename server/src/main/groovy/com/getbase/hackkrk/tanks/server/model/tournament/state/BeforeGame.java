package com.getbase.hackkrk.tanks.server.model.tournament.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.getbase.hackkrk.tanks.server.TournamentRepository;
import com.getbase.hackkrk.tanks.server.TournamentStateMachine;
import com.getbase.hackkrk.tanks.server.callback.Callbacks;
import com.getbase.hackkrk.tanks.server.model.game.Game;
import com.getbase.hackkrk.tanks.server.model.tournament.GamePlay;
import com.getbase.hackkrk.tanks.server.model.tournament.Tournament;
import com.newrelic.api.agent.Trace;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static com.getbase.hackkrk.tanks.server.callback.Events.gameStart;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class BeforeGame extends AbstractTournamentState {

    private final Instant gameStartsAt;

    @JsonCreator
    BeforeGame(
            @JsonProperty("previous") TournamentState previous,
            @JsonProperty("gameStartsAt") @NonNull Instant gameStartsAt) {
        super(true, previous, Inactive.class, BeforeTurn.class);
        this.gameStartsAt = gameStartsAt;
    }

    public static BeforeGame create(Tournament tournament, TournamentState previous) {
        final Instant gameStartsAt = Instant.now().plus(Duration.ofMillis(tournament.getConfiguration().getMillisBetweenGames()));
        return new BeforeGame(previous, gameStartsAt);
    }

    @Trace
    @Override
    public Optional<TournamentState> onTransition(ApplicationContext context, Tournament tournament) {
        if (getPrevious() instanceof Inactive) {
            log.info("Ignoring any previous request for tournament pause!");
            tournament.getConfiguration().setPauseRequested(false);
        }

        final TournamentStateMachine stateMachine = context.getBean(TournamentStateMachine.class);
        final TournamentRepository repository = context.getBean(TournamentRepository.class);
        final GamePlay gamePlay = context.getBean(GamePlay.class);
        final Callbacks callbacks = context.getBean(Callbacks.class);
        final TaskScheduler scheduler = context.getBean(TaskScheduler.class);
        scheduler.schedule(() -> {
            final Game.Setup setup = gamePlay.nextGameSetup(tournament);
            repository.withTournament(tournament.getId(), t -> t.addGame(setup));
            stateMachine.toState(tournament.getId(), BeforeTurn.create(tournament, this));
            callbacks.notify(gameStart(tournament.getId()), tournament.getLastGame().get());
            setup
                    .getPlayers()
                    .forEach(p -> callbacks.notify(gameStart(tournament.getId(), p), setup));
        }, Date.from(gameStartsAt));

        return Optional.empty();
    }

}

package com.getbase.hackkrk.tanks.server.model.tournament.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.getbase.hackkrk.tanks.server.TournamentStateMachine;
import com.getbase.hackkrk.tanks.server.model.tournament.Tournament;
import com.newrelic.api.agent.Trace;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = false)
public class BeforeTurn extends AbstractTournamentState {

    private final Instant turnStartsAt;

    @JsonCreator
    BeforeTurn(
            @JsonProperty("previous") TournamentState previous,
            @JsonProperty("turnStartsAt") @NonNull Instant turnStartsAt) {
        super(false, previous, DuringTurn.class);
        this.turnStartsAt = turnStartsAt;
    }

    public static BeforeTurn create(Tournament tournament, TournamentState previous) {
        final Instant turnStartsAt = Instant.now().plus(Duration.ofMillis(tournament.getConfiguration().getMillisBetweenTurns()));
        return new BeforeTurn(previous, turnStartsAt);
    }

    @Trace
    @Override
    public Optional<TournamentState> onTransition(ApplicationContext context, Tournament tournament) {
        final TournamentStateMachine stateMachine = context.getBean(TournamentStateMachine.class);
        final TaskScheduler scheduler = context.getBean(TaskScheduler.class);
        scheduler.schedule(() -> stateMachine.toState(tournament.getId(), DuringTurn.create(tournament, this)), Date.from(turnStartsAt));
        return Optional.empty();
    }

}

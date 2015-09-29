package com.getbase.hackkrk.tanks.server.model.tournament.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.getbase.hackkrk.tanks.server.model.tournament.RankingCalculator;
import com.getbase.hackkrk.tanks.server.model.tournament.Tournament;
import com.newrelic.api.agent.Trace;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

import static java.lang.Boolean.TRUE;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class FinalizingGame extends AbstractTournamentState {

    @JsonCreator
    FinalizingGame(
            @JsonProperty("previous") TournamentState previous) {
        super(true, previous, BeforeGame.class, Inactive.class);
    }

    public static FinalizingGame create(Tournament tournament, TournamentState previous) {
        return new FinalizingGame(previous);
    }

    @Trace
    @Override
    public Optional<TournamentState> onTransition(ApplicationContext context, Tournament tournament) {
        context.getBean(RankingCalculator.class).calculateForLastGameAndUpdate(tournament);
        if (tournament.getConfiguration().getPauseRequested() == TRUE) {
            return Optional.of(Inactive.create(tournament, this));
        }

        return Optional.of(BeforeGame.create(tournament, this));
    }

}

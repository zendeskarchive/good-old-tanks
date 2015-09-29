package com.getbase.hackkrk.tanks.server.model.tournament.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.getbase.hackkrk.tanks.server.model.tournament.Tournament;
import com.newrelic.api.agent.Trace;
import lombok.Getter;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.ArrayUtils.add;

abstract class AbstractTournamentState implements TournamentState {

    @JsonIgnore
    @Getter
    private final boolean backupable;

    @Getter
    private TournamentState previous;

    private final List<Class<? extends TournamentState>> allowedTransitions;

    @SafeVarargs
    AbstractTournamentState(boolean backupable, TournamentState previous, Class<? extends TournamentState>... allowedTransitions) {
        this.backupable = backupable;
        this.allowedTransitions = asList(add(allowedTransitions, Recovering.class));
        if (previous == null) {
            this.previous = null;
        } else {
            this.previous = previous.previousCleared();
        }
    }

    @Override
    public boolean isTransitionAllowed(TournamentState to) {
        return allowedTransitions.contains(to.getClass());
    }

    @Override
    public TournamentState previousCleared() {
        this.previous = null;
        return this;
    }

    @Trace
    @Override
    public Optional<TournamentState> onTransition(ApplicationContext context, Tournament tournament) {
        return Optional.empty();
    }

}

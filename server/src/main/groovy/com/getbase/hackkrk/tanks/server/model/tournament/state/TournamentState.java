package com.getbase.hackkrk.tanks.server.model.tournament.state;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.getbase.hackkrk.tanks.server.model.tournament.Tournament;
import com.newrelic.api.agent.Trace;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Inactive.class, name = "inactive"),
        @JsonSubTypes.Type(value = BeforeGame.class, name = "beforeGame"),
        @JsonSubTypes.Type(value = BeforeTurn.class, name = "beforeTurn"),
        @JsonSubTypes.Type(value = DuringTurn.class, name = "turn"),
        @JsonSubTypes.Type(value = FinalizingGame.class, name = "finalizingGame"),
})
public interface TournamentState {

    boolean isTransitionAllowed(TournamentState to);

    boolean isBackupable();

    TournamentState getPrevious();

    TournamentState previousCleared();

    @Trace
    Optional<TournamentState> onTransition(ApplicationContext context, Tournament tournament);

}

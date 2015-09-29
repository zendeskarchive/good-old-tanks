package com.getbase.hackkrk.tanks.server

import com.getbase.hackkrk.tanks.server.model.tournament.Tournament
import com.getbase.hackkrk.tanks.server.model.tournament.state.Recovering
import com.getbase.hackkrk.tanks.server.model.tournament.state.TournamentState
import com.newrelic.api.agent.Trace
import groovy.util.logging.Slf4j
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

import static com.newrelic.api.agent.NewRelic.setTransactionName

@Component
@Slf4j
class TournamentStateMachine {

    private final ApplicationContext applicationContext

    private final TournamentRepository repository

    @Autowired
    TournamentStateMachine(
            ApplicationContext applicationContext,
            TournamentRepository tournamentRepository) {
        this.applicationContext = applicationContext
        this.repository = tournamentRepository
    }

    Tournament recover(Tournament tournament) {
        log.info "Recovering {} for tournament #{}", tournament.state, tournament.id
        def current = tournament.state
        tournament.state = new Recovering(null)
        toState(tournament.id, current)
    }

    @Trace(dispatcher = true)
    Tournament toState(String tournamentId, TournamentState nextState) {
        try {
            setTransactionName("toState", nextState.class.simpleName);
            MDC.put("tid", tournamentId)

            log.info "Activating {} for tournament #{}", nextState.class.simpleName, tournamentId
            def tournament = repository.withTournament(tournamentId, true) {
                state = nextState
            }
            tournament.state
                    .onTransition(applicationContext, tournament)
                    .ifPresent { toState(tournamentId, it) }
            tournament
        } finally {
            MDC.remove("tid")
        }
    }

}

package com.getbase.hackkrk.tanks.server.model.tournament.state;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Recovering extends AbstractTournamentState {

    public Recovering(TournamentState previous) {
        super(false, previous);
    }

    @Override
    public boolean isTransitionAllowed(TournamentState to) {
        return true;
    }

}

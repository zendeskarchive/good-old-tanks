package com.getbase.hackkrk.tanks.server.model.tournament.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.getbase.hackkrk.tanks.server.model.tournament.Tournament;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Inactive extends AbstractTournamentState {

    @JsonCreator
    Inactive(
            @JsonProperty("previous") TournamentState previous) {
        super(true, previous, BeforeGame.class);
    }

    public static Inactive createInitial() {
        return new Inactive(null);
    }

    public static Inactive create(Tournament tournament, TournamentState previous) {
        return new Inactive(previous);
    }

}

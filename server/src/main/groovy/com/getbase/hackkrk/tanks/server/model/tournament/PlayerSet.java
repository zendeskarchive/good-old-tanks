package com.getbase.hackkrk.tanks.server.model.tournament;

import lombok.Data;

import java.util.List;

@Data
public class PlayerSet {

    private final int round;

    private final List<Player> players;
}

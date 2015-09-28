package com.getbase.hackkrk.tanks.server.simulation.events;

import com.getbase.hackkrk.tanks.server.model.tournament.Player;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;

import lombok.Data;

@Data
public class TankHit implements SimulationEvent {
    private final double timestamp;
    private final Player owner;
    private final Player hitBy;
    private final double newHealth;
    private final Point hitCoordinates;
    private final boolean targetDestroyed;
    @Override
    public void accept(SimulationEventHandler h) {
        h.onTankHit(this);
    }
}

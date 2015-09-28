package com.getbase.hackkrk.tanks.server.simulation.events;

import com.getbase.hackkrk.tanks.server.model.tournament.Player;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;

import lombok.Data;

@Data
public class TankPositionChange implements SimulationEvent {
    private final double timestamp;
    private final Player owner;
    private final Point newPosition;
    @Override
    public void accept(SimulationEventHandler h) {
        h.onTankPositionChange(this);
    }
}

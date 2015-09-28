package com.getbase.hackkrk.tanks.server.simulation.events;

import com.getbase.hackkrk.tanks.server.model.tournament.Player;

import lombok.Data;

@Data
public class TankHealthChange implements SimulationEvent {
    private final double timestamp;
    private final Player owner;
    private final double newHealth;
    
    @Override
    public void accept(SimulationEventHandler h) {
        h.onTankHealthChange(this);
    }
}

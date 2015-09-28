package com.getbase.hackkrk.tanks.server.simulation.events;

import com.getbase.hackkrk.tanks.server.model.tournament.Player;

public interface SimulationEvent {
    double getTimestamp();
    Player getOwner();
    
    void accept(SimulationEventHandler h);
}

package com.getbase.hackkrk.tanks.server.simulation;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.getbase.hackkrk.tanks.server.model.game.Move;
import com.getbase.hackkrk.tanks.server.model.game.Tank;
import com.getbase.hackkrk.tanks.server.model.scene.Physics;
import com.getbase.hackkrk.tanks.server.model.scene.Scene;
import com.getbase.hackkrk.tanks.server.model.tournament.Player;
import com.getbase.hackkrk.tanks.server.simulation.events.BulletPositionChange;
import com.getbase.hackkrk.tanks.server.simulation.events.SimulationEvent;
import com.getbase.hackkrk.tanks.server.simulation.events.TankHealthChange;
import com.getbase.hackkrk.tanks.server.simulation.events.TankHit;
import com.getbase.hackkrk.tanks.server.simulation.events.TankPositionChange;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;
import com.google.common.base.Preconditions;
import com.newrelic.api.agent.Trace;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import mikera.vectorz.Vector2;

public class Simulation {
    private Scene scene;
    private SeriousEnterpriseEventBus<SimulationEvent> events;
    private List<TankState> tanks;
    
    public Simulation(Scene scene, Map<Player, Move> moves, List<Tank> tanks) {
        this.scene = scene;
        this.events = new SeriousEnterpriseEventBus<SimulationEvent>();
        this.tanks = collectTanks(scene, moves, tanks);
    }
    
    @Trace
    public Stream<SimulationEvent> simulate() {
        List<Bullet> bullets = tanks.stream().flatMap(t -> t.spawnBullet()).collect(Collectors.toList());
        
        emitInitialEvents(tanks, bullets, 0);

        double dt = scene.getPhysics().getUnitOfTime();
        double maxTime = scene.getPhysics().getMaxSimulationIterationsPerTurn() * dt;
        for (double time = dt; time < maxTime; time += dt) {
            SimulationStep ss = new SimulationStep(time, dt, events);
            ss.perform(tanks, bullets, scene);
            
            bullets = ss.getRemainingBullets();
            tanks = tanks.stream().filter(tank -> tank.isAlive()).collect(Collectors.toList());

            if (ss.isEnded()) {
                break;
            }
        }

        return events.getAllEvents();
    }
    
    private void emitInitialEvents(List<TankState> tanks, List<Bullet> bullets, int time) {
        for (TankState tank : tanks) {
            events.add(new TankHealthChange(time, tank.tank.getPlayer(), tank.health));
            events.add(new TankPositionChange(time, tank.tank.getPlayer(), new Point(tank.position)));
        }

        for (Bullet b : bullets) {
            events.add(new BulletPositionChange(time, b.owner.tank.getPlayer(), new Point(b.position)));
        }
    }
    
    static class SeriousEnterpriseEventBus<T> {
        private List<T> allEvents = new LinkedList<>();
        private List<Consumer<T>> consumers = new ArrayList<>();
        
        public void add(T event) {
            allEvents.add(event);
            for(Consumer<T> consumer : consumers){
                consumer.accept(event);
            }
        }
        
        public void addConsumer(Consumer<T> consumer){
            consumers.add(consumer);
        }
        
        public Stream<T> getAllEvents(){
            return allEvents.stream();
        }
    }

    private List<TankState> collectTanks(Scene scene, Map<Player, Move> moves, List<Tank> tanks) {
        List<TankState> tankStates = new ArrayList<>();
        Map<Player, Tank> playerTanks = tanks.stream().collect(Collectors.toMap(t -> t.getPlayer(), t -> t));
        
        for(Player player : playerTanks.keySet()){
            Move move = moves.get(player);
            Tank tank = playerTanks.get(player);
            tankStates.add(new TankState(move, tank, scene.getPhysics(), events));
        }
        
        return tankStates;
    }
    
    // for debugging
    public void addEventConsumer(Consumer<SimulationEvent> consumer) {
        events.addConsumer(consumer);
    }
}

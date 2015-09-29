package com.getbase.hackkrk.tanks.server.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.getbase.hackkrk.tanks.server.model.game.Move;
import com.getbase.hackkrk.tanks.server.model.game.Tank;
import com.getbase.hackkrk.tanks.server.model.scene.LandscapeFactory;
import com.getbase.hackkrk.tanks.server.model.scene.Physics;
import com.getbase.hackkrk.tanks.server.model.scene.PhysicsProvider;
import com.getbase.hackkrk.tanks.server.model.scene.Scene;
import com.getbase.hackkrk.tanks.server.model.tournament.Player;
import com.getbase.hackkrk.tanks.server.model.tournament.TournamentConfiguration;
import com.getbase.hackkrk.tanks.server.model.tournament.debugui.UiAdapter;
import com.getbase.hackkrk.tanks.server.simulation.events.SimulationEvent;
import com.getbase.hackkrk.tanks.server.simulation.events.TankPositionChange;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestSimulation {
    private static LongAdder totalEvents = new LongAdder();
    private static double prevTimestamp = -1;
    
    public static void main(String... args) throws InterruptedException {
        Physics p = new PhysicsProvider().provide(TournamentConfiguration.createDefault());
        log.info("physics = {}", p);
        Scene scene = new Scene(new LandscapeFactory().create(true), p);
        log.info("landscape: {}", scene.getLandscape().getPoints());
        TheSetup setup = createPlayersAndMoves(scene);
        
        for(int t = 0; t < 100; t++){
            Simulation sim = new Simulation(scene, setup.moves, setup.tanks);
            UiAdapter.get().onSimulationRun(scene, sim);
            sim.addEventConsumer(x -> applyAnimationDelay(x));
            sim.addEventConsumer(x -> totalEvents.add(1));
            sim.addEventConsumer(e -> log.info(totalEvents.longValue() + " " + e));
            
            Stream<SimulationEvent> events = sim.simulate();
            setup = prepareNewMoves(setup, events);
        }
        log.info("simulation done");
    }

    private static void applyAnimationDelay(SimulationEvent x) {
        if(prevTimestamp != x.getTimestamp()){
            sleep(15);
        }
        prevTimestamp = x.getTimestamp();
    }

    private static TheSetup prepareNewMoves(TheSetup setup, Stream<SimulationEvent> events) {
        List<SimulationEvent> allEvents = events.collect(Collectors.toList());
        
        setup.tanks = setup.tanks.stream().map(t -> {
            TankPositionChange tpc = lastPositionChange(t, allEvents);
            return new Tank(tpc.getOwner(), 1, tpc.getNewPosition());
        }).collect(Collectors.toList());
            
        Random rnd = new  Random();
        
        setup.moves = setup.moves.entrySet().stream().collect(Collectors.toMap(
                e -> e.getKey(), 
                e -> new Move(rnd.nextInt(180)-90, rnd.nextInt(50)+50, rnd.nextInt(300)-150)));
        
        return setup;
    }
    
    private static TankPositionChange lastPositionChange(Tank t, List<SimulationEvent> allEvents){
        for(SimulationEvent e : Lists.reverse(allEvents)){
            if(e instanceof TankPositionChange && ((TankPositionChange)e).getOwner().equals(t.getPlayer())){
                return (TankPositionChange) e;
            }
        }
        return null;
    }

    private static void sleep(long len) {
        try {
            Thread.sleep(len);
        } catch (InterruptedException ie) {
        }
    }

    private static TheSetup createPlayersAndMoves(Scene scene) {
        Tank a = initTank("Alex", "green", 100, scene);
        Tank b = initTank("Boris", "red", -490, scene);
        
//        Move moveA = new Move(a, 45, 90, -100);
        Move moveA = new Move(0, 100, -100);
        Move moveB = new Move(32, 100, -20);
        
        TheSetup s = new TheSetup();
        
        s.tanks.add(a);
        s.tanks.add(b);
        
        s.moves.put(a.getPlayer(), moveA);
        s.moves.put(b.getPlayer(), moveB);
        
        return s;
    }
    
    static class TheSetup {
        Map<Player,Move> moves = new HashMap<>();;
        List<Tank> tanks = new ArrayList<>();
    }

    private static Tank initTank(String playerName, String color, double x, Scene scene) {
        double y = scene.getLandscape().findHeight(x);
        int hp = 1;
        Tank a = new Tank(new Player(playerName, color), hp, new Point(x, y));
        return a;
    }

    
}

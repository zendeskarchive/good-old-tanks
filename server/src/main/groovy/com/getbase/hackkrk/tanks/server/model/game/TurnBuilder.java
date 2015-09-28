package com.getbase.hackkrk.tanks.server.model.game;

import com.getbase.hackkrk.tanks.server.model.game.MoveOutcome.MoveOutcomeBuilder;
import com.getbase.hackkrk.tanks.server.model.game.MoveOutcome.Type;
import com.getbase.hackkrk.tanks.server.model.tournament.Player;
import com.getbase.hackkrk.tanks.server.model.tournament.Tournament;
import com.getbase.hackkrk.tanks.server.simulation.events.*;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts list of events returned from simulation into a convenient Turn
 */
@Slf4j
public class TurnBuilder {
    private Tournament tournament;
    private List<RequestedMove> requestedMoves;
    private List<SimulationEvent> events;

    public TurnBuilder(Tournament tournament, List<RequestedMove> requestedMoves, List<SimulationEvent> events) {
        this.tournament = tournament;
        this.requestedMoves = requestedMoves;
        this.events = events;
    }

    public Turn build(){
        long start = System.currentTimeMillis();

        Map<Player, Tank> tankByPlayer = collectTanks(events);
        ImmutableList<Tank> tanks = ImmutableList.copyOf(tankByPlayer.values());

        Map<Player, MoveOutcomeBuilder> outcomeBuilders = collectMoveOutcomes(events, tankByPlayer);
        appendTrajectories(outcomeBuilders, events);
        appendTankMovement(outcomeBuilders, events);

        Stream<MoveOutcome> moveOutcomesStream = outcomeBuilders.values().stream().map(b -> b.build());
        List<MoveOutcome> collectedOutcomes = moveOutcomesStream.collect(Collectors.toList());
        ImmutableList<MoveOutcome> outcome = ImmutableList.copyOf(collectedOutcomes);

        log.debug("Turn creation took: {}ms", System.currentTimeMillis()-start);

        return new Turn(tournament.getLastGame().get().getTurns().size(), requestedMoves, tanks, outcome, isLast(tanks));
    }

    private static class MoveOutcomeCollector implements SimulationEventHandler {
        private final Map<Player, Tank> tankByPlayer;
        private final Map<Player, MoveOutcomeBuilder> tmpOutcomes = new HashMap<>();

        public MoveOutcomeCollector(Map<Player, Tank> tankByPlayer){
            this.tankByPlayer = tankByPlayer;
        }

        // handling tank position change is needed only to make sure that we have entries for all tanks
        @Override
        public void onTankPositionChange(TankPositionChange h) {
            if (!tmpOutcomes.containsKey(h.getOwner())) {
                MoveOutcomeBuilder builder = MoveOutcome.builder()
                        .player(h.getOwner())
                        .type(Type.miss)
                        .targetDestroyed(false);
                tmpOutcomes.put(h.getOwner(), builder);
            }
        }

        @Override
        public void onTankHit(TankHit h){
            MoveOutcomeBuilder builder = MoveOutcome.builder()
                .player(h.getHitBy())
                .target(tankByPlayer.get(h.getOwner()))
                .hitCoordinates(h.getHitCoordinates())
                .type(Type.tank_hit)
                .targetDestroyed(h.isTargetDestroyed());
            tmpOutcomes.put(h.getHitBy(), builder);
        }

        @Override
        public void onBulletGroundHit(BulletGroundHit h){
            MoveOutcomeBuilder builder = MoveOutcome.builder()
                .player(h.getOwner())
                .hitCoordinates(h.getHitCoordinates())
                .type(Type.ground_hit);
            tmpOutcomes.put(h.getOwner(), builder);
        }

        @Override
        public void onBulletPositionChange(BulletPositionChange h){
            MoveOutcomeBuilder builder = MoveOutcome.builder()
                .player(h.getOwner())
                .type(Type.miss);
            if (!tmpOutcomes.containsKey(h.getOwner())) {
                tmpOutcomes.put(h.getOwner(), builder);
            }
        }

        private Map<Player, MoveOutcomeBuilder> collect(){
            return tmpOutcomes;
        }
    }

    private static Map<Player, MoveOutcomeBuilder> collectMoveOutcomes(List<SimulationEvent> events, Map<Player, Tank> tankByPlayer) {
        MoveOutcomeCollector c = new MoveOutcomeCollector(tankByPlayer);
        events.forEach(e -> e.accept(c));
        return c.collect();
    }

    private static class TankCollector implements SimulationEventHandler {
        Map<Player, TankStats> tankStats = new HashMap<>();

        @Override
        public void onTankPositionChange(TankPositionChange e) {
            TankStats ts = tankStats.getOrDefault(e.getOwner(), new TankStats());
            ts.setPosition(e.getNewPosition());
            tankStats.put(e.getOwner(), ts);
        }

        @Override
        public void onTankHealthChange(TankHealthChange e) {
            TankStats ts = tankStats.getOrDefault(e.getOwner(), new TankStats());
            ts.setHp(e.getNewHealth());
            tankStats.put(e.getOwner(), ts);
        }
        private Map<Player, Tank> collect(){
            return tankStats.entrySet().stream()
                    .map(e -> new Tank(e.getKey(), (int) Math.ceil(e.getValue().hp), e.getValue().position))
                    .collect(Collectors.toMap(t -> t.getPlayer(), t -> t));
        }
    }

    private Map<Player, Tank> collectTanks(List<SimulationEvent> events) {
        TankCollector c = new TankCollector();
        events.forEach(e -> e.accept(c));
        return c.collect();
    }

    public boolean isLast(List<Tank> tanks) {
        final long aliveTanks = tanks.stream().filter(Tank::isAlive).count();
        final int currentTurnNumber = tournament.getCurrentGame().get().getTurns().size() + 1;
        return aliveTanks <= 1 || currentTurnNumber >= tournament.getConfiguration().getMaxTurns();

    }

    private static void appendTrajectories(Map<Player, MoveOutcomeBuilder> outcomeBuilders, List<SimulationEvent> events) {
        Map<Player, ImmutableList<Point>> trajectories = collectTrajectory(events, BulletPositionChange.class, e -> e.getNewPosition());

        for(Player player : trajectories.keySet()){
            outcomeBuilders.get(player).bulletTrajectory(trajectories.get(player));
        }
    }

    private static void appendTankMovement(Map<Player, MoveOutcomeBuilder> outcomeBuilders, List<SimulationEvent> events) {
        Map<Player, ImmutableList<Point>> trajectories = collectTrajectory(events, TankPositionChange.class, e -> e.getNewPosition());

        for(Player player : trajectories.keySet()){
            outcomeBuilders.get(player).tankMovement(trajectories.get(player));
        }
    }

    private static <T extends SimulationEvent> Map<Player, ImmutableList<Point>> collectTrajectory(List<SimulationEvent> events, Class<T> eventType, Function<T, Point> positionProvider){
        // linkedHashMultimap because order is critical
        Multimap<Player, Point> points = LinkedHashMultimap.create();
        for(SimulationEvent e : events){
            if(eventType.isAssignableFrom(e.getClass())){
                @SuppressWarnings("unchecked")
                T event = (T) e;
                points.put(event.getOwner(), positionProvider.apply(event));
            }
        }
        Map<Player, ImmutableList<Point>> result = new HashMap<>();
        for(Player player : points.keySet()){
            Collection<Point> trajectory = points.get(player);
            result.put(player, ImmutableList.copyOf(trajectory));
        }
        return result;
    }

    @Data
    private static class TankStats {
        double hp;
        Point position;
    }
}

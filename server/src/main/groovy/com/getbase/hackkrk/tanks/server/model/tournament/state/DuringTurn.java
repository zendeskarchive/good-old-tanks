package com.getbase.hackkrk.tanks.server.model.tournament.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.getbase.hackkrk.tanks.server.Config;
import com.getbase.hackkrk.tanks.server.TournamentRepository;
import com.getbase.hackkrk.tanks.server.callback.Callbacks;
import com.getbase.hackkrk.tanks.server.model.game.*;
import com.getbase.hackkrk.tanks.server.model.scene.Scene;
import com.getbase.hackkrk.tanks.server.model.tournament.Player;
import com.getbase.hackkrk.tanks.server.model.tournament.Tournament;
import com.getbase.hackkrk.tanks.server.model.tournament.debugui.UiAdapter;
import com.getbase.hackkrk.tanks.server.simulation.Simulation;
import com.getbase.hackkrk.tanks.server.simulation.events.BulletPositionChange;
import com.getbase.hackkrk.tanks.server.simulation.events.SimulationEvent;
import com.getbase.hackkrk.tanks.server.simulation.events.TankPositionChange;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.newrelic.api.agent.Trace;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.getbase.hackkrk.tanks.server.callback.Events.turnFinished;
import static java.util.stream.Collectors.toList;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = false)
public class DuringTurn extends AbstractTournamentState {

    @JsonCreator
    DuringTurn(
            @JsonProperty("previous") TournamentState previous) {
        super(false, previous, BeforeTurn.class, FinalizingGame.class);
    }

    public static DuringTurn create(Tournament tournament, TournamentState previous) {
        return new DuringTurn(previous);
    }

    @Trace
    @Override
    public Optional<TournamentState> onTransition(ApplicationContext context, Tournament tournament) {
        final TournamentRepository repository = context.getBean(TournamentRepository.class);
        final Callbacks callbacks = context.getBean(Callbacks.class);


        // FIXME there might be no current game is we recover!
        Game game = tournament.getCurrentGame().get();
        List<Player> players = game.getSetup().getPlayers();

        Optional<Turn> lastTurn = game.getLastTurn();

        // FIXME init tanks in a smart way...
        List<Tank> tanks = getOrCreateTanks(players, game, lastTurn);

        Map<Player, Move> moves = tournament.getSubmittedMoves();

        Scene scene = game.getSetup().getScene();
        Simulation sim = new Simulation(scene, moves, tanks);

        if(showDebugUi(context)){
            UiAdapter.get().onSimulationRun(scene, sim);
        }

        Stream<SimulationEvent> simulationResult = sim.simulate();

        List<SimulationEvent> result = simulationResult.collect(toList());
        logEvents(result);
        List<RequestedMove> requestedMoves = moves.entrySet().stream().map(e -> new RequestedMove(e.getKey(), e.getValue())).collect(toList());

        Turn turn = new TurnBuilder(tournament, requestedMoves, result).build();

        repository.withTournament(tournament.getId(), t -> t.addTurn(turn));
        callbacks.notify(turnFinished(tournament.getId()), turn);
        callbacks.notify(turnFinished(tournament.getId(), game.getNumber(), turn.getNumber()), turn);

        if(game.isConcluded()){
            return Optional.of(FinalizingGame.create(tournament, this));
        }else{
            return Optional.of(BeforeTurn.create(tournament, this));
        }
    }

    private boolean showDebugUi(ApplicationContext context) {
        return context.getBean(Config.class).isShowDebugUi();
    }

    private void logEvents(List<SimulationEvent> events) {
        log.info("{} events generated", events.size());
        int i = 0;
        for (SimulationEvent e : events) {
            if (e instanceof TankPositionChange || e instanceof BulletPositionChange) {
                log.trace("{} {}", i, e);
            } else {
                log.info("{} {}", i, e);
            }
            i++;
        }
    }

    private List<Tank> getOrCreateTanks(List<Player> players, Game game, Optional<Turn> lastTurn) {
        if(lastTurn.isPresent()){
            return lastTurn.get().getTanks().stream().filter(Tank::isAlive).collect(toList());
        }else{
            Builder<Tank> b = ImmutableList.builder();
            for(Player player : players){
                int hp = game.getSetup().getScene().getPhysics().getInitialTankHealth();
                Point p = game.getSetup().getInitialPositions().get(player.getName());
                b.add(new Tank(player, hp, p));
            }
            return b.build();
        }
    }
}

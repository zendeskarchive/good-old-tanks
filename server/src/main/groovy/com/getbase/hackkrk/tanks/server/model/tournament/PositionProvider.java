package com.getbase.hackkrk.tanks.server.model.tournament;

import static java.util.Collections.shuffle;

import java.util.*;

import com.getbase.hackkrk.tanks.server.simulation.utils.Point;
import org.springframework.stereotype.Component;

import com.getbase.hackkrk.tanks.server.model.scene.Landscape;
import com.google.common.base.Preconditions;

import lombok.Data;

@Component
@Data
public class PositionProvider {
    private final Random random = new Random();

    public Map<String, Point> provide(Landscape landscape, List<Player> players) {
        Preconditions.checkArgument(landscape.getWidth() > 0);

        final List<Player> tmpPlayers = new ArrayList<>(players);
        shuffle(tmpPlayers);

        int count = players.size();
        double minX = landscape.getMinX();
        double step = landscape.getWidth() / (double) (count + 1);
        double maxDeviation = step / 3.0;

        final Map<String, Point> initialPositions = new HashMap<>();
        for (int i = 0; i < players.size(); i++) {
            final double x = minX + (double) (i + 1) * step + deviation((int) maxDeviation);
            final double y = landscape.findHeight(x);
            initialPositions.put(players.get(i).getName(), new Point(x, y));
        }
        return initialPositions;
    }

    private double deviation(int maxDeviation) {
        return random.nextInt(maxDeviation * 2) - maxDeviation;
    }
}

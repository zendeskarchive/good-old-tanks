package com.getbase.hackkrk.tanks.server.model.scene;

import com.getbase.hackkrk.tanks.server.model.scene.random.PerlinNoiseGenerator;
import com.getbase.hackkrk.tanks.server.simulation.utils.Point;
import com.google.common.base.Preconditions;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

@Component
@Data
public class LandscapeFactory {

    @Value("${landscape.miny:50}")
    private int minY = 50;

    @Value("${landscape.maxy:480}")
    private int maxY = 480;

    @Value("${landscape.minx:-500}")
    private int minX = -500;

    @Value("${landscape.maxx:500}")
    private int maxX = 500;

    @Value("${landscape.points:400}")
    private int numberOfPoints = 400;

    @Value("${landscape.defaultDifficulty:40}")
    private int defaultDifficulty = 40;

    public Landscape create(boolean equalEdgeHeight, int difficulty) {
        final List<Double> ys = ys(minY, maxY, numberOfPoints, Difficulty.of(difficulty), equalEdgeHeight);

        final double width = maxX - minX;
        double xStep = width / (double) (numberOfPoints - 1);

        final List<Point> points = range(0, numberOfPoints)
                .boxed()
                .map(i -> new Point((double) i * xStep - width / 2.0, ys.get(i)))
                .collect(toList());

        return new Landscape(points);
    }

    public Landscape create(boolean equalEdgeHeight) {
        return create(equalEdgeHeight, defaultDifficulty);
    }

    private List<Double> adjustEdgeHeight(List<Double> ys) {
        List<Double> result = new ArrayList<>();
        Double left = ys.get(0);
        Double right = ys.get(ys.size() - 1);

        double heightDifference = left - right;
        double delta = heightDifference / (ys.size() - 1);

        for (int idx = 0; idx < ys.size(); idx++) {
            result.add(adjust(idx, ys, delta));
        }
        return result;
    }

    private Double adjust(int idx, List<Double> ys, double delta) {
        Double y = ys.get(idx);
        Double first = ys.get(0);
        if (idx == 0) {
            return y;
        } else if (idx == ys.size() - 1) {
            return first;
        } else {
            return y + delta * idx;
        }
    }

    protected List<Double> ys(double windowMin, double windowMax, int numberOfPoints, Difficulty difficulty,
                              boolean equalEdgeHeight) {
        List<Double> noise = noise(numberOfPoints, difficulty);

        if (equalEdgeHeight) {
            noise = adjustEdgeHeight(noise);
        }
        final List<Double> scaled = scale(windowMin, windowMax, difficulty, noise);
        final double scaledMin = scaled.stream().mapToDouble(Double::new).min().getAsDouble();
        return scaled.stream().map(d -> d + (windowMin - scaledMin)).collect(toList());
    }

    protected List<Double> scale(double windowMin, double windowMax,Difficulty difficulty, List<Double> noise) {
        final double noiseMin = noise.stream().mapToDouble(Double::new).min().getAsDouble();
        final double noiseMax = noise.stream().mapToDouble(Double::new).max().getAsDouble();
        final double noiseSpan = noiseMax - noiseMin;
        final double scaleTo1 = 1 / noiseSpan;
        final double scaleToWindow = (windowMax - windowMin);
        final double scale = scaleTo1 * difficulty.scale * scaleToWindow;
        return noise.stream().map(d -> d * scale).collect(toList());
    }

    /**
     * noise values in range of [-1, 1]
     */
    protected List<Double> noise(int numberOfPoints, Difficulty difficulty) {
//        protected List<Double> noise(int numberOfPoints, Difficulty difficulty, Function<Double, Double> transformer) {
        final PerlinNoiseGenerator generator = new PerlinNoiseGenerator(new Random());
        int bound = numberOfPoints/2;
        return range(-bound, bound)
                .boxed()
                .map(i -> (double) i / (double) bound)
                .map(x -> generator.noise(x, difficulty.octaves, difficulty.frequency, difficulty.amplitude, true))
                .collect(toList());
    }

    static class Difficulty {
        final double scale;
        final int octaves;
        final double frequency;
        final double amplitude;

        private Difficulty(double scale, int octaves, double frequency, double amplitude) {
            this.scale = scale;
            this.octaves = octaves;
            this.frequency = frequency;
            this.amplitude = amplitude;
        }

        /**
         * @param difficulty [1,100]
         */
        static Difficulty of(int difficulty) {
            Preconditions.checkArgument(difficulty > 0 && difficulty <= 100);
            final double scale = (double) difficulty/100.0;
            final int octaves = 2; //constant
            final double frequency = difficulty/10;
            final double amplitude = 2; //constant
            return new Difficulty(scale, octaves, frequency, amplitude);
        }
    }
}

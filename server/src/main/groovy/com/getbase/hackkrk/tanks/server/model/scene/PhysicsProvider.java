package com.getbase.hackkrk.tanks.server.model.scene;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.getbase.hackkrk.tanks.server.model.tournament.TournamentConfiguration;
import jersey.repackaged.com.google.common.base.Throwables;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PhysicsProvider {
    private ObjectMapper mapper = new ObjectMapper();
    /**
     * Generates new physics parameters. Difficulty 0 returns always the predefined ones (no wind, gravity -9, drag 0.002)
     * @param difficulty
     *            between 0 and 100
     * @param physics2 
     */
    public Physics provide(TournamentConfiguration configuration) {
        Physics physics = clonePhysics(configuration.getPhysicsTemplate());
        
        int difficulty = configuration.getPhysicsDifficulty();
        physics.setWind(wind(difficulty, configuration));
        physics.setGravity(gravity(difficulty, configuration));
        physics.setAirDragCoefficient(airDrag(difficulty, configuration));
        return physics;
    }

    private double wind(int difficulty, TournamentConfiguration configuration) {
        if (difficulty == 0) {
            return 0;
        }
        double mean = configuration.getPhysicsWindMean();

        double stdev = configuration.getPhysicsWindStdev() + difficulty / 100.0 * 4;

        return random(mean, stdev);
    }

    private double gravity(int difficulty, TournamentConfiguration configuration) {
        double mean = configuration.getPhysicsGravityMean();
        if (difficulty <= 2) {
            return mean;
        }
        double stdev = configuration.getPhysicsGravityStdev() + difficulty / 100.0 * 3;

        double result = random(mean, stdev);

        if (result > 0) {
            return 0.1;
        } else {
            return result;
        }
    }

    private double airDrag(int difficulty, TournamentConfiguration configuration) {
        if (difficulty <= 50) {
            return configuration.getPhysicsAirDragBase();
        } else {
            double spread = configuration.getPhysicsAirDragStdevMultiplier() * (difficulty) / 400;
            return random(configuration.getPhysicsAirDragMean(), spread);
        }
    }

    private double random(double mean, double stdev) {
        return new NormalDistribution(mean, stdev).sample();
    }
    
    private Physics clonePhysics(Physics physics) {
        try {
            byte[] bytes = mapper.writeValueAsBytes(physics);
            return mapper.readValue(bytes, Physics.class);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}

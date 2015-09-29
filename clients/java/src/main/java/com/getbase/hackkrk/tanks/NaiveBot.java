package com.getbase.hackkrk.tanks;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.getbase.hackkrk.tanks.api.Command;
import com.getbase.hackkrk.tanks.api.GameSetup;
import com.getbase.hackkrk.tanks.api.TanksClient;
import com.getbase.hackkrk.tanks.api.TurnResult;

public class NaiveBot {
    private static final Logger log = LoggerFactory.getLogger(NaiveBot.class);
    private Random rand = new Random();

    public static void main(String... args) throws Exception {
        new NaiveBot().run();
    }

    public void run() throws Exception {
        TanksClient client = new TanksClient("http://localhost:9999", "main", "DisinterestedCrimsonGazelleDuck");

        while (true) {
            log.info("Waiting for the next game...");
            GameSetup gameSetup = client.getMyGameSetup();
            log.info("Playing {}", gameSetup);

            playGame(client);
        }
    }

    private void playGame(TanksClient client) {
        boolean gameFinished = false;
        while (!gameFinished) {
            TurnResult result = client.submitMove(generateCommand());

            gameFinished = result.last;
        }
    }

    public Command generateCommand() {
        if (rand.nextDouble() > 0.2) {
            return Command.fire(rand.nextInt(90) - 45, rand.nextInt(100) + 30);
        } else {
            return Command.move(rand.nextDouble() > 0.5 ? -100 : 100);
        }
    }
}

package com.getbase.hackkrk.tanks.script

//t = Tournament.create('master')
t = new Tournament(id: 'master', adminToken: 'token', host: 'localhost')

t.with {
    //training - 1h
    at '18:14' set landscapeDifficulty: 1, pointsForHp: 1, pointsForSuccessfulShot: 20, maxPlayersPerGame: 10,
            maxTurns: 100, millisBetweenGames: 5_000, millisBetweenTurns: 1_000

    at '18:15' start()

    at '18:30' set landscapeDifficulty: 10

    at '18:45' set landscapeDifficulty: 20

    //after training - before food
    at '19:00' set landscapeDifficulty: 30, pointsForHp: 5, pointsForSuccessfulShot: 100, maxPlayersPerGame: 6, maxTurns: 100

    at '19:30' set landscapeDifficulty: 40

    //food
    at '20:00' set pointsForHp: 0, pointsForSuccessfulShot: 0

    //after food - main game
    at '21:00' set landscapeDifficulty: 50, pointsForHp: 50, pointsForSuccessfulShot: 1000, maxTurns: 50

    at '21:30' set landscapeDifficulty: 60

    //at '21:45' set physicsDifficulty: 10

    //end of qualification phase
    at '22:15' set pointsForHp: 0, pointsForSuccessfulShot: 0
}

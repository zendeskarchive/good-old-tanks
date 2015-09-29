package com.getbase.hackkrk.tanks.script

t = new Tournament(id: 'master', adminToken: 'token', host: 'localhost')

t.with {
    at '22:19' set landscapeDifficulty: 30, pointsForHp: 50, pointsForSuccessfulShot: 1000, maxPlayersPerGame: 4,
            maxTurns: 100, millisBetweenGames: 5_000, millisBetweenTurns: 1_000

    at '22:20' start()

    at '22:22' set landscapeDifficulty: 40

    at '22:26' set landscapeDifficulty: 50

    at '22:28' set landscapeDifficulty: 60

    at '22:30' set landscapeDifficulty: 70
}


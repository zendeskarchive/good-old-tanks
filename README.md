Welcome to the HackKrk!

Your mission today it to take control of a real tank! You will compete with others to achieve the glorious victory.

# Important Links

- the main tournament where you compete against others:
    - server: http://10.12.202.141:9999/ (swagger api documentation)
    - UI: http://10.12.202.140:8080/launcher.html
    - tournamentId "master"
- sandbox where you can freely test your bots:
    - server: http://10.12.202.144:9999/
    - UI: http://10.12.202.144:8080/launcher.html
    - tournamentId "sandbox-1" through "sandbox-5" - look at the back of the note you've received for the proper number
- sample bots:
	- [java](java/)
	- [ruby](ruby/)

# The game
When a game starts your tank will be placed with others on a randomly generated map. From now on every few seconds a new turn happens during which tanks perform their actions. 
You can submit your move/command every turn. The valid orders are:
- move N units
or
- fire with power A under the angle B
(you cannot request your tank to do both during the same turn)

Tanks fire bullets which follow laws of physics and travel along (more or less) ballistic curves. If a tank gets hit it looses 50 health. Tanks start with 100 health points.
After reaching 0 hp the tank is destroyed.

The game lasts until there is only 1 tank remaining or 200 turns has passed. Then after a short pause a new game starts.

## The tournament

Tournament is made of succeeding games. For every game, players are randomely choosen out of the players that didn't play recently. If all players played a game, then members of next game will be selected from entire pool, and so on.

## Score

Every player that survives a game will get a score for their remaining health points. Additionaly, every successful shot will be also scored (even though player might have been destroyed)

Scoring Formula for a game: `[remaining health points] * HP_SCORE + [successful shots] * SHOT_SCORE`

Scoring points will change throughout the game. We will start with HP_SCORE = 1 and SHOT_SCORE = 20. After some number of games these scores will be increased. It means that first games will be mostly for training. Later games will be highly scored and will select winners.

Finally, 4 players with highest scores will be invited to the final tournament and fight till the winner is selected.

## Difficulty

First games will start wiht flat terrains so that you can learn in easy environment. Throughout the tournament, landscapes will become more challenging - having more hills and being more steep.

# How do I play?

The gameplay is simulated on a server with which you can communicate via REST API. See "Important Links".

### Register your team's player
In order to start you need to give your bot a name. To do that call http://10.12.202.141:9999/#!/tournament-controller/addPlayer
- tournamentId will be announced by the organizers
- Authorization or userToken is your private api token. If you haven't received one talk to the organizers. Do not share it with other teams.
- playerName - name for your bot

E.g. in order to register player named "Johnny" for tournament "main" use:

```
mabn@hackkrk$ curl -w"\n" 'http://10.12.202.141:9999/tournaments/master/players/Johnny' -X POST -H 'Content-Type: application/json' -H 'Authorization: Secret'
{
  "name" : "Johnny",
  "color" : "#ff0000"
}
mabn@hackkrk$
```

### How to find out if a game is starting?

After you've registered your player name games including Your tank will start from time to time. The best way to know when a game is starting is to try to fetch its setup:

```
curl -w"\n" 'http://10.12.202.141:9999/tournaments/master/games/my/setup' -H 'Authorization: Secret' -H 'Content-Type: application/json'
{
  "name" : "main game #1",
  "scene" : {
    "landscape" : {
      "points" : [ {"x" : -500.0,"y" : 159.08951762171824}, ... (an array of points)
    },
  "players" : [ {
    "name" : "Alex",
    "color" : "0xff0000"
  }, {
    "name" : "Franz",
    "color" : "0xff0000"
  }, {
    "name" : "Boris",
    "color" : "0xff0000"
  } ],
  "round" : 2,
  "initialPositions" : {
    "Alex" : {
      "x" : -292.0,
      "y" : 76.69588891456416
    },
    "Boris" : {
      "x" : 320.0,
      "y" : 190.7577320003437
    },
    "Franz" : {
      "x" : -74.0,
      "y" : 81.88844364594792
    }
  }
```
This call is blocking and will block until your game starts. If it times out - no worries, just retry. It will give you information about:
- terrain shape
- list of participating players
- initial tank positions

Note: *player name is a unique identifier for* players and tanks. You can treat it as tank ID.

### How to submit a move?

You can submit a move at any time during the game (preferably every turn). The call will block until the next turn is processed and will return the turn outcome.

```
mabn@hackkrk$ curl 'http://10.12.202.141:9999/tournaments/master/moves' -H 'Content-Type: application/json' -H 'Authorization: MagnanimousHoneyDewWombatPony' --data-binary $'{\n  "shotAngle": 0,\n  "shotPower": 0,\n  "moveDistance": 10\n}'
{
  "number" : 94,                <- turn number
  "requestedMoves" : [ {
    "name" : "Alex",            <- the move sent by Alex
    "color" : "0xff0000",
    "shotAngle" : -27.0, 
    "shotPower" : 98.0,
    "moveDistance" : 0.0
    }, ... ]
  "tanks" : [ {
    "name" : "Alex",            <- details of Alex's tank, after the turn
    "color" : "0xff0000",
    "hp" : 100,                 <- remaining health
    "position" : {              <- tank position after the turn
      "x" : 500.0,
      "y" : 153.3877940324485
    },
    "alive" : true              <- did it survive?
  }, ... ]
  "outcome" : [ {
    "name" : "Alex",            <- this is the outcome of Alex's move
    "color" : "0xff0000",
    "type" : "ground_hit",      <- Alex managed to hit the ground (other possibilities: miss, tank_hit)
    "hitCoordinates" : {        <- exact hit coordinates
      "x" : -617.0816193514617,
      "y" : -5.886205017684311
    },
    "target" : null,            <- a target tank if a tank was hit (name, color, hp, position, alive)
    "targetDestroyed" : false,  <- if tank was hit was it destroyed?
    "bulletTrajectory" : [ {"x" : -500.0,"y" : 159.08951762171824}, ... (an array of points) ], <- bullet trajectory
    "tankMovement" : [ {"x" : -211.0, "y" : 53.24394436116528}, ... (an array of points) ]      <- tank position changes
  }, ...]
  "last" : false                <- is it the last turn?
}
```

As you can see you can learn quite a lot about what has happened from the response.

You should be sending moves in a loop:
- send a move
- call blocks until the turn ends
- you get the result
- do some magic, calculate stuff, decide what to do
- send a move

You can do that until the response contains `last: true` which means it was the last turn. You are expected to go back to waiting for the next game at this point (using `tournaments/master/games/my/setup` endpoint)

#### What is shotAngle, shotPower and moveDistance?

The values you're submitting require some explanation.

1. *shotAngle* - this is the angle between "up" and firing vector, in degrees. Positive values mean firing towards the increasing horizontal coordinates. This image should help with understanding: 

![Alt text](https://monosnap.com/file/2sp7SV7mM9qrLhVlouXCJaziexUAar.png)

2. *shotPower* - this is rather intuitive. Should be in the range 1 to 100. Usually higher power = bullet travels further.
3. *moveDistance* - in order to make it easy to calculate desired value this is *horizontal* distance (not along the terrain). If you are at (150, 200) and want to move to (200, 500) then in fact you want to move by +50 because that's the difference in horizontal coordinates.

# FAQ

#### Can a tank destroy itself?
No, there is no friendly fire. You are free to fire directly upwards and survive the returning bullet.

#### Where do the bullets start?
Bullets are fired from the top part of the tank (slightly above the ground)

#### Are there collisions between tanks? 
No, you can safely move through other tanks, but it's a bit dangerous - when you're close it's easy to fire at you!

#### What if I reach the end of the map? 
Well, that's the end of the map - your tank cannot move further. But bullets are simulated outside of the map, so with some heavy wind they might leave the map for a while and return.

#### What are the map dimensions? 
Horizontally from -500 to 500. Vertically from 0 (the terrain starts around 50) up to around 500. There is no physical upper limit, but the display shows only the part near the ground. High shots will return!

#### What's the tank size?
The diameter is around 30

#### What exactly is the position of a tank?
It's the position of it's central point. It's always on the ground (as described by landscape points)

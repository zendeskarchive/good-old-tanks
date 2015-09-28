config      = require './config.coffee'
Boot        = require './boot.coffee'
Game        = require './game.coffee'
Phaser      = require 'Phaser'
Tournament  = require './tournament.coffee'

game = new Phaser.Game config.width, config.height, Phaser.CANVAS, 'game-stage'

game.state.add 'Boot', Boot
game.state.add 'Tournament', Tournament
game.state.add 'Game', Game

game.state.start 'Boot', true, true, 'test'

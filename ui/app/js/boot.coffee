Phaser = require 'Phaser'
config = require './config.coffee'

class Boot extends Phaser.State
  constructor: -> super

  preload: ->
    @load.pack "good-old-tanks", config.pack

  create: ->
    @game.renderer.renderSession.roundPixels = true
    @game.world.setBounds 0, 0, config.width, config.height
    @game.stage.disableVisibilityChange=true
    @stage.backgroundColor = '#ddd'
    @state.start 'Tournament'

module.exports = Boot

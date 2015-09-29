Phaser = require 'Phaser'
config = require './config.coffee'
$ = require 'jquery'

class Game extends Phaser.State

  constructor: ->
    super
    @turnInProgress = false
    @turnIndex = 0
    @tournamentId = this.getQueryParam('tournamentId')

  init: (gameData) =>
    @gameId = gameData.number
    @gameData = gameData
    @background = @add.sprite 0, 0, 'bg_landscape'
    @landscapeGraphics = @add.graphics 0, 0
    setup = gameData.setup
    @tanks = (new Tank(this, this.swapCoords(setup.initialPositions[player.name]).x, this.swapCoords(setup.initialPositions[player.name]).y, 100, player) for player in gameData.setup.players)
    @shootSound = @add.audio('aagun')
    @destroySound = @add.audio('grenade')
    @gameEnd = @add.audio('game_end')
    @gameOverLabel = @add.text 240, 100, "GAME\nOVER" , { font: "128px 'Press Start 2P'", fill: '#ee0000' }
    @gameOverLabel.setShadow 1, 1, 'rgba(0, 0, 0, 0.8)', 1
    @gameOverLabel.alpha = 0

    @show = false
    @time.desiredFps = 100
    if this.getQueryParam("fps")!=""
      @time.desiredFps=this.getQueryParam("fps")
    if !this.getQueryParam("dev")
      this.loadRemainingTurns()
    if this.getQueryParam("tail")!=""
      console.log("tailing...")
      @turnIndex = @lastTurnIndex()
      lastTurn = @gameData.turns[@turnIndex]
      if lastTurn
        names = (tank.name for tank in lastTurn.tanks)
        tank.destroy() for tank in @tanks when tank.player.name not in names
      else
        this.reloadWholeGame()

    if this.getQueryParam("from") != ""
      @turnIndex = this.getQueryParam("from")
      if @turnIndex > @lastTurnIndex()
        @turnIndex = @lastTurnIndex()
      lastTurn = @gameData.turns[@turnIndex]
      if lastTurn
        names = (tank.name for tank in lastTurn.tanks)
        tank.destroy() for tank in @tanks when tank.player.name not in names
    if this.getQueryParam("singleTurnTime")!=""
      config.singleTurnTime=this.getQueryParam("singleTurnTime")

  loadRemainingTurns: =>
    turnIndex = this.lastTurnIndex()
    $.ajax({ url: config.gameServerUrl + @tournamentId + "/games/#{@gameId}/turns/#{turnIndex}" })
      .done(this.setGameData)
      .error(() =>
        console.error('ERROR loading turns, retrying...')
        if !this.loadingFinished()
          @time.events.add(Phaser.Timer.SECOND * 3, this.loadRemainingTurns, this)
      )

  setGameData: (data) =>
    @gameData.turns.push(data)
    if !this.loadingFinished()
      this.loadRemainingTurns()

  loadingFinished: =>
    @gameData.turns.length >= 200 || (t for t in @gameData.turns[this.lastTurnIndex()].tanks when t.alive).length <= 1 || @gameData.turns[this.lastTurnIndex()].last

  lastTurnIndex: =>
    index = @gameData.turns.length - 1
    if index > 0 then index else 0

  create: =>
    @gameLabel = @add.text 0, 0, @gameData.setup.name, { font: "18px 'Press Start 2P'", fill: "#ffffff" }
    @gameLabel.setShadow 1, 1, 'rgba(0, 0, 0, 0.8)', 1
    @trajectoryGraphics = @add.graphics 0, 0
    @drawLandscape(@gameData.setup.scene.landscape)

  drawLandscape: (landscape) =>
    @landscapeGraphics.beginFill 0xade30a
    @landscapeGraphics.moveTo config.width, config.height
    @landscapeGraphics.lineTo 0, config.height
    points = (this.swapCoords(point) for point in landscape.points)
    for point in points
      @landscapeGraphics.lineTo(point.x, point.y)
    lastPoint = points[-1..][0]
    @landscapeGraphics.lineTo(lastPoint.x, lastPoint.y)
    @landscapeGraphics.endFill()

    @landscapeGraphics.lineStyle 3, 0x484848
    @landscapeGraphics.moveTo points[0].x, points[0].y
    for point in points
      @landscapeGraphics.lineTo(point.x, point.y)

  swapCoords: (point) -> {
    x: config.scalingFactor * (point.x + config.worldWidthDelta),
    y: config.scalingFactor * (config.height - point.y)
  }

  update: =>
    if @turnInProgress
      @intraTicker++
      if @currentTurn && @show
        @animateOutcome(outcome) for outcome in @currentTurn.outcome
      return
    @currentTurn = @nextTurn()
    if !@currentTurn
      return
    @startTurn()

  animateOutcome: (outcome) =>
    if outcome.bulletTrajectory
      @drawTrajectory(outcome.bulletTrajectory, 0xFFFFFF, 1)
      @drawTrajectory(outcome.bulletTrajectory, outcome.color, 0)
    if outcome.tankMovement
      @drawTankMovement(outcome,t) for t in @tanks when t.player.name == outcome.name

  nextTurn: =>
    if this.isConcluded()
      this.gameOver()
    if @turnIndex < @gameData.turns.length
      @gameData.turns[@turnIndex]

  gameOver: =>
    console.log("*** GAME OVER ***")
    @add.tween(@gameOverLabel).to { alpha: 1 }, 400, 'Linear', true
    @gameEnd.play()
    @time.events.add(Phaser.Timer.SECOND * 8, this.reloadWholeGame, this)

  reloadWholeGame: =>
    location.search = "?tournamentId=" + @tournamentId

  isConcluded: =>
    if @currentTurn && @currentTurn.last
      return true
    if @turnIndex > 0 && @currentTurn
      @turnIndex >= 200 || (t for t in @currentTurn.tanks when t.alive).length <= 1
    else
      false

  drawTankMovement: (outcome, tank) =>
    if outcome.tankMovement
      mov = outcome.tankMovement
      if mov.length > @intraTicker
        translated = @swapCoords(mov[@intraTicker])
        tank.move(translated.x, translated.y)

  drawTrajectory: (trajectory, color, widthMod) =>
    if !@shotPlayed
      @shootSound.play()
      @shotPlayed = true

    firstTranslated = @swapCoords(trajectory[0])

    trajectoryCount = trajectory.length

    fittedTicker = @intraTicker
    if @intraTicker>=trajectoryCount
      return

    if fittedTicker>0
      firstTranslated = @swapCoords(trajectory[fittedTicker-1])

    @trajectoryGraphics.moveTo(firstTranslated.x,firstTranslated.y)

    point = trajectory[trajectoryCount-1]
    if @intraTicker<trajectoryCount
      point = trajectory[@intraTicker]
    translatedPoint = @swapCoords(point)
    width = @intraTicker/trajectoryCount*3
    if width>6
      width=6
    if width<2
      width=2
    width=width+widthMod
    trajectoryAlpha=@intraTicker/trajectoryCount*2
    if trajectoryAlpha<0.4
      trajectoryAlpha=0.4
    @trajectoryGraphics.lineStyle width, color, trajectoryAlpha
    @trajectoryGraphics.lineTo(translatedPoint.x, translatedPoint.y)

  startTurn: =>
    console.log("starting turn " + @turnIndex)
    @shotPlayed = false
    @turnInProgress = true
    @intraTicker = 0
    @time.events.add(config.singleTurnTime, this.endTurn, this)

  endTurn: =>
#    console.log("ending turn " + @turnIndex+", tanks in current turn: "+@currentTurn.tanks)
    @trajectoryGraphics.clear()
    @setTankState(tank) for tank in @currentTurn.tanks
    @turnInProgress = false
    if @turnIndex <= 200
      @gameLabel.text = @gameData.setup.name + " turn #" + @turnIndex
    @turnIndex++
    tank.show() for tank in @tanks
    @show = true

  setTankState: (tankdata) ->
    this.updateTank(t, tankdata) for t in @tanks when t.player.name == tankdata.name

  updateTank: (tank, tankdata) ->
    console.log("setting end turn state for: "+tankdata.name+", hp:"+tankdata.hp)
    translatedPosition = this.swapCoords(tankdata.position)
    console.log('tankdata position')
    console.log(tankdata.position)
    tank.move(translatedPosition.x,translatedPosition.y)
    tank.updateHp tankdata.hp
    if tankdata.hp <= 0
      console.log("TANK DESTROYED")
      tank.destroy()
      @destroySound.play()

  getQueryParam: (name) ->
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]")
    regex = new RegExp("[\\?&]" + name + "=([^&#]*)")
    results = regex.exec(location.search)
    if results == null then "" else decodeURIComponent(results[1].replace(/\+/g, " "))


class Tank

  constructor: (@state, x, y, @hp, @player) ->
    @tankShadow = @state.add.sprite  -3, -3, 'tank_shadow'
    @tankShadow.scale.setTo(0.8,0.8)
    @tank = @state.add.sprite  0, 0, 'tank'
    @tank.tint = @player.color
    @tank.scale.setTo(0.7,0.7)
    @tankDescription = @state.add.text @tank.x, @tank.y - 20, '[' + @hp + ']', { font: "12px 'Press Start 2P'", fill: @player.color }

    @flame = @state.add.sprite 0, 0, 'flame'
    @flame.visible = false
    @flame.anchor.set 0.5, 0.5
    @emitter = @state.add.emitter 0, 0, 40
    @emitter.makeParticles 'flame'
    @emitter.setXSpeed -120, 120
    @emitter.setYSpeed -100, -200
    @emitter.setRotation()

    @playerDetailsOffset = -50
    @playerLabel = @state.add.text 0, 0, this.playerInfo() , { font: "18px 'Press Start 2P'", fill: '#'+@player.color.slice(2) }
    @playerLabel.setShadow 1, 1, 'rgba(0, 0, 0, 0.8)', 1

    @playerDetailsGroup = @state.game.add.group()
    @playerDetailsGroup.add @playerLabel
    @tankGroup = @state.game.add.group()
    @tankGroup.add element for element in [ @tankShadow, @tank ]

    this.hide()
    this.move(x, y)

  move: (x, y) =>
    @tankGroup.x = x - 20
    @tankGroup.y = y - 20
    if x > config.width / 2
      @playerDetailsGroup.x = x - @playerDetailsGroup.width
      @playerDetailsGroup.y = y + @playerDetailsOffset
    else
      @playerDetailsGroup.x = x
      @playerDetailsGroup.y = y + @playerDetailsOffset

  hide: () => this.visible(0)

  show: () => this.visible(1)

  visible: (value) =>
    @playerDetailsGroup.alpha = value
    @tankGroup.alpha = value

  updateHp: (hp) =>
    @hp = hp
    @playerLabel.text = this.playerInfo()

  playerInfo: =>
    @player.name + " [" + @hp + "]"

  destroy: =>
    @emitter.at @tank
    @emitter.explode 2000, 40
    @tankGroup.forEach (s) -> s.kill()
    @playerDetailsGroup.forEach (s) -> s.kill()

  fire: =>
    unless @tank.exists
      return
    turretEnd = new Phaser.Point(@turret.x, @turret.y)
    turretEnd.rotate turretEnd.x, turretEnd.y, @turret.rotation, false, 10
    @flame.x = turretEnd.x
    @flame.y = turretEnd.y
    @flame.alpha = 1
    @flame.visible = true
    @state.add.tween(@flame).to { alpha: 0 }, 250, 'Linear', true

module.exports = Game

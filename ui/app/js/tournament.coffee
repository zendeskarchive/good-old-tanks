config = require './config.coffee'
Phaser = require 'Phaser'
$ = require 'jquery'


class Tournament extends Phaser.State

  constructor: ->
    super
    @tournamentData = null

  preload: ->
    this.loadGames()

  loadGames: () =>
    isDev = this.getQueryParam('dev')
    if isDev
      $.ajax({ url: config.gameServerUrlDev })
        .done(this.setGameDataDev)
        .error(() => setTimeout this.loadGames, 1000)
    else
      tournamentId = this.getQueryParam('tournamentId')
      gameId = this.getQueryParam('gameId')
      $.ajax({ url: config.gameServerUrl + tournamentId + this.urlSuffix(gameId) }).then(this.setGameData)

  urlSuffix: (gameId) ->
    if gameId == ''
      "/games/current"
    else
      "/games/#{gameId}"

  setGameDataDev: (data) =>
    @tournamentData = data.games[0]

  setGameData: (data) =>
    @tournamentData = data

  update: =>
    if @tournamentData
      @state.start 'Game', true, false, @tournamentData

  getQueryParam: (name) ->
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]")
    regex = new RegExp("[\\?&]" + name + "=([^&#]*)")
    results = regex.exec(location.search)
    if results == null then "" else decodeURIComponent(results[1].replace(/\+/g, " "))

module.exports = Tournament

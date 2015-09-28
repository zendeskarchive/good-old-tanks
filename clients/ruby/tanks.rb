# requires rest-client installed: gem install rest-client

require 'rest-client'
require 'yaml'

SERVER = "http://localhost:9999"
TIMEOUT = 300000

TOURNAMENT_ID = "main"
AUTHORIZATION_TOKEN = "DisinterestedCrimsonGazelleDuck"

STD_ANGLE = 5.0
STD_MOVE = 30.0
POWER = 80
USERNAME = "Alex"

class RestClientWrapper < Struct.new(:tournamentId, :authorization)
  def post_move(params)
    url = "#{SERVER}/tournaments/#{tournamentId}/moves"
    headers = { 'Authorization' => authorization, 'content-type' => 'application/json' }
    RestClient::Request.execute(method: :post, payload: params.to_json, url: url, headers: headers, timeout: TIMEOUT)
  end
  
  def wait_for_game()
    url = "#{SERVER}/tournaments/#{tournamentId}/games/my/setup"
    headers = { 'Authorization' => authorization, 'content-type' => 'application/json' }
    RestClient::Request.execute(method: :get, url: url, headers: headers, timeout: TIMEOUT)
  end
end

class Bot < Struct.new(:rest_client)
  def perform_move(angle, power, distance)
    payload = {
      "shotAngle" => "#{angle}",
      "shotPower" => "#{power}",
      "moveDistance" => "#{distance}"
    }

    response = rest_client.post_move(payload)
    JSON.parse(response)
  end
  
  def wait_for_game()
    response = rest_client.wait_for_game()
    JSON.parse(response)
  end
end

class Tank < Struct.new(:name, :pos_x, :pos_y); end

class Tanks
  attr_reader :tanks

  def initialize(tanks)
    @tanks = []
    tanks.each do |tank|
      @tanks << Tank.new(tank["name"], tank["position"]["x"].to_f, tank["position"]["y"].to_f)
    end
  end

  def my_tank
    @tanks.find { |t| t.name == USERNAME }
  end
end


rest_client = RestClientWrapper.new(TOURNAMENT_ID, AUTHORIZATION_TOKEN)
bot = Bot.new(rest_client)

p "Bot initialized"

move_direction = 1


while true
  game = bot.wait_for_game()
  
  p "--- Joining game #{game['name']}"
  
  game_in_progress = true
  while game_in_progress
    if rand(2) > 0
      distance = rand(2) > 0 ? - STD_MOVE : STD_MOVE 
    
      p "Moving #{distance}"
      response = bot.perform_move(0, 0, distance)
    else
      p "Shooting #{STD_ANGLE} with power #{POWER}"
      response = bot.perform_move(STD_ANGLE, POWER, 0)
    end
    
    tanks = Tanks.new(response["tanks"])
  
    p "My position x: #{tanks.my_tank.pos_x}"
    
    game_in_progress = ! response['last']
  end
  
  p " --- game finished"
end

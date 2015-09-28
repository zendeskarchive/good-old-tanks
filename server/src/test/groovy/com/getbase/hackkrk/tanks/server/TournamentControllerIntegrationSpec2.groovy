package com.getbase.hackkrk.tanks.server

import com.getbase.hackkrk.tanks.ApiHelper
import com.getbase.hackkrk.tanks.bot.RandomBot
import com.getbase.hackkrk.tanks.bot.StupidBot
import com.jayway.awaitility.groovy.AwaitilityTrait
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.boot.test.WebIntegrationTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

@ContextConfiguration(loader = SpringApplicationContextLoader, classes = Application)
@ActiveProfiles('development')
@WebIntegrationTest("server.port=9999")
@Unroll

@Ignore
class TournamentControllerIntegrationSpec2 extends Specification implements AwaitilityTrait {
    @Value('9999')
    int port
    
    @Autowired
    TournamentRepository tournaments
    
    def client
    
    def players = [
        [name:"Alex", color:"0x3c87c9"],
        [name:"Boris",color:"0xc9703c"],
        [name:"alert('dupa')",color:"0xc9b63c"],
//        [name:"Bajtosmok",color:"0xa33cc9"],
//        [name:"O'Donnel",color:"0x00ffff"],
//        [name:"正體字/繁體字",color:"0xff00ff"]
    ]
    
    def "should successfully submit a Player move"() {
        given:
        def tournament = createTournament()
        startTournament(tournament)

        when:
            println "acces tokens " + tournament.data.playerAccessTokens
        
        int i = 0;
        for (def p : players){
            i++;
            def playerAccessToken = tournament.data.playerAccessTokens[p.name]
            def bot
            if(i == 1){
                bot =  new StupidBot("http://localhost:$port/", tournament.data.id, playerAccessToken, p.name)
            }else{
                bot = new RandomBot("http://localhost:$port/", tournament.data.id, playerAccessToken)
            }
            new Thread({->
                bot.run()
            }).start()
        }
        
        Thread.sleep(10000000)
        
        then:
        noExceptionThrown()
    }

    def createTournament(){
        ApiHelper.createTournament(port)
        def tournamentAfter
        for (def p : players){
            tournamentAfter = client.post(
                    path: "tournaments/$tournament.data.id/players",
                    headers: [
                            Authorization: tournament.data.adminToken
                    ],
                    body: [
                            name : p.name,
                            color: p.color,
                    ]
            )
        }
        tournamentAfter
    }
    
    def startTournament(tournament){
        client.post(
                path: "tournaments/$tournament.data.id/start",
                headers: [
                        Authorization: tournament.data.adminToken
                ]
        )
    }
}

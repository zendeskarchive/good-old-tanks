package com.getbase.hackkrk.tanks.bot;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Ordering;

import mikera.vectorz.Vector;
import mikera.vectorz.Vector2;

public class StupidBot {
    private WebTarget target;
    private String accessToken;
    private ObjectMapper mapper = new ObjectMapper();
    private String myName;
    
    public StupidBot(String url, String tournamentId, String accessToken, String myName){
        this.accessToken = accessToken;
        this.myName = myName;
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).addMixIn(Vector2.class, Vector2MixIn.class);
        Client client = ClientBuilder.newClient();
        target = client.target(url).path("tournaments/" + tournamentId + "/moves");
    }
    
    public void run() {
        // any move at the start to learn about the map
        Move move = new Move(10, 20, 0);
        Response r = sendMove(move);
        while (true) {
            Optional<Tank> maybeMe = r.tanks.stream()
                .filter(t -> t.name.equals(myName))
                .findFirst();
            if(!maybeMe.isPresent()){
                // not participating in current game
                sleep(1000);
                continue;
            }
            Tank me = maybeMe.get();
            
            Optional<Tank> enemy = r.tanks.stream()
                    .filter(t -> !t.name.equals(myName))
                    .filter(t -> t.alive)
                    .sorted((left, right) -> Ordering.natural().compare(left.name, right.name))
                    .findFirst();
            
            if(enemy.isPresent()){
                double moveX = enemy.get().position.x - me.position.x;
            
                Vector2 fireVector = enemy.get().position.clone();
                fireVector.sub(Vector.of(0, 5)); // tank height
                fireVector.sub(me.position);
                
                
                double angle = fireVector.angle(Vector2.of(0, 1)); // always positive
                angle = angle * Math.signum(moveX);
//                r = sendMove(new Move(Math.toDegrees(angle), 100, moveX > 0 ? 10 : -10));
                r = sendMove(new Move(Math.toDegrees(angle), 100, moveX));
            }else{
                r = sendMove(new Move(45, 100, -100));
            }
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

    private Response sendMove(Move move) {
        if(Math.random() > 0.5){
            move.moveDistance = 0;
        }else{
            move.shotPower = 0;
        }
        javax.ws.rs.core.Response response = null;
        try {
            String json = mapper.writeValueAsString(move);
            response = target.request().header("Authorization", accessToken).post(Entity.entity(json, MediaType.APPLICATION_JSON));

            String responseJson = response.readEntity(String.class);
            return mapper.readValue(responseJson, Response.class);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
    
    static class Move {
        public double shotAngle;
        public double shotPower;
        public double moveDistance;
        
        public Move(double shotAngle, double shotPower, double moveDistance) {
            this.shotAngle = shotAngle;
            this.shotPower = shotPower;
            this.moveDistance = moveDistance;
        }
    }
    
    static class Response {
        public List<Tank> tanks;
    }
    
    static class Tank{
        public String name;
        public Vector2 position;
        public boolean alive;
    }
    @JsonIgnoreProperties({"z","t","transpose","transposeCopy","transposeView","sliceViews","zero","mutable","view","fullyMutable","slices","shapeClone","longShape","shape","elements","elementConstrained"
                           ,"unitLengthVector","components","sparse","dense","boolean"})
    class Vector2MixIn{
    }

}

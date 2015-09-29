package com.getbase.hackkrk.tanks.api;

import java.util.List;

import mikera.vectorz.Vector2;

public class GameSetup {
    public String name;
    public Scene scene;
    public List<Player> players;

    public static class Scene {
        public Landscape landscape;

        @Override
        public String toString() {
            return "Scene [landscape=" + landscape + "]";
        }
    }
    
    public static class Landscape {
        public List<Vector2> points;

        @Override
        public String toString() {
            return "Landscape [points=" + points + "]";
        }
    }
    
    public static class Player{
        public String name;
        public String color;
        
        @Override
        public String toString() {
            return "Player [name=" + name + ", color=" + color + "]";
        }
        
    }

    @Override
    public String toString() {
        return "GameSetup [name=" + name + ",\n scene=" + scene + ",\n players=" + players + "]";
    }
}


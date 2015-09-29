package com.getbase.hackkrk.tanks.server.model.tournament;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Ordering;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = "color")
public class Player implements Comparable<Player>{

    private final String name;

    private final String color;

    @JsonCreator
    public Player(
            @JsonProperty("name") String name,
            @JsonProperty("color") String color) {
        this.name = name;
        this.color = color;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int compareTo(Player other) {
        return Ordering.natural().compare(this.name, other.name);
    }

}

package com.getbase.hackkrk.tanks.server.model.tournament;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@ToString
@Slf4j
public class Ranking {

    private final Map<String, Long> score;

    @JsonCreator
    protected Ranking(
            @JsonProperty("score") Map<String, Long> score) {
        this.score = new TreeMap<>(score);
    }

    public Ranking() {
        this(emptyMap());
    }

    public Map<String, Long> getScore() {
        return unmodifiableMap(score);
    }

    public void incScore(Player player, long playerScore) {
        log.info("Player {} scored {} points!", player, playerScore);
        this.score.merge(player.getName(), playerScore, Long::sum);
    }

    public Map<String, Long> getScoreForPlayers(List<Player> players) {
        final List<String> playerNames = players.stream().map(Player::getName).collect(toList());
        return this.score
                .entrySet()
                .stream()
                .filter(e -> playerNames.contains(e.getKey()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}

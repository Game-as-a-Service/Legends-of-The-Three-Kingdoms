package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class GameOverEvent extends DomainEvent {
    private final List<PlayerEvent> players;
    private final List<String> winners;

    public GameOverEvent(String message, List<String> winners, List<PlayerEvent> players){
        super("GameOverEvent", message);
        this.players = players;
        this.winners = winners;
    }
}

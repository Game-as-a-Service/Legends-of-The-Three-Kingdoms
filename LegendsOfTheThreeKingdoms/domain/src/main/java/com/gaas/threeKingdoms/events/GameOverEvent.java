package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class GameOverEvent extends DomainEvent {
    private final List<String> winners;

    public GameOverEvent(String message, List<String> winners){
        super("GameOverEvent", message);
        this.winners = winners;
    }
}

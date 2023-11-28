package com.waterball.LegendsOfTheThreeKingdoms.domain.events;

import lombok.Getter;

import java.util.List;

@Getter
public class GameOverEvent extends DomainEvent {
    private final String name = "GameOverEvent";
    private final List<PlayerEvent> players;
    private final List<String> winners;
    private final String message;

    public GameOverEvent(String message, List<String> winners, List<PlayerEvent> players){
        this.message = message;
        this.players = players;
        this.winners = winners;
    }
}

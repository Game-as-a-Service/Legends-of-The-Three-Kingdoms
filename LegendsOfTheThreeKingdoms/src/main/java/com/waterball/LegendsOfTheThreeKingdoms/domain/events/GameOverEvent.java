package com.waterball.LegendsOfTheThreeKingdoms.domain.events;

public class GameOverEvent extends DomainEvent {
    private final String name = "GameOverEvent";
    private final String message;

    public GameOverEvent(String message){
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }
}

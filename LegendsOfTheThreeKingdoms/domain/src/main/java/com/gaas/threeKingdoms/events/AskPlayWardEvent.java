package com.gaas.threeKingdoms.events;


import lombok.Getter;

@Getter
public class AskPlayWardEvent extends DomainEvent {

    private final String playerId;

    public AskPlayWardEvent(String playerId) {
        super("AskPlayWardEvent", String.format("%s 是否要出無懈可擊", playerId));
        this.playerId = playerId;
    }
}

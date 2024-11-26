package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class DismantleEvent extends DomainEvent {
    private String playerId;
    private String targetPlayerId;
    private String cardId;


    public DismantleEvent(String playerId, String cardId, String targetPlayerId, String message) {
        super("DismantleEvent", message);
        this.playerId = playerId;
        this.cardId = cardId;
        this.targetPlayerId = targetPlayerId;
    }

}

package com.gaas.threeKingdoms.events;

public class SnatchEvent  extends DomainEvent {
    private String playerId;
    private String targetPlayerId;
    private String cardId;

    public SnatchEvent(String playerId, String targetPlayerId, String cardId, String message) {
        super("SnatchEvent", message);
        this.playerId = playerId;
        this.targetPlayerId = targetPlayerId;
        this.cardId = cardId;

    }
}

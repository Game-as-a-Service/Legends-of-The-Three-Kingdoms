package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class PlayCardEvent extends DomainEvent {

    private String playerId;
    private String targetPlayerId;
    private String cardId;
    private String playType;

    public PlayCardEvent(String message, String playerId, String targetPlayerId, String cardId, String playType) {
        super("PlayCardEvent", message);
        this.playerId = playerId;
        this.targetPlayerId = targetPlayerId;
        this.cardId = cardId;
        this.playType = playType;
    }
}
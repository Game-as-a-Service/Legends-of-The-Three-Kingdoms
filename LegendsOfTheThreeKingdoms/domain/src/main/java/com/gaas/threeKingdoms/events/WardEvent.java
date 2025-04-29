package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class WardEvent extends DomainEvent {

    private final String playerId;
    private final String cardId;
    private final String wardCardId;

    public WardEvent(String playerId, String cardId, String wardCardId) {
        super("WardEvent", "發動無懈可擊");
        this.playerId = playerId;
        this.cardId = cardId;
        this.wardCardId = wardCardId;
    }
}

package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class LightningTransferredEvent extends DomainEvent{

    private final String sourcePlayerId;
    private final String targetPlayerId;
    private final String cardId;

    public LightningTransferredEvent(String sourcePlayerId, String targetPlayerId, String cardId, String message) {
        super("LightningTransferredEvent", message);
        this.sourcePlayerId = sourcePlayerId;
        this.targetPlayerId = targetPlayerId;
        this.cardId = cardId;
    }
}

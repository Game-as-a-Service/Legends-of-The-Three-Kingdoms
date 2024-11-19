package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class DuelEvent extends DomainEvent {

    private final String duelPlayerId;
    private final String targetPlayerId;
    private final String duelCardId;

    public DuelEvent(String duelPlayerId, String targetPlayerId, String duelCardId) {
        super("DuelEvent", String.format("%s 對 %s 發動決鬥", duelCardId, targetPlayerId));
        this.duelPlayerId = duelPlayerId;
        this.targetPlayerId = targetPlayerId;
        this.duelCardId = duelCardId;
    }
}

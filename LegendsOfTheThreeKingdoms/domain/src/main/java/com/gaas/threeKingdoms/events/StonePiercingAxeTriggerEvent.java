package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class StonePiercingAxeTriggerEvent extends DomainEvent {

    private final String attackerPlayerId;
    private final String targetPlayerId;
    private final List<String> discardedCardIds;

    public StonePiercingAxeTriggerEvent(String attackerPlayerId, String targetPlayerId, List<String> discardedCardIds) {
        super("StonePiercingAxeTriggerEvent",
                String.format("貫石斧發動：%s 棄兩張牌強制命中 %s", attackerPlayerId, targetPlayerId));
        this.attackerPlayerId = attackerPlayerId;
        this.targetPlayerId = targetPlayerId;
        this.discardedCardIds = discardedCardIds;
    }
}

package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class GreenDragonCrescentBladeTriggerEvent extends DomainEvent {

    private final String attackerPlayerId;
    private final String targetPlayerId;
    private final String killCardId;

    public GreenDragonCrescentBladeTriggerEvent(String attackerPlayerId, String targetPlayerId, String killCardId) {
        super("GreenDragonCrescentBladeTriggerEvent",
                String.format("青龍偃月刀發動：%s 對 %s 追加一張殺", attackerPlayerId, targetPlayerId));
        this.attackerPlayerId = attackerPlayerId;
        this.targetPlayerId = targetPlayerId;
        this.killCardId = killCardId;
    }
}

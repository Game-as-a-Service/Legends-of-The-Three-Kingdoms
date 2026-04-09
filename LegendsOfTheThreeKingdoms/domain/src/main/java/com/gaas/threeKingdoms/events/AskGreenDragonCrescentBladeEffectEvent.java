package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class AskGreenDragonCrescentBladeEffectEvent extends DomainEvent {

    private final String attackerPlayerId;
    private final String targetPlayerId;

    public AskGreenDragonCrescentBladeEffectEvent(String attackerPlayerId, String targetPlayerId) {
        super("AskGreenDragonCrescentBladeEffectEvent",
                String.format("青龍偃月刀效果：%s 是否要再出一張殺對 %s", attackerPlayerId, targetPlayerId));
        this.attackerPlayerId = attackerPlayerId;
        this.targetPlayerId = targetPlayerId;
    }

    public enum Choice {
        KILL,    // 發動效果，再出一張殺
        SKIP     // 不發動
    }
}

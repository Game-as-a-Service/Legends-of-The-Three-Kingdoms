package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class AskStonePiercingAxeEffectEvent extends DomainEvent {

    private final String attackerPlayerId;
    private final String targetPlayerId;

    public AskStonePiercingAxeEffectEvent(String attackerPlayerId, String targetPlayerId) {
        super("AskStonePiercingAxeEffectEvent",
                String.format("貫石斧效果：%s 是否棄兩張牌強制命中 %s", attackerPlayerId, targetPlayerId));
        this.attackerPlayerId = attackerPlayerId;
        this.targetPlayerId = targetPlayerId;
    }

    public enum Choice {
        DISCARD_TWO,  // 棄兩張牌強制命中
        SKIP          // 不發動
    }
}

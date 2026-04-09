package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class ViperSpearKillTriggerEvent extends DomainEvent {

    private final String attackerPlayerId;
    private final String targetPlayerId;
    private final List<String> discardedCardIds;

    public ViperSpearKillTriggerEvent(String attackerPlayerId, String targetPlayerId, List<String> discardedCardIds) {
        super("ViperSpearKillTriggerEvent",
                String.format("丈八蛇矛發動：%s 棄兩張牌當殺攻擊 %s", attackerPlayerId, targetPlayerId));
        this.attackerPlayerId = attackerPlayerId;
        this.targetPlayerId = targetPlayerId;
        this.discardedCardIds = discardedCardIds;
    }
}

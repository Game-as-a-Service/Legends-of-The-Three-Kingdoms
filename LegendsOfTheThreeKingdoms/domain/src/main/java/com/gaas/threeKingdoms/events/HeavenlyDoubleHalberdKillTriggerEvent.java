package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

/**
 * 方天畫戟發動事件：攻擊者使用殺並額外指定至多 2 名目標。
 * 前端收到此事件後應顯示多目標殺的動畫與狀態。
 */
@Getter
public class HeavenlyDoubleHalberdKillTriggerEvent extends DomainEvent {

    private final String attackerPlayerId;
    private final String cardId;
    private final List<String> targetPlayerIds;

    public HeavenlyDoubleHalberdKillTriggerEvent(String attackerPlayerId, String cardId, List<String> targetPlayerIds) {
        super("HeavenlyDoubleHalberdKillTriggerEvent",
                String.format("方天畫戟發動：%s 用殺攻擊 %s", attackerPlayerId, targetPlayerIds));
        this.attackerPlayerId = attackerPlayerId;
        this.cardId = cardId;
        this.targetPlayerIds = targetPlayerIds;
    }
}

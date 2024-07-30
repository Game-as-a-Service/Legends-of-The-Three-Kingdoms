package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class SkipEquipmentEffectEvent extends DomainEvent {
    private final String playerId;
    private final String cardId;

    public SkipEquipmentEffectEvent(String playerId, String cardId) {
        super("SkipEquipmentEffectEvent", "玩家已跳過裝備卡效果");
        this.playerId = playerId;
        this.cardId = cardId;
    }
}

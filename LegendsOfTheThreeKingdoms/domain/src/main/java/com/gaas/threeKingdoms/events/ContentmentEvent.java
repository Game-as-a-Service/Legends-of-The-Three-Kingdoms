package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class ContentmentEvent extends EffectEvent {
    private final String playerId;
    private final String drawCardId;

    public ContentmentEvent(boolean isSuccess, String playerId, String drawCardId) {
        super(isSuccess, String.format("樂不思蜀效果生效！抽出卡牌 %s 效果%s", drawCardId, isSuccess ? "成功" : "失敗"), "ContentmentEvent");
        this.playerId = playerId;
        this.drawCardId = drawCardId;
    }
}
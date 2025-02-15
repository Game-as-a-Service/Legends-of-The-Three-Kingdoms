package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.handcard.Suit;
import lombok.Getter;

@Getter
public class ContentmentEvent extends EffectEvent {
    private final String playerId;
    private final String drawCardId;

    public ContentmentEvent(boolean isSuccess, String playerId, String drawCardId, Suit suit) {
        super(isSuccess, String.format("樂不思蜀效果判定！抽出卡牌%s，花色是%s，效果%s", drawCardId, suit.getDisplayName(), isSuccess ? "成功" : "失敗"), "ContentmentEvent");
        this.playerId = playerId;
        this.drawCardId = drawCardId;
    }
}
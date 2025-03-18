package com.gaas.threeKingdoms.events;

import lombok.Getter;

import static com.gaas.threeKingdoms.handcard.PlayCard.findById;

@Getter
public class LightningEvent extends EffectEvent {
    private final String playerId;
    private final String drawCardId;

    public LightningEvent(boolean isSuccess, String playerId, String drawCardId) {
        super(isSuccess, String.format("閃電效果判定！抽出卡牌%s，是%s%s，效果%s", drawCardId, findById(drawCardId).getSuit().getDisplayName(), findById(drawCardId).getRank().getRepresentation(), isSuccess ? "成功" : "失敗"), "ContentmentEvent");
        this.playerId = playerId;
        this.drawCardId = drawCardId;
    }
}

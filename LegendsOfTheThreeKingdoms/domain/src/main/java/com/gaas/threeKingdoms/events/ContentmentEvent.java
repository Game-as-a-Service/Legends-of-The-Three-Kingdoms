package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.handcard.HandCard;
import lombok.Getter;

@Getter
public class ContentmentEvent extends EffectEvent {
    private final String playerId;
    private final String drawCardId;

    public ContentmentEvent(boolean isSuccess, String playerId, HandCard drawnCard) {
        super(isSuccess, isSuccess
                ? String.format("樂不思蜀判定：%s → 非紅心，生效，跳過出牌階段", drawnCard.judgementDescription())
                : String.format("樂不思蜀判定：%s → 紅心，未生效", drawnCard.judgementDescription()), "ContentmentEvent");
        this.playerId = playerId;
        this.drawCardId = drawnCard.getId();
    }
}

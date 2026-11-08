package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.handcard.HandCard;
import lombok.Getter;

@Getter
public class LightningEvent extends EffectEvent {
    private final String playerId;
    private final String drawCardId;

    public LightningEvent(boolean isSuccess, String playerId, HandCard drawnCard) {
        super(isSuccess, isSuccess
                ? String.format("閃電判定：%s → 黑桃2~9，生效，受到雷電傷害", drawnCard.judgementDescription())
                : String.format("閃電判定：%s → 未生效", drawnCard.judgementDescription()), "ContentmentEvent");
        this.playerId = playerId;
        this.drawCardId = drawnCard.getId();
    }
}

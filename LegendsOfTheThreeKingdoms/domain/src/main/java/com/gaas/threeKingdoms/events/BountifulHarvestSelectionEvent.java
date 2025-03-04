package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class BountifulHarvestSelectionEvent  extends DomainEvent {

    private final String playerId;
    private final String cardId;

    public BountifulHarvestSelectionEvent(String message, String playerId, String cardId) {
        super("BountifulHarvestChooseCardEvent", message); // 趙雲 選擇了 赤兔馬
        this.playerId = playerId;
        this.cardId = cardId;
    }
}
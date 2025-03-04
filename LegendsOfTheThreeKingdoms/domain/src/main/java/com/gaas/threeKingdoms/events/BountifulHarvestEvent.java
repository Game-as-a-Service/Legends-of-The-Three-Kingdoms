package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class BountifulHarvestEvent extends DomainEvent {

    private final String playerId;
    private final List<String> assignmentCardIds;

    public BountifulHarvestEvent(String message, String playerId, List<String> cardIds) {
        super("BountifulHarvestEvent", message); // 輪到 趙雲 選牌
        this.playerId = playerId;
        this.assignmentCardIds = cardIds;
    }
}

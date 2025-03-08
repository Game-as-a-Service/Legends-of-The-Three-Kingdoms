package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class BountifulHarvestEvent extends DomainEvent {

    private final String nextChoosingPlayerId;
    private final List<String> assignmentCardIds;

    public BountifulHarvestEvent(String message, String playerId, List<String> cardIds) {
        super("BountifulHarvestEvent", message);
        this.nextChoosingPlayerId = playerId;
        this.assignmentCardIds = cardIds;
    }
}

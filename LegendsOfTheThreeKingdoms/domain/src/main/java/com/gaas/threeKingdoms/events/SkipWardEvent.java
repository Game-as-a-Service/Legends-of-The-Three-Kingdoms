package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class SkipWardEvent extends DomainEvent {

    private final String playerId;
    private final String skipWardCardId;

    public SkipWardEvent(String playerId, String skipWardCardId) {
        super("SkipWardEvent", "不發動無懈可擊");
        this.playerId = playerId;
        this.skipWardCardId = skipWardCardId;
    }
}

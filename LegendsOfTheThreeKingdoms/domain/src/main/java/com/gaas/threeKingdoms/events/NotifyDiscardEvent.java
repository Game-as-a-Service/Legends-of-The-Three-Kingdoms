package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class NotifyDiscardEvent extends DomainEvent {
    private int discardCount;
    private String discardPlayerId;

    public NotifyDiscardEvent(
            String message,
            int discardCount,
            String discardPlayerId
    ) {
        super("NotifyDiscardEvent",message);
        this.discardCount = discardCount;
        this.discardPlayerId = discardPlayerId;
    }

}

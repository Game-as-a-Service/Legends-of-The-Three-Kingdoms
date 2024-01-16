package org.gaas.domain.events;

import org.gaas.domain.handcard.HandCard;
import lombok.Getter;

import java.util.List;
@Getter
public class DiscardEvent extends DomainEvent {
    private final String name = "DiscardEvent";
    private final String message;
    private final List<HandCard> discardCards;
    private final String discardPlayerId;

    public DiscardEvent(List<HandCard> discardCards,String message, String discardPlayerId) {
        this.discardCards = discardCards;
        this.message = message;
        this.discardPlayerId = discardPlayerId;
    }
}

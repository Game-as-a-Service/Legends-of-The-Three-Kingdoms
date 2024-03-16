package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.handcard.HandCard;
import lombok.Getter;

import java.util.List;
@Getter
public class DiscardEvent extends DomainEvent {
    private final List<HandCard> discardCards;
    private final String discardPlayerId;

    public DiscardEvent(List<HandCard> discardCards,String message, String discardPlayerId) {
        super("DiscardEvent", message);
        this.discardCards = discardCards;
        this.discardPlayerId = discardPlayerId;
    }
}

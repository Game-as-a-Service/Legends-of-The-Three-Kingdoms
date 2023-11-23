package com.waterball.LegendsOfTheThreeKingdoms.domain.events;

import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import lombok.Getter;

import java.util.List;
@Getter
public class DiscardEvent extends DomainEvent {
    private final String name = "DiscardEvent";
    private final String message;
    private final List<HandCard> discardCards;

    public DiscardEvent(List<HandCard> discardCards,String message) {
        this.discardCards = discardCards;
        this.message = message;
    }
}

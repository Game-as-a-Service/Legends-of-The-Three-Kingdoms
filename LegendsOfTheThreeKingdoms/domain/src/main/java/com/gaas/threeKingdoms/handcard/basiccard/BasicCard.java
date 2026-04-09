package com.gaas.threeKingdoms.handcard.basiccard;

import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.Rank;
import com.gaas.threeKingdoms.handcard.Suit;


public abstract class BasicCard extends HandCard {
    public BasicCard(PlayCard playCard) {
        super(playCard);
    }

    protected BasicCard(String name, String id, Suit suit, Rank rank) {
        super(name, id, suit, rank);
    }
}

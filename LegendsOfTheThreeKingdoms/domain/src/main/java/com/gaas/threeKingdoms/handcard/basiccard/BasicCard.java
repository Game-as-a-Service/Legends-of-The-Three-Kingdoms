package com.gaas.threeKingdoms.handcard.basiccard;

import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;


public abstract class BasicCard extends HandCard {
    public BasicCard(PlayCard playCard) {
        super(playCard);
    }
}

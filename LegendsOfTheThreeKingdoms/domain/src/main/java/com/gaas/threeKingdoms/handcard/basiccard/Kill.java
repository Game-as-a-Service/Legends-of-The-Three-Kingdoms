package com.gaas.threeKingdoms.handcard.basiccard;

import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.Rank;
import com.gaas.threeKingdoms.handcard.Suit;
import com.gaas.threeKingdoms.player.Player;

public class Kill extends BasicCard {

    public Kill(PlayCard playCard) {
        super(playCard);
    }

    protected Kill(String name, String id, Suit suit, Rank rank) {
        super(name, id, suit, rank);
    }

    @Override
    public void effect(Player targetPlayer) {
        targetPlayer.damage(1);
    }

}

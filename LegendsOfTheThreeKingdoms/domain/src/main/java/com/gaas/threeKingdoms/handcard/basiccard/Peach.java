package com.gaas.threeKingdoms.handcard.basiccard;

import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;

public class Peach extends BasicCard {

    public Peach(PlayCard playCard) {
        super(playCard);
    }
    @Override
    public void effect(Player player) {
        player.heal(1);
    }
}

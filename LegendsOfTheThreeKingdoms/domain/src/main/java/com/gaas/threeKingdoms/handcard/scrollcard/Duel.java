package com.gaas.threeKingdoms.handcard.scrollcard;

import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;

public class Duel extends ScrollCard {

    public Duel(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public void effect(Player player) {
        player.damage(1);
    }
}

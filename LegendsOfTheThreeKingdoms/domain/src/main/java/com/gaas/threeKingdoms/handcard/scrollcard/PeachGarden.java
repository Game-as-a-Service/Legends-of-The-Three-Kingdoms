package com.gaas.threeKingdoms.handcard.scrollcard;

import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;

public class PeachGarden extends ScrollCard {
    public PeachGarden(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public void effect(Player player) {
        if (player.isStillAlive()) player.heal(1);
    }
}

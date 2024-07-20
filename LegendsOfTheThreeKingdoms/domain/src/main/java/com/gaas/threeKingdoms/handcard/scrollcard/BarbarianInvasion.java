package com.gaas.threeKingdoms.handcard.scrollcard;

import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;

public class BarbarianInvasion extends ScrollCard {
    public BarbarianInvasion(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public void effect(Player targetPlayer) {
        targetPlayer.damage(1);
    }
}

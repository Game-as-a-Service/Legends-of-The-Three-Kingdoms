package com.gaas.threeKingdoms.handcard.basiccard;

import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;

public class Kill extends BasicCard {

    public Kill(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public void effect(Player targetPlayer) {
        targetPlayer.damage(1);
    }

}

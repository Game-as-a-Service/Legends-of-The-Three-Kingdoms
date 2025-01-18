package com.gaas.threeKingdoms.handcard.scrollcard;

import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;

public class ArrowBarrage extends ScrollCard {
    public ArrowBarrage(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public void effect(Player targetPlayer) {
        targetPlayer.damage(1);
    }
}

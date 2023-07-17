package com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard;

import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;

public class Kill extends BasicCard {

    public Kill(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public void effect(Player targetPlayer) {
        targetPlayer.damage(1);
    }

}

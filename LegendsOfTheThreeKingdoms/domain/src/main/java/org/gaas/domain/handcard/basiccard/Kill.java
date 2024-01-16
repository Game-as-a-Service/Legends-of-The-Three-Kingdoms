package org.gaas.domain.handcard.basiccard;

import org.gaas.domain.handcard.PlayCard;
import org.gaas.domain.player.Player;

public class Kill extends BasicCard {

    public Kill(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public void effect(Player targetPlayer) {
        targetPlayer.damage(1);
    }

}

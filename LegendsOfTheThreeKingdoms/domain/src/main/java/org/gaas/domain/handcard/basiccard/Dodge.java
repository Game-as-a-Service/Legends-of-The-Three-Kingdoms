package org.gaas.domain.handcard.basiccard;

import org.gaas.domain.handcard.PlayCard;
import org.gaas.domain.player.Player;

public class Dodge extends BasicCard {

    public Dodge(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public void effect(Player player) {

    }

    public boolean isDodge(String cardId){

        return true;
    }


}

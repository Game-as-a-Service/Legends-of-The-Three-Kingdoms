package com.gaas.threeKingdoms.handcard.basiccard;

import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;

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

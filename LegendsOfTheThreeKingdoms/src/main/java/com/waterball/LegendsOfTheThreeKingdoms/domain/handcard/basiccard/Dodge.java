package com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard;

import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.BasicCard;

public class Dodge extends BasicCard {
    public Dodge() {
        super("閃");
    }

    @Override
    public String getCardId(){return "D";}
}

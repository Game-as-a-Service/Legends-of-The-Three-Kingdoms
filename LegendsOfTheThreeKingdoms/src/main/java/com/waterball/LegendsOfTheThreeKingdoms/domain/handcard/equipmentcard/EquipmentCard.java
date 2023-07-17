package com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.equipmentcard;

import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayCard;

public abstract class EquipmentCard extends HandCard {

    public EquipmentCard(PlayCard playCard) {
        super(playCard);
    }
}

package com.gaas.threeKingdoms.handcard.equipmentcard;

import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import lombok.Getter;

@Getter
public abstract class EquipmentCard extends HandCard {
    protected boolean hasSpecialEffect = false;

    public EquipmentCard(PlayCard playCard) {
        super(playCard);
    }
}

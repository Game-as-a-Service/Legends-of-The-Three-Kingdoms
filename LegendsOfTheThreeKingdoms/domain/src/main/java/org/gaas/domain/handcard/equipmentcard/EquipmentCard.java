package org.gaas.domain.handcard.equipmentcard;

import org.gaas.domain.handcard.HandCard;
import org.gaas.domain.handcard.PlayCard;

public abstract class EquipmentCard extends HandCard {

    public EquipmentCard(PlayCard playCard) {
        super(playCard);
    }
}

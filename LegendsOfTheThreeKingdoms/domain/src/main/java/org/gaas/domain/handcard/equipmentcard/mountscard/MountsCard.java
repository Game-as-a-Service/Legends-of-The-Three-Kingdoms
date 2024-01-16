package org.gaas.domain.handcard.equipmentcard.mountscard;

import org.gaas.domain.handcard.PlayCard;
import org.gaas.domain.handcard.equipmentcard.EquipmentCard;
import org.gaas.domain.player.Player;

public abstract class MountsCard extends EquipmentCard {
    public MountsCard(PlayCard playCard) {
        super(playCard);
    }
}

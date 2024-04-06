package com.gaas.threeKingdoms.handcard.equipmentcard.mountscard;

import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import com.gaas.threeKingdoms.player.Player;

public class MinusMountsCard extends EquipmentCard {
    public MinusMountsCard(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public void effect(Player player) {
        player.getEquipment().setMinusOne(this);
    }
}

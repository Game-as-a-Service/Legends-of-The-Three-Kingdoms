package com.gaas.threeKingdoms.handcard.equipmentcard.armorcard;

import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;

/**
 * 諸葛連弩
 * */
public abstract class ArmorCard extends EquipmentCard {

    public ArmorCard(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public void effect(Player player) {
        player.getEquipment().setArmor(this);
    }

}

package com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard;

import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import com.gaas.threeKingdoms.player.Player;
public abstract class WeaponCard extends EquipmentCard {

    public WeaponCard(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public void effect(Player player) {
        player.getEquipment().setWeapon(this);
    }

}

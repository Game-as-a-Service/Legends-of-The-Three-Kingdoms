package com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard;

import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import com.gaas.threeKingdoms.player.Player;
public abstract class WeaponCard extends EquipmentCard {
    protected int weaponDistance;
    public WeaponCard(PlayCard playCard, int weaponDistance) {
        super(playCard);
        this.weaponDistance = weaponDistance;
    }

    @Override
    public void effect(Player player) {
        player.getEquipment().setWeapon(this);
    }

    public int getWeaponDistance() {return weaponDistance;}

}

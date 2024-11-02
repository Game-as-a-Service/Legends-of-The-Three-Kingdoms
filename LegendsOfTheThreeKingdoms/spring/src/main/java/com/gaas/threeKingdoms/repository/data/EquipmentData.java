package com.gaas.threeKingdoms.repository.data;


import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.ArmorCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.MinusMountsCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.PlusMountsCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.WeaponCard;
import com.gaas.threeKingdoms.player.Equipment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentData {

    private String plusOneCardId;
    private String minusOneCardId;
    private String armorCardId;
    private String weaponCardId;

    public Equipment toDomain() {
        Equipment equipment = new Equipment();
        equipment.setPlusOne((PlusMountsCard) PlayCard.findById(this.plusOneCardId));
        equipment.setMinusOne((MinusMountsCard) PlayCard.findById(this.minusOneCardId));
        equipment.setArmor((ArmorCard) PlayCard.findById(this.armorCardId));
        equipment.setWeapon((WeaponCard) PlayCard.findById(this.weaponCardId));
        return equipment;
    }

    public static EquipmentData fromDomain(Equipment equipment) {
        EquipmentData equipmentData = new EquipmentData();
        equipmentData.setPlusOneCardId(equipment.getPlusOne() == null ? null : equipment.getPlusOne().getId());
        equipmentData.setMinusOneCardId(equipment.getMinusOne() == null ? null : equipment.getMinusOne().getId());
        equipmentData.setArmorCardId(equipment.getArmor() == null ? null : equipment.getArmor().getId());
        equipmentData.setWeaponCardId(equipment.getWeapon() == null ? null : equipment.getWeapon().getId());
        return equipmentData;
    }
}

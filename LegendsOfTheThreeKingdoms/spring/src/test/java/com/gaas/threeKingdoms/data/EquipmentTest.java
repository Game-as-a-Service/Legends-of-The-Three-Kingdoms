package com.gaas.threeKingdoms.data;

import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.ArmorCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.MinusMountsCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.PlusMountsCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.WeaponCard;
import com.gaas.threeKingdoms.player.Equipment;
import com.gaas.threeKingdoms.repository.data.EquipmentData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EquipmentTest {

    @Test
    public void testToDomainConversion() {
        // Arrange
        EquipmentData equipmentData = EquipmentData.builder()
                .plusOneCardId("ES5018")
                .minusOneCardId("EH5044")
                .armorCardId("ES2015")
                .weaponCardId("EH5031")
                .build();

        // Act
        Equipment equipment = equipmentData.toDomain();

        // Assert
        assertEquals("ES5018", equipment.getPlusOne().getId());
        assertEquals("EH5044", equipment.getMinusOne().getId());
        assertEquals("ES2015", equipment.getArmor().getId());
        assertEquals("EH5031", equipment.getWeapon().getId());
    }

    @Test
    public void testFromDomainConversion() {
        // Arrange
        PlusMountsCard plusOne = (PlusMountsCard) PlayCard.findById("ES5018");
        MinusMountsCard minusOne = (MinusMountsCard) PlayCard.findById("EH5044");
        ArmorCard armor = (ArmorCard) PlayCard.findById("ES2015");
        WeaponCard weapon = (WeaponCard) PlayCard.findById("EH5031");

        Equipment equipment = new Equipment();
        equipment.setPlusOne(plusOne);
        equipment.setMinusOne(minusOne);
        equipment.setArmor(armor);
        equipment.setWeapon(weapon);

        // Act
        EquipmentData equipmentData = EquipmentData.fromDomain(equipment);

        // Assert
        assertEquals("ES5018", equipmentData.getPlusOneCardId());
        assertEquals("EH5044", equipmentData.getMinusOneCardId());
        assertEquals("ES2015", equipmentData.getArmorCardId());
        assertEquals("EH5031", equipmentData.getWeaponCardId());
    }
}
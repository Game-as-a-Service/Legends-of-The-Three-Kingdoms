package com.gaas.threeKingdoms.player;

import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.ArmorCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.MinusMountsCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.PlusMountsCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.WeaponCard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Equipment {
    private PlusMountsCard plusOne;
    private MinusMountsCard minusOne;
    private ArmorCard armor;
    private WeaponCard weapon;

    public boolean hasAnyEquipment() {
        return Stream.of(plusOne, minusOne, armor, weapon).anyMatch(Objects::nonNull);
    }

    public List<String> getAllEquipmentCardIds() {
        return Stream.of(weapon, armor, plusOne, minusOne)
                .map(equipment -> equipment != null ? equipment.getId() : "")
                .collect(Collectors.toList());
    }

    public boolean hasSpecialEffect() {
        boolean plusOneHasSpecialEffect = plusOne != null && plusOne.isHasSpecialEffect();
        boolean minusOneHasSpecialEffect = minusOne != null && minusOne.isHasSpecialEffect();
        boolean armorHasSpecialEffect = armor != null && armor.isHasSpecialEffect();
        boolean weaponHasSpecialEffect = weapon != null && weapon.isHasSpecialEffect();
        return plusOneHasSpecialEffect || minusOneHasSpecialEffect || armorHasSpecialEffect || weaponHasSpecialEffect;
    }

    public boolean hasThisEquipment(String equipmentId) {
        return Stream.of(plusOne, minusOne, armor, weapon)
                .anyMatch(equipment -> equipment != null && equipment.getId().equals(equipmentId));
    }

    public void removeEquipment(String equipmentId) {
        if (plusOne != null && plusOne.getId().equals(equipmentId)) {
            plusOne = null;
        }
        if (minusOne != null && minusOne.getId().equals(equipmentId)) {
            minusOne = null;
        }
        if (armor != null && armor.getId().equals(equipmentId)) {
            armor = null;
        }
        if (weapon != null && weapon.getId().equals(equipmentId)) {
            weapon = null;
        }
    }

}

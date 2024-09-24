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

}

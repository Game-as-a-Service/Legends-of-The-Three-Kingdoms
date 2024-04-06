package com.gaas.threeKingdoms.player;

import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.ArmorCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.MinusMountsCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.PlusMountsCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.WeaponCard;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class Equipment {
    private PlusMountsCard plusOne;
    private MinusMountsCard minusOne;
    private ArmorCard armor;
    private WeaponCard weapon;

    public List<String> getAllEquipmentCardIds() {
        return Stream.of(plusOne, minusOne, armor, weapon)
                .map(equipment -> equipment != null ? equipment.getId() : "")
                .collect(Collectors.toList());
    }
}

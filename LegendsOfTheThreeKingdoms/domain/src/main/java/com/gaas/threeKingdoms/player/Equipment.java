package com.gaas.threeKingdoms.player;

import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.ArmorCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.MountsCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.WeaponCard;
import lombok.Data;

@Data
public class Equipment {
      private MountsCard plusOne;
      private MountsCard minusOne;
      private ArmorCard armor;
      private WeaponCard weapon;
}

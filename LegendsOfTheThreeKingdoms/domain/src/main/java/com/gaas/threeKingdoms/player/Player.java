package com.gaas.threeKingdoms.player;


import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.ArmorCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.MinusMountsCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.PlusMountsCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.WeaponCard;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private Hand hand;
    private String id;
    private RoleCard roleCard;
    private GeneralCard generalCard;
    private BloodCard bloodCard;
    private HealthStatus healthStatus;
    private Equipment equipment = new Equipment();

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    public void setBloodCard(BloodCard bloodCard) {
        this.bloodCard = bloodCard;
    }

    public int getHP() {
        return bloodCard.getHp();
    }

    public int getHandSize() {
        return hand.size();
    }

    public HandCard playCard(String cardId) {
        return hand.playCard(cardId);
    }

    public PlusMountsCard getEquipmentPlusOneMountsCard() {
        return equipment.getPlusOne();
    }

    public MinusMountsCard getEquipmentMinusOneMountsCard() {
        return equipment.getMinusOne();
    }

    public ArmorCard getEquipmentArmorCard() {
        return equipment.getArmor();
    }

    public WeaponCard getEquipmentWeaponCard() {
        return equipment.getWeapon();
    }

    public void damage(int i) {
        bloodCard.setHp(getHP() - i);
    }

    public void heal(int value) {
        bloodCard.setHp(Math.min(getHP() + value, bloodCard.getMaxHp()));
    }

    public int judgeEscapeDistance() {
        PlusMountsCard plusOne = equipment.getPlusOne();
        if (plusOne == null) {
            return 0;
        } else {
            return 1;
        }
    }

    public int judgeAttackDistance() {
        int weaponDis = getWeaponDistance();
        int minusOne = getMinusOneDistance();
        return weaponDis + minusOne + 1; // 初始攻擊距離 1
    }

    private int getWeaponDistance() {
        if (getEquipmentWeaponCard() == null) return 0;
        WeaponCard weaponCard = equipment.getWeapon();
        return weaponCard.getWeaponDistance();
    }

    private int getMinusOneDistance() {
        MinusMountsCard minusOne = equipment.getMinusOne();
        if (minusOne == null) {
            return 0;
        } else {
            return 1; // 我打別人的攻擊距離
        }
    }

    public boolean hasAnyDelayScrollCard() {
        return false;
    }

    public boolean isHandCardSizeBiggerThanHP() {
        return getHandSize() > getHP();
    }

    public List<HandCard> discardCards(List<String> cardIds) {
        return cardIds.stream()
                .map(id -> hand.playCard(id))
                .collect(Collectors.toList());
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public int getDiscardCount() {
        if (isHandCardSizeBiggerThanHP()) {
            return hand.size() - bloodCard.getHp();
        } else {
            return 0;
        }
    }
}

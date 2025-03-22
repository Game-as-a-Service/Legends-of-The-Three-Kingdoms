package com.gaas.threeKingdoms.player;


import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.RemoveHorseEvent;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.ArmorCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.MinusMountsCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.PlusMountsCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.WeaponCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.ScrollCard;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
public class Player {
    private Hand hand;
    private String id;
    private RoleCard roleCard;
    private GeneralCard generalCard;
    private BloodCard bloodCard;
    private HealthStatus healthStatus;
    private Equipment equipment = new Equipment();
    private Stack<ScrollCard> delayScrollCards = new Stack<>();

    public Player(Hand hand, String id, RoleCard roleCard, GeneralCard generalCard, BloodCard bloodCard, HealthStatus healthStatus,
                  Equipment equipment) {
        this.hand = hand;
        this.id = id;
        this.roleCard = roleCard;
        this.generalCard = generalCard;
        this.bloodCard = bloodCard;
        this.healthStatus = healthStatus;
        this.equipment = equipment;
    }

    public Player(Hand hand, String id, RoleCard roleCard, GeneralCard generalCard, BloodCard bloodCard, HealthStatus healthStatus,
                  Equipment equipment, Stack<ScrollCard> delayScrollCards) {
        this.hand = hand;
        this.id = id;
        this.roleCard = roleCard;
        this.generalCard = generalCard;
        this.bloodCard = bloodCard;
        this.healthStatus = healthStatus;
        this.equipment = equipment;
        this.delayScrollCards = delayScrollCards;
    }

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

    public boolean hasThisEquipment(String equipmentId) {
       return equipment.hasThisEquipment(equipmentId);
    }

    public boolean hasMountsCard() {
        return equipment.getMinusOne() != null || equipment.getPlusOne() != null;
    }

    public boolean onlyHasOneMount() {
        PlusMountsCard plusOne = equipment.getPlusOne();
        MinusMountsCard minusOne = equipment.getMinusOne();
        return (plusOne == null && minusOne != null) || (plusOne != null && minusOne == null);
    }

    public HandCard removeOneMount() {
        PlusMountsCard plusOne = equipment.getPlusOne();
        MinusMountsCard minusOne = equipment.getMinusOne();
        if (plusOne != null) {
            equipment.setPlusOne(null);
            return plusOne;
        } else {
            equipment.setMinusOne(null);
            return minusOne;
        }
    }

    public DomainEvent removeMountsCard(String playerId, String cardId) {
        EquipmentCard plusOne = equipment.getPlusOne();
        EquipmentCard minusOne = equipment.getMinusOne();
        if (plusOne.getId().equals(cardId)) {
            equipment.setPlusOne(null);
        } else if (minusOne.getId().equals(cardId)) {
            equipment.setMinusOne(null);
        }
        return new RemoveHorseEvent("RemoveHorseEvent",
                String.format("%s 玩家 已被%s 移除 %s 卡", id, playerId, cardId),
                id,
                cardId);
    }

    public HandCard getMountsCard(String cardId) {
        EquipmentCard plusOne = equipment.getPlusOne();
        EquipmentCard minusOne = equipment.getMinusOne();
        if (plusOne.getId().equals(cardId)) {
            return plusOne;
        } else if (minusOne.getId().equals(cardId)) {
            return minusOne;
        }
        return null;
    }

    public List<HandCard> discardAllCards() {
        List<HandCard> discardCards = hand.getCards();
        hand.setCards(new ArrayList<>());
        return discardCards;
    }

    public List<EquipmentCard> discardAllEquipment() {
        List<EquipmentCard> discardEquipmentCards = equipment.getAllEquipmentCards();
        equipment.removeAllEquipment();
        return discardEquipmentCards;
    }

    public ArmorCard getEquipmentArmorCard() {
        return equipment.getArmor();
    }

    public WeaponCard getEquipmentWeaponCard() {
        return equipment.getWeapon();
    }

    public void damage(int i) {
        bloodCard.setHp(getHP() - i);
        if (bloodCard.getHp() <= 0) {
            setHealthStatus(HealthStatus.DYING);
        }
    }

    public void heal(int value) {
        bloodCard.setHp(Math.min(getHP() + value, bloodCard.getMaxHp()));
        if (HealthStatus.DYING.equals(healthStatus) && isHPGreaterThanZero()) {
            setHealthStatus(HealthStatus.ALIVE);
        }
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
        return !delayScrollCards.isEmpty();
    }

    public boolean hasAnyContentmentCard() {
        return delayScrollCards.stream().anyMatch(card -> card instanceof Contentment);
    }

    public boolean hasThisDelayScrollCard(String cardId) {
        return delayScrollCards.stream().anyMatch(card -> card.getId().equals(cardId));
    }

    public void removeDelayScrollCard(String cardId) {
        delayScrollCards.stream()
                .filter(scrollCard -> scrollCard.getId().equals(cardId))
                .findFirst()
                .ifPresentOrElse(delayScrollCards::remove,
                        () -> {
                            throw new RuntimeException("Card with ID " + cardId + " not found");
                        });
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

    public boolean isStillAlive() {
        return HealthStatus.ALIVE.equals(healthStatus);
    }

    public boolean isAlreadyDeath() {
        return HealthStatus.DEATH.equals(healthStatus);
    }

    public boolean isHPGreaterThanZero() {
        return this.getHP() > 0;
    }

    public void addDelayScrollCard(ScrollCard card) {
        delayScrollCards.add(card);
    }

    public List<String> getDelayScrollCardIds() {
        return delayScrollCards.stream()
                .map(ScrollCard::getId)
                .collect(Collectors.toList());
    }

    public String getGeneralName() {
        return generalCard.getGeneralName();
    }


    // 覆寫 equals 方法
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; // 檢查是否為同一個物件
        if (obj == null || getClass() != obj.getClass()) return false; // 類型不同則不相等

        Player player = (Player) obj;
        return id != null && id.equals(player.id); // 根據 id 屬性來判斷
    }

    // 覆寫 hashCode 方法
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0; // 根據 id 的 hashCode 計算
    }
}

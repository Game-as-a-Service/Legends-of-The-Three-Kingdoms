package org.gaas.domain.player;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.gaas.domain.generalcard.GeneralCard;
import org.gaas.domain.handcard.HandCard;
import org.gaas.domain.rolecard.RoleCard;

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

    public void damage(int i) {
        bloodCard.setHp(getHP() - i);
    }

    public int judgeEscapeDistance() {
        return 0;
    }

    public int judgeAttackDistance() {
        return 1;
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

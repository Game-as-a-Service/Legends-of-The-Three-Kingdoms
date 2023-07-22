package com.waterball.LegendsOfTheThreeKingdoms.domain.player;


import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Kill;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.RoleCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private boolean isShowKill;

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
        HandCard handCard = hand.getCard(cardId);
        if (handCard instanceof Kill && isShowKill) {
            throw new IllegalStateException("Player already played Kill Card");
        } else if (handCard instanceof Kill) {
            setShowKill(true);
        }
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

    public boolean handCardSizeBiggerThanHP() {
        return false;
    }
}

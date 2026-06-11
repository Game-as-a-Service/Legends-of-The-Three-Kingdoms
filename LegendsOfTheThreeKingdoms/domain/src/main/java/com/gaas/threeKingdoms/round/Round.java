package com.gaas.threeKingdoms.round;

import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.player.Player;
import lombok.Data;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@Data
public class Round {
    private RoundPhase roundPhase;
    private Player currentRoundPlayer;
    private Player activePlayer;
    private Player dyingPlayer;
    private HandCard currentCard;
    private boolean isShowKill;
    private Stage stage;
    // 每回合限一次的技能使用紀錄（反間 / 結姻 / 突襲）+ 仁德本回合已給牌數
    private Set<String> usedOncePerTurnSkills = new HashSet<>();
    private int renDeGivenCount = 0;
    private boolean renDeHealed = false;

    public Round (Player currentRoundPlayer) {
        this.roundPhase = RoundPhase.Judgement;
        this.currentRoundPlayer = currentRoundPlayer;
        this.activePlayer = currentRoundPlayer;
        this.stage = Stage.Normal;
    }
    public boolean isPlayedValidCard(String cardId) {
        Optional<HandCard> handCardOptional = currentRoundPlayer.getHand().getCard(cardId);
        if (handCardOptional.isEmpty()) throw new IllegalStateException("Player " + currentRoundPlayer.getId() + " have no this card: " + cardId);

        HandCard handCard = handCardOptional.get();
        if (handCard instanceof Kill && (currentRoundPlayer.getEquipmentWeaponCard() instanceof RepeatingCrossbowCard
                || com.gaas.threeKingdoms.skill.registry.SkillEngine.isKillCountUnlimited(currentRoundPlayer))) {
            // 諸葛連弩 / 咆哮：使用殺無次數限制
            isShowKill = false;
        } else if (handCard instanceof Kill && isShowKill) {
            throw new IllegalStateException("Player already played Kill Card");
        } else if (handCard instanceof Kill) {
            isShowKill = true;
        }
        return true;
    }
}

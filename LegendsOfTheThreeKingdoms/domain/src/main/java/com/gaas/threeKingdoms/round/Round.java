package com.gaas.threeKingdoms.round;

import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.player.Player;
import lombok.Data;

import java.util.Optional;


@Data
public class Round {
    private RoundPhase roundPhase;
    private Player currentRoundPlayer;
    private Player activePlayer;
    private Player dyingPlayer;
    private HandCard currentPlayCard;
    private boolean isShowKill;
    private Stage stage;

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
        if (handCard instanceof Kill && currentRoundPlayer.getEquipmentWeaponCard() instanceof RepeatingCrossbowCard) {
            isShowKill = false;
        } else if (handCard instanceof Kill && isShowKill) {
            throw new IllegalStateException("Player already played Kill Card");
        } else if (handCard instanceof Kill) {
            isShowKill = true;
        }
        return true;
    }
}

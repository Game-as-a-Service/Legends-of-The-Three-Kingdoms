package com.waterball.LegendsOfTheThreeKingdoms.domain;

import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Kill;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import lombok.Data;


@Data
public class Round {
    private RoundPhase roundPhase;
    private Player currentRoundPlayer;
    private Player activePlayer;
    private Player dyingPlayer;
    private boolean isShowKill;

    public Round (Player currentRoundPlayer) {
        this.roundPhase = RoundPhase.Judgement;
        this.currentRoundPlayer = currentRoundPlayer;
    }
    public boolean isPlayerPlayedKill(String cardId){
        HandCard handCard = currentRoundPlayer.getHand().getCard(cardId);
        if (handCard instanceof Kill && isShowKill) {
            throw new IllegalStateException("Player already played Kill Card");
        } else if (handCard instanceof Kill) {
            isShowKill = true;
        }
        return true;
    }
}

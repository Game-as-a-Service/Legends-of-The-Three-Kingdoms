package com.waterball.LegendsOfTheThreeKingdoms.domain;

import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Kill;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import lombok.Data;


@Data
public class Round {
    private Phase phase;
    private Player currentPlayer;
    private boolean isShowKill;

    public Round (Player currentPlayer) {
        this.phase = Phase.Judgement;
        this.currentPlayer = currentPlayer;
    }
    public boolean checkPlayedCardIsValid(String cardId){
        HandCard handCard = currentPlayer.getHand().getCard(cardId);
        if (handCard instanceof Kill && isShowKill) {
            throw new IllegalStateException("Player already played Kill Card");
        } else if (handCard instanceof Kill) {
            isShowKill = true;
        }
        return true;
    }
}

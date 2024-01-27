package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
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

    public Round (Player currentRoundPlayer) {
        this.roundPhase = RoundPhase.Judgement;
        this.currentRoundPlayer = currentRoundPlayer;
    }
    public boolean isPlayedValidCard(String cardId) {
        Optional<HandCard> handCardOptional = currentRoundPlayer.getHand().getCard(cardId);
        if (handCardOptional.isEmpty()) return false;

        HandCard handCard = handCardOptional.get();
        if (handCard instanceof Kill && isShowKill) {
            throw new IllegalStateException("Player already played Kill Card");
        } else if (handCard instanceof Kill) {
            isShowKill = true;
        }
        return true;
    }
}

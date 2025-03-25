package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.RoundPhase;
import com.gaas.threeKingdoms.round.Stage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoundData {
    private String roundPhase;
    private String currentRoundPlayer;
    private String activePlayer;
    private String dyingPlayer;
    private String currentCard;
    private boolean isShowKill;
    private String stage;

    public static RoundData fromDomain(Round round) {
        if (round == null) {
            return null;
        }

        return RoundData.builder()
                .roundPhase(round.getRoundPhase().name())
                .currentRoundPlayer(round.getCurrentRoundPlayer().getId())
                .activePlayer(round.getActivePlayer().getId())
                .dyingPlayer(round.getDyingPlayer() != null ? round.getDyingPlayer().getId() : null)
                .currentCard(round.getCurrentCard() != null ? round.getCurrentCard().getId() : null)
                .isShowKill(round.isShowKill())
                .stage(round.getStage().name())
                .build();
    }

    public Round toDomain(Game game) {
        Round round = new Round(game.getPlayer(currentRoundPlayer));
        round.setRoundPhase(RoundPhase.valueOf(this.roundPhase));
        round.setActivePlayer(game.getPlayer(activePlayer));
        round.setDyingPlayer(dyingPlayer == null ? null : game.getPlayer(dyingPlayer));
        round.setCurrentCard(PlayCard.findById(currentCard));
        round.setShowKill(isShowKill);
        round.setStage(Stage.valueOf(this.stage));
        return round;
    }
}

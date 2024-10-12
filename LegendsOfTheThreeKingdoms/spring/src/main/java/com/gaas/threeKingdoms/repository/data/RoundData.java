package com.gaas.threeKingdoms.repository.data;

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
    private PlayerData currentRoundPlayer;
    private PlayerData activePlayer;
    private PlayerData dyingPlayer;
    private String currentPlayCard;
    private boolean isShowKill;
    private String stage;

    public static RoundData fromDomain(Round round) {
        return RoundData.builder()
                .roundPhase(round.getRoundPhase().name())
                .currentRoundPlayer(PlayerData.fromDomain(round.getCurrentRoundPlayer()))
                .activePlayer(PlayerData.fromDomain(round.getActivePlayer()))
                .dyingPlayer(PlayerData.fromDomain(round.getDyingPlayer()))
                .currentPlayCard(round.getCurrentPlayCard() != null ? round.getCurrentPlayCard().getId() : null)
                .isShowKill(round.isShowKill())
                .stage(round.getStage().name())
                .build();
    }

    public Round toDomain() {
        Round round = new Round(currentRoundPlayer.toDomain());
        round.setRoundPhase(RoundPhase.valueOf(this.roundPhase));
        round.setActivePlayer(activePlayer.toDomain());
        round.setDyingPlayer(dyingPlayer == null ? null : dyingPlayer.toDomain());
        round.setCurrentPlayCard(PlayCard.findById(currentPlayCard));
        round.setShowKill(isShowKill);
        round.setStage(Stage.valueOf(this.stage));
        return round;
    }
}

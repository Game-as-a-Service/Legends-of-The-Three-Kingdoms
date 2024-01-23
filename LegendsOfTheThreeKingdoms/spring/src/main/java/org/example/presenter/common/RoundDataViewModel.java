package org.example.presenter.common;

import org.gaas.domain.events.RoundEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoundDataViewModel {
    private String roundPhase;
    private String currentRoundPlayer;
    private String activePlayer;
    private String dyingPlayer;
    private boolean isShowKill;

    public RoundDataViewModel(RoundEvent roundEvent) {
        this.roundPhase = roundEvent.getRoundPhase();
        this.currentRoundPlayer = roundEvent.getCurrentRoundPlayer();
        this.activePlayer = roundEvent.getActivePlayer();
        this.dyingPlayer = roundEvent.getDyingPlayer();
        this.isShowKill = roundEvent.isShowKill();
    }
}
package com.waterball.LegendsOfTheThreeKingdoms.domain.events;


import com.waterball.LegendsOfTheThreeKingdoms.domain.Round;

import java.util.Optional;

public class RoundEvent extends DomainEvent {
    private String roundPhase;
    private String currentRoundPlayer;
    private String activePlayer;
    private String dyingPlayer;
    private boolean isShowKill;

    public RoundEvent(String roundPhase, String currentRoundPlayer, String activePlayer, String dyingPlayer, boolean isShowKill) {
        this.roundPhase = roundPhase;
        this.currentRoundPlayer = currentRoundPlayer;
        this.activePlayer = activePlayer;
        this.dyingPlayer = dyingPlayer;
        this.isShowKill = isShowKill;
    }

    public RoundEvent(Round currentRound) {
        this.roundPhase = currentRound.getRoundPhase().toString();
        this.currentRoundPlayer = currentRound.getCurrentRoundPlayer().getId();
        this.activePlayer = Optional.ofNullable(currentRound.getActivePlayer()).map(activeplayer -> activeplayer.getId()).orElse("");
        this.dyingPlayer = Optional.ofNullable(currentRound.getDyingPlayer()).map(dyingPlayer -> dyingPlayer.getId()).orElse("");
        this.isShowKill = currentRound.isShowKill();
    }

    public String getRoundPhase() {
        return roundPhase;
    }

    public String getCurrentRoundPlayer() {
        return currentRoundPlayer;
    }

    public String getActivePlayer() {
        return activePlayer;
    }

    public String getDyingPlayer() {
        return dyingPlayer;
    }

    public boolean isShowKill() {
        return isShowKill;
    }
}

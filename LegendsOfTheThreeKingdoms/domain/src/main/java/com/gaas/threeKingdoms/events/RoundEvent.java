package com.gaas.threeKingdoms.events;


import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.Optional;

public class RoundEvent extends DomainEvent {
    private String roundPhase;
    private String currentRoundPlayer;
    private String activePlayer;
    private String dyingPlayer;
    private boolean isShowKill;
    private String currentPlayedCard;

    public RoundEvent(String roundPhase, String currentRoundPlayer, String activePlayer, String dyingPlayer, boolean isShowKill) {
        super("RoundEvent", "Round Event");
        this.roundPhase = roundPhase;
        this.currentRoundPlayer = currentRoundPlayer;
        this.activePlayer = activePlayer;
        this.dyingPlayer = dyingPlayer;
        this.isShowKill = isShowKill;
    }

    public RoundEvent(Round currentRound) {
        super("RoundEvent", "Round Event");
        this.roundPhase = currentRound.getRoundPhase().toString();
        this.currentRoundPlayer = currentRound.getCurrentRoundPlayer().getId();
        this.activePlayer = Optional.ofNullable(currentRound.getActivePlayer()).map(Player::getId).orElse("");
        this.dyingPlayer = Optional.ofNullable(currentRound.getDyingPlayer()).map(Player::getId).orElse("");
        this.isShowKill = currentRound.isShowKill();
        this.currentPlayedCard = Optional.ofNullable(currentRound.getCurrentPlayCard()).map(HandCard::getId).orElse("");
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

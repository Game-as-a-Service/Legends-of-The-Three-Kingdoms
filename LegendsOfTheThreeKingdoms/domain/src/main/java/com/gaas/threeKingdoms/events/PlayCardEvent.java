package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class PlayCardEvent extends DomainEvent {

    private String playerId;
    private String targetPlayerId;
    private String cardId;
    private String playType;
    private String gameId;
    private List<PlayerEvent> seats;
    private RoundEvent round;
    private String gamePhase;


    public PlayCardEvent(String message, String playerId, String targetPlayerId, String cardId, String playType, String gameId, List<PlayerEvent> seats, RoundEvent round, String gamePhase) {
        super("PlayCardEvent", message);
        this.playerId = playerId;
        this.targetPlayerId = targetPlayerId;
        this.cardId = cardId;
        this.playType = playType;
        this.gameId = gameId;
        this.seats = seats;
        this.round = round;
        this.gamePhase = gamePhase;
    }
}
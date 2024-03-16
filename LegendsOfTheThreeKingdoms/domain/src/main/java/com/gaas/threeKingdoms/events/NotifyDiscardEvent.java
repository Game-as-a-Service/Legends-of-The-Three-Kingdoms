package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class NotifyDiscardEvent extends DomainEvent {
    private int discardCount;
    private String discardPlayerId;

    // Game state
    private String playerId;
    private String gameId;
    private List<PlayerEvent> seats;
    private RoundEvent round;
    private String gamePhase;

    public NotifyDiscardEvent(
            String message,
            int discardCount,
            String discardPlayerId,
            String playerId,
            String gameId,
            List<PlayerEvent> seats,
            RoundEvent round,
            String gamePhase
    ) {
        super("NotifyDiscardEvent",message);
        this.discardCount = discardCount;
        this.discardPlayerId = discardPlayerId;
        this.playerId = playerId;
        this.gameId = gameId;
        this.seats = seats;
        this.round = round;
        this.gamePhase = gamePhase;
    }

}

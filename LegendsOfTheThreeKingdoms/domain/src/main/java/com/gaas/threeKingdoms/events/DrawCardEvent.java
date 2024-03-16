package com.gaas.threeKingdoms.events;

import lombok.*;

import java.util.List;


@Getter
public class DrawCardEvent extends DomainEvent {
    private int size;
    private List<String> cardIds;
    private String gameId;
    private List<PlayerEvent> seats;
    private RoundEvent round;
    private String gamePhase;
    private String drawCardPlayerId;

    public DrawCardEvent(int size, List<String> cardIds, String message, String gameId, List<PlayerEvent> seats, RoundEvent round, String gamePhase, String drawCardPlayerId) {
        super("DrawCardToPlayerEvent", message);
        this.size = size;
        this.cardIds = cardIds;
        this.gameId = gameId;
        this.seats = seats;
        this.round = round;
        this.gamePhase = gamePhase;
        this.drawCardPlayerId = drawCardPlayerId;
    }
}

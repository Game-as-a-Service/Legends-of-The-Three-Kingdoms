package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class GameStatusEvent extends DomainEvent {
    private String gameId;
    private List<PlayerEvent> seats;
    private RoundEvent round;
    private String gamePhase;
    public GameStatusEvent(String gameId, List<PlayerEvent> seats, RoundEvent round, String gamePhase, String message) {
        super("GameStatusEvent", message);
        this.gameId = gameId;
        this.seats = seats;
        this.round = round;
        this.gamePhase = gamePhase;
    }
}

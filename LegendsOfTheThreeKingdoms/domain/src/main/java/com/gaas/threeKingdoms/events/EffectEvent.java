package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class EffectEvent extends DomainEvent {
    private final boolean isSuccess;
    private String gameId;
    private List<PlayerEvent> seats;
    private RoundEvent round;
    private String gamePhase;

    public EffectEvent(boolean isSuccess, String message, String gameId, List<PlayerEvent> seats, String name, RoundEvent round, String gamePhase) {
        super(name, message);
        this.isSuccess = isSuccess;
        this.gameId = gameId;
        this.seats = seats;
        this.round = round;
        this.gamePhase = gamePhase;
    }

}

package com.gaas.threeKingdoms.events;

import lombok.*;

import java.util.List;

@Getter
public class InitialEndEvent extends DomainEvent {
    private String gameId;

    private List<PlayerEvent> seats;

    private RoundEvent round;

    private String gamePhase;

    public InitialEndEvent(String gameId, List<PlayerEvent> seats, RoundEvent round, String gamePhase) {
        super("OtherChooseGeneralCardEvent", "所有玩家選完腳色牌");
        this.gameId = gameId;
        this.seats = seats;
        this.round = round;
        this.gamePhase = gamePhase;
    }
}

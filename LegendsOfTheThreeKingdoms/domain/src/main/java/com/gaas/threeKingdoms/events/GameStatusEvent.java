package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class GameStatusEvent extends DomainEvent {
    private String gameId;
    private List<PlayerEvent> playerEvents;
    private RoundEvent round;
    private String gamePhase;
    public GameStatusEvent(String gameId, List<PlayerEvent> playerEvents, RoundEvent round, String gamePhase) {
        super("GameStatusEvent", "遊戲狀態");
        this.gameId = gameId;
        this.playerEvents = playerEvents;
        this.round = round;
        this.gamePhase = gamePhase;
    }
}

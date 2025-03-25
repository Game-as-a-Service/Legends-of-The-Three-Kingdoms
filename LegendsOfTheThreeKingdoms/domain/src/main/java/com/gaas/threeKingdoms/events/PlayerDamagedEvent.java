package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class PlayerDamagedEvent extends DomainEvent {
    private final String name = "PlayerDamagedEvent";
    private final String message = "扣血";
    private final String playerId;
    private final int from;
    private final int to;

    public PlayerDamagedEvent(String playerId, int from, int to) {
        super("PlayerDamagedEvent", "扣血");
        this.playerId = playerId;
        this.from = from;
        this.to = to;
    }
}

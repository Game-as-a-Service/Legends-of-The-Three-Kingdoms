package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class PeachEvent extends DomainEvent {
    private final String name = "PeachEvent";
    private final String message;
    private final String playerId;
    private final int from;
    private final int to;


    public PeachEvent(String message, String playerId, int from, int to) {
        this.message = message;
        this.playerId = playerId;
        this.from = from;
        this.to = to;
    }
}

package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class PeachEvent extends DomainEvent {
    private final String playerId;
    private final int from;
    private final int to;


    public PeachEvent(String playerId, int from, int to) {
        super("PeachEvent", "玩家已使用桃");
        this.playerId = playerId;
        this.from = from;
        this.to = to;
    }
}

package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class SomethingForNothingEvent extends DomainEvent {
    private final String playerId;

    public SomethingForNothingEvent(String playerId) {
        super("SomethingForNothingEvent", String.format("玩家 %s 使用了無中生有", playerId));
        this.playerId = playerId;
    }
}

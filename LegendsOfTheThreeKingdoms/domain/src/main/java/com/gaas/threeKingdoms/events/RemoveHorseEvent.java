package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class RemoveHorseEvent extends DomainEvent{

    private final String playerId;
    private final String mountCardId;

    public RemoveHorseEvent(String name, String message, String playerId, String mountCardId) {
        super(name, message);
        this.playerId = playerId;
        this.mountCardId = mountCardId;
    }
}

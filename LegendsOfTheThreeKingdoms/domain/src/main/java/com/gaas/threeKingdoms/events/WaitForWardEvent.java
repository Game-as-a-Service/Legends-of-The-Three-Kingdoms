package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.Set;

@Getter
public class WaitForWardEvent extends DomainEvent {
    private final Set<String> playerIds;
    public WaitForWardEvent(Set<String> playerIds) {
        super("WaitForWardEvent", "");
        this.playerIds = playerIds;
    }
}

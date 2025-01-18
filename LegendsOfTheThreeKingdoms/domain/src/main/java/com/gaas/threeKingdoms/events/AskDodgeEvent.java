package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class AskDodgeEvent extends DomainEvent {

    private final String playerId;

    public AskDodgeEvent(String playerId) {
        super("AskDodgeEvent", "要求玩家出閃");
        this.playerId = playerId;
    }
}

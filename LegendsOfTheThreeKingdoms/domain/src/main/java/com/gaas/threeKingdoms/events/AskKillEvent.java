package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class AskKillEvent extends DomainEvent {

    private final String playerId;

    public AskKillEvent(String playerId) {
        super("AskKillEvent", "要求玩家出殺");
        this.playerId = playerId;
    }
}

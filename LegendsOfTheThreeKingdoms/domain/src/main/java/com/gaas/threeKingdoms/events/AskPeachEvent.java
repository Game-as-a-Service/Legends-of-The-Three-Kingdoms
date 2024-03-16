package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class AskPeachEvent extends DomainEvent{

    private String playerId;
    private String dyingPlayerId;

    public AskPeachEvent(String playerId, String dyingPlayerId) {
        super("AskPeachEvent", "要求玩家出桃");
        this.playerId = playerId;
        this.dyingPlayerId = dyingPlayerId;
    }

}

package org.gaas.domain.events;

import java.util.List;

public class AskPeachEvent extends DomainEvent{

    private String playerId;
    private String name = "AskPeachEvent";
    private String message = "要求玩家出桃";

    public AskPeachEvent(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }
}

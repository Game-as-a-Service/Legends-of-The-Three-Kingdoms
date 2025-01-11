package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class FinishActionEvent extends DomainEvent {

    private final String playerId;


    public FinishActionEvent(String playerId) {
        super("FinishActionEvent", "結束出牌");
        this.playerId = playerId;
    }
}

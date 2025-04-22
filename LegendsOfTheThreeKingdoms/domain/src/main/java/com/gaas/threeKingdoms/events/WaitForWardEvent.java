package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class WaitForWardEvent extends DomainEvent {
    private final String playerId;

    public WaitForWardEvent(String playerId) {
        super("WaitForWardEvent", String.format("%s 等待其他玩家是否要出無懈可擊", playerId));
        this.playerId = playerId;
    }
}

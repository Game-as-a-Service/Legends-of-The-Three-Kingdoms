package com.gaas.threeKingdoms.events;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PlayerDyingEvent extends DomainEvent {

    private String playerId;
    private final String name = "PlayerDyingEvent";
    private final String message = "玩家已瀕臨死亡";

    public PlayerDyingEvent(String playerId) {
        this.playerId = playerId;
    }
}

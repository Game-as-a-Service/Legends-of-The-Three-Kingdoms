package com.gaas.threeKingdoms.events;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class PlayerDyingEvent extends DomainEvent {

    private final String playerId;
    public PlayerDyingEvent(String playerId) {
        super("PlayerDyingEvent", "玩家已瀕臨死亡");
        this.playerId = playerId;
    }
}

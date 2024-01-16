package org.gaas.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
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

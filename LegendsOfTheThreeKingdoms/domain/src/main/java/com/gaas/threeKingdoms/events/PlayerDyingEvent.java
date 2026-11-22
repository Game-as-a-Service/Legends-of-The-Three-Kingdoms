package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.player.Player;
import lombok.Getter;

@Getter
public class PlayerDyingEvent extends DomainEvent {

    private final String playerId;

    public PlayerDyingEvent(Player player) {
        super("PlayerDyingEvent", String.format("%s 已瀕臨死亡", player.getGeneralName()));
        this.playerId = player.getId();
    }
}

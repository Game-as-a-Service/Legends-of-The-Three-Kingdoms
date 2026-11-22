package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.player.Player;
import lombok.Getter;

@Getter
public class PlayerDamagedEvent extends DomainEvent {
    private final String playerId;
    private final int from;
    private final int to;

    public PlayerDamagedEvent(Player player, int from, int to) {
        super("PlayerDamagedEvent", String.format("%s 扣 %d 點血", player.getGeneralName(), from - to));
        this.playerId = player.getId();
        this.from = from;
        this.to = to;
    }
}

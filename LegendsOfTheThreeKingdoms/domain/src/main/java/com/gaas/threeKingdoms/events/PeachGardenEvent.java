package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.player.Player;
import lombok.Getter;

import java.util.List;

@Getter
public class PeachGardenEvent extends DomainEvent {
    private final String playerId;
    private final List<PeachEvent> peachEvents;

    public PeachGardenEvent(Player player, List<PeachEvent> peachEvents) {
        super("PeachGardenEvent", String.format("%S 已使用桃園結義", player.getGeneralCard().getGeneralName()));
        this.playerId = player.getId();
        this.peachEvents = peachEvents;
    }
}

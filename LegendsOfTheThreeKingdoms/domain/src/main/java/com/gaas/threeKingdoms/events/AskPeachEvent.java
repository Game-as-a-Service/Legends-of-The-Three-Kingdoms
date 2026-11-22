package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.player.Player;
import lombok.Getter;

@Getter
public class AskPeachEvent extends DomainEvent {

    private final String playerId;
    private final String dyingPlayerId;

    public AskPeachEvent(Player player, Player dyingPlayer) {
        super("AskPeachEvent", String.format("要求 %s 出桃（救 %s）", player.getGeneralName(), dyingPlayer.getGeneralName()));
        this.playerId = player.getId();
        this.dyingPlayerId = dyingPlayer.getId();
    }
}

package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.Set;

@Getter
public class WaitForWardEvent extends DomainEvent {
    private final Set<String> playerIds;
    private final String wardTriggerPlayerId;
    private final String wardTriggerCardId;

    public WaitForWardEvent(Set<String> playerIds, String whoCauseWardPlayId, String wardTriggerCardId) {
        super("WaitForWardEvent", "等待玩家發動無懈可擊");
        this.playerIds = playerIds;
        this.wardTriggerPlayerId = whoCauseWardPlayId;
        this.wardTriggerCardId = wardTriggerCardId;
    }
}

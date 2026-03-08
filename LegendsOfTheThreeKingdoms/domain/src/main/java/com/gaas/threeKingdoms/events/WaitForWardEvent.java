package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
public class WaitForWardEvent extends DomainEvent {
    private final Set<String> playerIds;
    private final String wardTriggerPlayerId;
    private final String wardTriggerCardId;
    private final List<String> targetPlayerIds;

    public WaitForWardEvent(Set<String> playerIds, String whoCauseWardPlayId, String wardTriggerCardId, List<String> targetPlayerIds) {
        super("WaitForWardEvent", "等待玩家發動無懈可擊");
        this.playerIds = playerIds;
        this.wardTriggerPlayerId = whoCauseWardPlayId;
        this.wardTriggerCardId = wardTriggerCardId;
        this.targetPlayerIds = targetPlayerIds;
    }
}

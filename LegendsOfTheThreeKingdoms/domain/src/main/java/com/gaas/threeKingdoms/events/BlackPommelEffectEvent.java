package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class BlackPommelEffectEvent extends EffectEvent {
    private final String attackerPlayerId;
    private final String targetPlayerId;

    public BlackPommelEffectEvent(String attackerPlayerId, String targetPlayerId) {
        super(true, "青釭劍發動，殺無視防具", "BlackPommelEffectEvent");
        this.attackerPlayerId = attackerPlayerId;
        this.targetPlayerId = targetPlayerId;
    }
}

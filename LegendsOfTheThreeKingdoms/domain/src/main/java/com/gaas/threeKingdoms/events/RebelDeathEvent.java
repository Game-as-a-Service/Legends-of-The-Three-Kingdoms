package com.gaas.threeKingdoms.events;


public class RebelDeathEvent extends DomainEvent {
    private final String deadPlayerId;
    private final String getEffectPlayerId;

    public RebelDeathEvent(String deadPlayerId, String getEffectPlayerId) {
        super("RebelDeathEvent", "反賊" + deadPlayerId + "死亡，" + getEffectPlayerId + "抽三張卡");
        this.deadPlayerId = deadPlayerId;
        this.getEffectPlayerId = getEffectPlayerId;
    }
}

package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class JianXiongEffectEvent extends DomainEvent {

    private final String playerId;
    private final String sourceCardId;
    private final boolean taken;

    public JianXiongEffectEvent(String playerId, String sourceCardId, boolean taken) {
        super("JianXiongEffectEvent",
                taken
                        ? String.format("奸雄：%s 獲得 %s", playerId, sourceCardId)
                        : String.format("奸雄：%s 放棄 %s", playerId, sourceCardId));
        this.playerId = playerId;
        this.sourceCardId = sourceCardId;
        this.taken = taken;
    }
}

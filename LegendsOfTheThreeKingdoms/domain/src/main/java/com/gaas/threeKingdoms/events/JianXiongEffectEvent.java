package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class JianXiongEffectEvent extends DomainEvent {

    private final String playerId;
    private final List<String> sourceCardIds;
    private final boolean taken;

    public JianXiongEffectEvent(String playerId, List<String> sourceCardIds, boolean taken) {
        super("JianXiongEffectEvent",
                taken
                        ? String.format("奸雄：%s 獲得 %s", playerId, sourceCardIds)
                        : String.format("奸雄：%s 放棄 %s", playerId, sourceCardIds));
        this.playerId = playerId;
        this.sourceCardIds = sourceCardIds;
        this.taken = taken;
    }
}

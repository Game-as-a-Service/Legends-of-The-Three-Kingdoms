package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class AskJianXiongEffectEvent extends DomainEvent {

    private final String playerId;
    private final String sourceCardId;

    public AskJianXiongEffectEvent(String playerId, String sourceCardId) {
        super("AskJianXiongEffectEvent",
                String.format("奸雄：%s 是否獲得造成傷害的牌 %s", playerId, sourceCardId));
        this.playerId = playerId;
        this.sourceCardId = sourceCardId;
    }

    public enum Choice {
        ACCEPT,
        SKIP
    }
}

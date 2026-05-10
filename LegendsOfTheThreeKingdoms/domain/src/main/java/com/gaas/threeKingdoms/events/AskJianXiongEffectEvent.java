package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

@Getter
public class AskJianXiongEffectEvent extends DomainEvent {

    private final String playerId;
    private final List<String> sourceCardIds;

    public AskJianXiongEffectEvent(String playerId, List<String> sourceCardIds) {
        super("AskJianXiongEffectEvent",
                String.format("奸雄：%s 是否獲得造成傷害的牌 %s", playerId, sourceCardIds));
        this.playerId = playerId;
        this.sourceCardIds = sourceCardIds;
    }

    public enum Choice {
        ACCEPT,
        SKIP
    }
}

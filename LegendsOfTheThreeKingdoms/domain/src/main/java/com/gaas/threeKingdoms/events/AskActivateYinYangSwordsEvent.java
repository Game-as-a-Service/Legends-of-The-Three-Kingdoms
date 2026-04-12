package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class AskActivateYinYangSwordsEvent extends DomainEvent {

    private final String attackerPlayerId;
    private final String targetPlayerId;

    public AskActivateYinYangSwordsEvent(String attackerPlayerId, String targetPlayerId) {
        super("AskActivateYinYangSwordsEvent",
                String.format("雌雄雙股劍效果：%s 是否要對 %s 發動雌雄雙股劍？", attackerPlayerId, targetPlayerId));
        this.attackerPlayerId = attackerPlayerId;
        this.targetPlayerId = targetPlayerId;
    }

    public enum Choice {
        ACTIVATE, SKIP
    }
}

package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class AskYinYangSwordsEffectEvent extends DomainEvent {

    private final String attackerPlayerId;
    private final String targetPlayerId;

    public AskYinYangSwordsEffectEvent(String attackerPlayerId, String targetPlayerId) {
        super("AskYinYangSwordsEffectEvent", String.format("雌雄雙股劍效果：%s 請選擇棄一張手牌或讓 %s 摸一張牌", targetPlayerId, attackerPlayerId));
        this.attackerPlayerId = attackerPlayerId;
        this.targetPlayerId = targetPlayerId;
    }
}

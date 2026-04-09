package com.gaas.threeKingdoms.events;

import lombok.Getter;

@Getter
public class YinYangSwordsEffectEvent extends DomainEvent {

    private final String attackerPlayerId;
    private final String targetPlayerId;
    private final Choice choice;
    private final String discardedCardId;  // null if choice is ATTACKER_DRAWS

    public YinYangSwordsEffectEvent(String attackerPlayerId, String targetPlayerId, Choice choice, String discardedCardId) {
        super("YinYangSwordsEffectEvent", String.format("雌雄雙股劍效果：%s %s", targetPlayerId,
                choice == Choice.TARGET_DISCARDS ? "棄牌" : "讓攻擊者摸牌"));
        this.attackerPlayerId = attackerPlayerId;
        this.targetPlayerId = targetPlayerId;
        this.choice = choice;
        this.discardedCardId = discardedCardId;
    }

    public enum Choice {
        TARGET_DISCARDS,    // 目標棄一張手牌
        ATTACKER_DRAWS      // 目標讓攻擊者摸一張牌
    }
}

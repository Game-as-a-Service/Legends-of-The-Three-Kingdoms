package com.gaas.threeKingdoms.events;

import lombok.Getter;

import java.util.List;

/**
 * 護駕：詢問 Wei 武將是否代替主公曹操出閃。
 *
 * playerId            — 被詢問的 Wei 武將 ID（當前 reactor）
 * caoCaoPlayerId      — 被代替的主公（曹操）ID
 * dodgeCardIdsInHand  — 該 Wei 手中所有「閃」cardId，可用於 ACCEPT
 */
@Getter
public class AskHuJiaEffectEvent extends DomainEvent {

    private final String playerId;
    private final String caoCaoPlayerId;
    private final List<String> dodgeCardIdsInHand;

    public AskHuJiaEffectEvent(String playerId, String caoCaoPlayerId, List<String> dodgeCardIdsInHand) {
        super("AskHuJiaEffectEvent",
                String.format("護駕：詢問 %s 是否代替主公 %s 打出閃", playerId, caoCaoPlayerId));
        this.playerId = playerId;
        this.caoCaoPlayerId = caoCaoPlayerId;
        this.dodgeCardIdsInHand = dodgeCardIdsInHand;
    }

    public enum Choice {
        ACCEPT,
        DECLINE
    }
}

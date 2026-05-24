package com.gaas.threeKingdoms.events;

import lombok.Getter;

/**
 * 護駕：Wei 武將回應結果廣播。
 *
 * playerId        — 回應的 Wei 武將 ID
 * caoCaoPlayerId  — 被代替的曹操 ID
 * accepted        — true 為代閃成功（含 dodgeCardId）；false 為拒絕
 * dodgeCardId     — accepted=true 時 Wei 打出的閃；accepted=false 時為 null
 */
@Getter
public class HuJiaEffectEvent extends DomainEvent {

    private final String playerId;
    private final String caoCaoPlayerId;
    private final boolean accepted;
    private final String dodgeCardId;

    public HuJiaEffectEvent(String playerId, String caoCaoPlayerId, boolean accepted, String dodgeCardId) {
        super("HuJiaEffectEvent",
                String.format("護駕：%s %s 代替 %s 出閃", playerId, accepted ? "成功" : "拒絕", caoCaoPlayerId));
        this.playerId = playerId;
        this.caoCaoPlayerId = caoCaoPlayerId;
        this.accepted = accepted;
        this.dodgeCardId = dodgeCardId;
    }
}

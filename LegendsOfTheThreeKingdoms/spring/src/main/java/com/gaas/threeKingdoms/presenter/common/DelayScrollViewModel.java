package com.gaas.threeKingdoms.presenter.common;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.gaas.threeKingdoms.events.DelayScrollEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判定區的一張牌。cardId 是實際的牌（畫牌面用），effect 是它在判定區的效果。
 * 國色轉化時兩者不同：例 { cardId: "SD3081"（方塊3 順手牽羊）, effect: "CONTENTMENT", effectName: "樂不思蜀" }。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"cardId", "effect", "effectName"})
public class DelayScrollViewModel {
    private String cardId;
    private String effect;
    private String effectName;

    public DelayScrollViewModel(DelayScrollEvent event) {
        this(event.getCardId(), event.getEffect(), event.getEffectName());
    }
}

package com.gaas.threeKingdoms.events;

import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.handcard.scrollcard.ScrollCard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判定區的一張牌：實際的牌（cardId）＋它在判定區的效果（effect）。
 * <p>
 * 兩者可能不同：國色（大喬）會把方塊的任意牌當樂不思蜀放進判定區，
 * 例如 SD3081（方塊3 順手牽羊）的 effect 是 CONTENTMENT。只給 cardId 的話前端無從得知它是樂不思蜀。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DelayScrollEvent {
    public static final String CONTENTMENT = "CONTENTMENT";
    public static final String LIGHTNING = "LIGHTNING";

    /** 實際的那張牌（前端畫牌面用） */
    private String cardId;
    /** 在判定區的效果：CONTENTMENT（樂不思蜀）/ LIGHTNING（閃電） */
    private String effect;
    /** 效果的中文名稱 */
    private String effectName;

    public static DelayScrollEvent of(ScrollCard card) {
        if (card instanceof Lightning) {
            return new DelayScrollEvent(card.getId(), LIGHTNING, "閃電");
        }
        if (card instanceof Contentment) {
            return new DelayScrollEvent(card.getId(), CONTENTMENT, "樂不思蜀");
        }
        throw new IllegalStateException("判定區不該出現的牌：" + card.getId() + " " + card.getName());
    }
}

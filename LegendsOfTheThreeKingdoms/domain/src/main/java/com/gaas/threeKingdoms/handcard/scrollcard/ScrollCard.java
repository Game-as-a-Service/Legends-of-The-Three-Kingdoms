package com.gaas.threeKingdoms.handcard.scrollcard;

import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;

public abstract class ScrollCard extends HandCard {
    public ScrollCard(PlayCard playCard) {
        super(playCard);
    }

    /** 延時錦囊（樂不思蜀、閃電）回傳 true；集智等「使用非延時錦囊」技能據此排除。 */
    public boolean isDelayScroll() {
        return false;
    }
}

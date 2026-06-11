package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.Suit;
import com.gaas.threeKingdoms.skill.trigger.CardConversionSkill;

/**
 * 甄姬 (WEI007) 傾國 — 可將一張黑色手牌當閃使用或打出（issue #170）。
 */
public class QingGuoSkill implements CardConversionSkill {

    public static final String GENERAL_ID = General.甄姬.getGeneralId();
    public static final String SKILL_NAME = "傾國";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public boolean canConvert(HandCard source, String as) {
        return AS_DODGE.equals(as)
                && (source.getSuit() == Suit.SPADE || source.getSuit() == Suit.CLUB);
    }
}

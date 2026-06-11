package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.Suit;
import com.gaas.threeKingdoms.skill.trigger.CardConversionSkill;

/**
 * 大喬 (WU006) 國色 — 可將一張方塊牌當樂不思蜀使用（issue #190）。
 */
public class GuoSeSkill implements CardConversionSkill {

    public static final String GENERAL_ID = General.大喬.getGeneralId();
    public static final String SKILL_NAME = "國色";

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
        return AS_CONTENTMENT.equals(as) && source.getSuit() == Suit.DIAMOND;
    }
}

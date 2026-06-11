package com.gaas.threeKingdoms.skill.shu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.Suit;
import com.gaas.threeKingdoms.skill.trigger.CardConversionSkill;

/**
 * 關羽 (SHU002) 武聖 — 可將一張紅色牌當殺使用或打出（issue #174）。
 * v1：紅色「手牌」（紅心/方塊）；裝備區紅牌轉化為 follow-up。
 */
public class WuShengSkill implements CardConversionSkill {

    public static final String GENERAL_ID = General.關羽.getGeneralId();
    public static final String SKILL_NAME = "武聖";

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
        return AS_KILL.equals(as)
                && (source.getSuit() == Suit.HEART || source.getSuit() == Suit.DIAMOND);
    }
}

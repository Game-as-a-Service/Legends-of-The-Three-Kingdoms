package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.Suit;
import com.gaas.threeKingdoms.skill.trigger.CardConversionSkill;

/**
 * 甘寧 (WU002) 奇襲 — 可將一張黑色牌當過河拆橋使用（issue #185）。
 * v1：黑色「手牌」；裝備區黑牌轉化為 follow-up。
 */
public class QiXiSkill implements CardConversionSkill {

    public static final String GENERAL_ID = General.甘寧.getGeneralId();
    public static final String SKILL_NAME = "奇襲";

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
        return AS_DISMANTLE.equals(as)
                && (source.getSuit() == Suit.SPADE || source.getSuit() == Suit.CLUB);
    }
}

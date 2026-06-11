package com.gaas.threeKingdoms.skill.shu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.skill.trigger.CardConversionSkill;

/**
 * 趙雲 (SHU005) 龍膽 — 可將殺當閃、閃當殺使用或打出（issue #178）。
 */
public class LongDanSkill implements CardConversionSkill {

    public static final String GENERAL_ID = General.趙雲.getGeneralId();
    public static final String SKILL_NAME = "龍膽";

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
        if (AS_DODGE.equals(as)) {
            return source instanceof Kill;
        }
        if (AS_KILL.equals(as)) {
            return source instanceof Dodge;
        }
        return false;
    }
}

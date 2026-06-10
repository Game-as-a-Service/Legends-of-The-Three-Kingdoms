package com.gaas.threeKingdoms.skill.shu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.skill.trigger.KillCountUnlimitedSkill;

/**
 * 張飛 (SHU003) 咆哮 — 鎖定技。出牌階段使用殺無次數限制（issue #175）。
 */
public class PaoXiaoSkill implements KillCountUnlimitedSkill {

    public static final String GENERAL_ID = General.張飛.getGeneralId();
    public static final String SKILL_NAME = "咆哮";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }
}

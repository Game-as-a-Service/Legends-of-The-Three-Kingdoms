package com.gaas.threeKingdoms.skill.shu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.skill.trigger.ScrollRangeUnlimitedSkill;

/**
 * 黃月英 (SHU007) 奇才 — 鎖定技。使用錦囊無距離限制（issue #182）。
 * 現行 codebase 唯一有距離限制的錦囊為順手牽羊。
 */
public class QiCaiSkill implements ScrollRangeUnlimitedSkill {

    public static final String GENERAL_ID = General.黃月英.getGeneralId();
    public static final String SKILL_NAME = "奇才";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }
}

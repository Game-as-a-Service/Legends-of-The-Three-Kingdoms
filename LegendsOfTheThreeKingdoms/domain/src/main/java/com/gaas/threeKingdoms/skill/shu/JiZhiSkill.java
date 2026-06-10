package com.gaas.threeKingdoms.skill.shu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.skill.trigger.AfterScrollUsedSkill;

/**
 * 黃月英 (SHU007) 集智 — 每當你使用錦囊牌時摸一張牌（issue #181）。
 * v1 自動觸發；「可」的選擇權（玩家拒絕摸牌）留 follow-up。
 */
public class JiZhiSkill implements AfterScrollUsedSkill {

    public static final String GENERAL_ID = General.黃月英.getGeneralId();
    public static final String SKILL_NAME = "集智";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public int drawCountAfterScrollUsed() {
        return 1;
    }
}

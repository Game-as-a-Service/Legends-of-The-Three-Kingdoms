package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.skill.Skill;

/**
 * 呂蒙 (WU003) 克己 — 棄牌階段開始時，若此回合未使用過殺，可略過棄牌階段（issue #186）。
 * v1 自動觸發（保留手牌嚴格有利，不詢問）；「使用過殺」以 Round.killPlayedThisTurn 判斷
 * （含轉化殺；諸葛連弩/咆哮重設 isShowKill 不影響）。
 * 觸發邏輯在 Game.getCurrentRoundPlayerDiscardCount（回 0 → finishAction 直接進下一回合）。
 */
public class KeJiSkill implements Skill {

    public static final String GENERAL_ID = General.呂蒙.getGeneralId();
    public static final String SKILL_NAME = "克己";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }
}

package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.skill.Skill;

/**
 * 甄姬 (WEI007) 洛神 — 回合開始階段可判定：黑色收為手牌並繼續判定，紅色結束（issue #171）。
 * v1 自動觸發（黑色全收，紅色停 — 嚴格有利不詢問）；
 * 觸發點在 Game.playerTakeTurnStartInJudgement（delay scroll 判定之前）。
 * Marker：實際 loop 邏輯在 SkillEngine.luoShenJudgementLoop。
 */
public class LuoShenSkill implements Skill {

    public static final String GENERAL_ID = General.甄姬.getGeneralId();
    public static final String SKILL_NAME = "洛神";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }
}

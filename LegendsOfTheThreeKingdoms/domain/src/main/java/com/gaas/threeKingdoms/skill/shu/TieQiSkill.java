package com.gaas.threeKingdoms.skill.shu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.skill.Skill;

/**
 * 馬超 (SHU006) 鐵騎 — 指定殺的目標後可判定：非紅桃則目標不能出閃（issue #180）。
 * v1 自動判定（嚴格有利不詢問）；觸發點在 NormalActiveKillBehavior 的
 * AskDodge 之前（SkillEngine.tieQiJudgement）：判定生效時跳過 AskDodge，直接結算傷害。
 */
public class TieQiSkill implements Skill {

    public static final String GENERAL_ID = General.馬超.getGeneralId();
    public static final String SKILL_NAME = "鐵騎";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }
}

package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.skill.trigger.AfterLoseLastHandCardSkill;

/**
 * 陸遜 (WU007) 連營 — 鎖定技。當你失去最後一張手牌時，摸一張牌（issue #193）。
 * v1 覆蓋範圍：自己出牌（含 response 出閃/殺）與棄牌階段棄光手牌；
 * 被拆/被順走最後一張手牌的路徑列 follow-up。
 */
public class LianYingSkill implements AfterLoseLastHandCardSkill {

    public static final String GENERAL_ID = General.陸遜.getGeneralId();
    public static final String SKILL_NAME = "連營";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public int drawCountAfterLoseLastHandCard() {
        return 1;
    }
}

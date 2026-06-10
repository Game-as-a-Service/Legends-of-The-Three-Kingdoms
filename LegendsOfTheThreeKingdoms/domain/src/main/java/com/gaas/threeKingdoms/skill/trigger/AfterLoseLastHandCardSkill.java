package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.skill.Skill;

/**
 * 失去最後一張手牌時觸發的鎖定技（例：連營 — 摸一張）。
 */
public interface AfterLoseLastHandCardSkill extends Skill {
    int drawCountAfterLoseLastHandCard();
}

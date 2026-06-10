package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.skill.Skill;

/**
 * 使用錦囊牌後觸發的技能（例：集智 — 摸一張）。Marker interface；
 * v1 自動觸發（「可」的選擇權留 follow-up）。
 */
public interface AfterScrollUsedSkill extends Skill {
    int drawCountAfterScrollUsed();
}

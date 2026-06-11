package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.skill.Skill;

/**
 * 失去裝備牌後觸發（例：梟姬 — 摸 2）。
 */
public interface AfterLoseEquipmentSkill extends Skill {
    int drawCountAfterLoseEquipment();
}

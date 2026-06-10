package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.skill.Skill;

/**
 * 修正摸牌階段抽牌數的鎖定技（例：英姿 +1、裸衣 -1）。
 */
public interface DrawPhaseDeltaSkill extends Skill {
    int drawCardDelta();
}

package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;

/**
 * 覆寫手牌上限的鎖定技（例：英姿 = max(HP, 4)）。
 */
public interface HandLimitSkill extends Skill {
    int handLimit(Player self);
}

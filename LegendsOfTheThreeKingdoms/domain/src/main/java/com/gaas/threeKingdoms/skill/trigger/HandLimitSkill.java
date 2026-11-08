package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;

/**
 * 覆寫手牌上限的鎖定技（預留擴充點，目前沒有武將使用；標準版英姿不改手牌上限）。
 */
public interface HandLimitSkill extends Skill {
    int handLimit(Player self);
}

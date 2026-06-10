package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.skill.Skill;

/**
 * 修正「自己計算與其他角色距離」的鎖定技（例：馬術 -1）。
 * 負值代表距離縮短；套用於攻擊範圍與順手牽羊範圍計算。
 */
public interface DistanceDeltaSkill extends Skill {
    int distanceDeltaToOthers();
}

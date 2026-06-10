package com.gaas.threeKingdoms.skill.shu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.skill.trigger.DistanceDeltaSkill;

/**
 * 馬超 (SHU006) 馬術 — 鎖定技。計算你與其他角色距離時 -1。
 * 套用於攻擊範圍與順手牽羊範圍（issue #179）。
 */
public class MaShuSkill implements DistanceDeltaSkill {

    public static final String GENERAL_ID = General.馬超.getGeneralId();
    public static final String SKILL_NAME = "馬術";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public int distanceDeltaToOthers() {
        return -1;
    }
}

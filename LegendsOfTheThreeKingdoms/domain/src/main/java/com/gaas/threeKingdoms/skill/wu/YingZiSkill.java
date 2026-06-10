package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.DrawPhaseDeltaSkill;
import com.gaas.threeKingdoms.skill.trigger.HandLimitSkill;

/**
 * 周瑜 (WU005) 英姿 — 鎖定技。摸牌階段多摸一張；手牌上限為 max(HP, 4)（issue #188）。
 */
public class YingZiSkill implements DrawPhaseDeltaSkill, HandLimitSkill {

    public static final String GENERAL_ID = General.周瑜.getGeneralId();
    public static final String SKILL_NAME = "英姿";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public int drawCardDelta() {
        return 1;
    }

    @Override
    public int handLimit(Player self) {
        return Math.max(self.getHP(), 4);
    }
}

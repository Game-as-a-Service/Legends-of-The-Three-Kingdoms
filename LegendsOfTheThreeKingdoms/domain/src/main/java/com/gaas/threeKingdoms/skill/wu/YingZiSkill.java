package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.DrawPhaseDeltaSkill;

/**
 * 周瑜 (WU005) 英姿（身份標準版）— 摸牌階段多摸一張牌（issue #188）。
 * 標準版英姿不改手牌上限：手牌上限仍等於當前體力。
 * （issue #188 原寫「手牌上限 = max(HP, 4)」，不符合任何官方版本，已移除。）
 */
public class YingZiSkill implements DrawPhaseDeltaSkill {

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
    public int drawCardDelta(com.gaas.threeKingdoms.Game game, Player player) {
        return 1;
    }
}

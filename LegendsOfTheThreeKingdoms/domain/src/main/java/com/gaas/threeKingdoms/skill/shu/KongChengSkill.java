package com.gaas.threeKingdoms.skill.shu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.TargetImmunitySkill;

/**
 * 諸葛亮 (SHU004) 空城 — 鎖定技。手牌數為 0 時，他人無法以殺或決鬥指定你為目標（issue #177）。
 */
public class KongChengSkill implements TargetImmunitySkill {

    public static final String GENERAL_ID = General.諸葛亮.getGeneralId();
    public static final String SKILL_NAME = "空城";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public boolean isImmune(Player self, HandCard card) {
        return self.getHandSize() == 0 && (card instanceof Kill || card instanceof Duel);
    }
}

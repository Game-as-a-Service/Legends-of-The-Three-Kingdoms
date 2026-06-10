package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.DamageBoostSkill;
import com.gaas.threeKingdoms.skill.trigger.DrawPhaseDeltaSkill;

/**
 * 許褚 (WEI005) 裸衣 — 鎖定技。摸牌階段少摸一張；此回合使用殺與決鬥造成的傷害 +1（issue #167）。
 *
 * 「此回合」= 許褚自己的行動回合（attacker == currentRoundPlayer 時生效）。
 * Kill 含 VirtualKill（丈八蛇矛等武器虛擬殺繼承 Kill）。
 */
public class LuoYiSkill implements DrawPhaseDeltaSkill, DamageBoostSkill {

    public static final String GENERAL_ID = General.許褚.getGeneralId();
    public static final String SKILL_NAME = "裸衣";

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
        return -1;
    }

    @Override
    public int extraDamage(Game game, Player attacker, HandCard sourceCard) {
        boolean isOwnRound = attacker != null
                && game.getCurrentRound() != null
                && attacker.equals(game.getCurrentRound().getCurrentRoundPlayer());
        boolean isKillOrDuel = sourceCard instanceof Kill || sourceCard instanceof Duel;
        return (isOwnRound && isKillOrDuel) ? 1 : 0;
    }
}

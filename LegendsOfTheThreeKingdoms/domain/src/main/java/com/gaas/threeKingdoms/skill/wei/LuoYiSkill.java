package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;
import com.gaas.threeKingdoms.skill.trigger.DamageBoostSkill;
import com.gaas.threeKingdoms.skill.trigger.DrawPhaseDeltaSkill;

import java.util.ArrayList;
import java.util.List;

/**
 * 許褚 (WEI005) 裸衣 — 摸牌階段可少摸一張；此回合使用殺與決鬥造成的傷害 +1（issue #167）。
 *
 * 主動觸發（使用者指示，原為自動生效）：回合開始判定階段之後、摸牌之前，
 * 詢問許褚是否發動（AskSkillEffectEvent + useSkillEffect ACCEPT/SKIP；mirror 洛神 #227）。
 * ACCEPT → 當回合旗標記入 Round.usedOncePerTurnSkills（可持久化），摸牌 -1、殺/決鬥傷害 +1；
 * SKIP → 正常摸牌，無加成。
 *
 * 「此回合」= 許褚自己的行動回合（attacker == currentRoundPlayer 時生效）。
 * Kill 含 VirtualKill（丈八蛇矛等武器虛擬殺繼承 Kill）。
 */
public class LuoYiSkill implements DrawPhaseDeltaSkill, DamageBoostSkill, ChoiceResolvableSkill {

    public static final String GENERAL_ID = General.許褚.getGeneralId();
    public static final String SKILL_NAME = "裸衣";
    public static final String PARAM_CONTENTMENT_SUCCESS = "LUOYI_CONTENTMENT_SUCCESS";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    private static boolean isActivatedThisTurn(Game game) {
        return game.getCurrentRound() != null
                && game.getCurrentRound().getUsedOncePerTurnSkills().contains(SKILL_NAME);
    }

    @Override
    public int drawCardDelta(Game game, Player player) {
        return isActivatedThisTurn(game) ? -1 : 0;
    }

    @Override
    public int extraDamage(Game game, Player attacker, HandCard sourceCard) {
        if (!isActivatedThisTurn(game)) {
            return 0;
        }
        boolean isOwnRound = attacker != null
                && game.getCurrentRound() != null
                && attacker.equals(game.getCurrentRound().getCurrentRoundPlayer());
        boolean isKillOrDuel = sourceCard instanceof Kill || sourceCard instanceof Duel;
        return (isOwnRound && isKillOrDuel) ? 1 : 0;
    }

    @Override
    public List<DomainEvent> resolveChoice(Game game, WaitingSkillEffectBehavior waiting,
                                           String choice, List<String> cardIds, String targetPlayerId) {
        Player xuChu = waiting.getBehaviorPlayer();
        boolean contentmentSuccess = Boolean.parseBoolean(
                (String) waiting.getParam(PARAM_CONTENTMENT_SUCCESS));
        List<DomainEvent> events = new ArrayList<>();

        // 先 pop 本 waiting，接續的摸牌流程需要乾淨 stack
        waiting.setIsOneRound(true);
        game.removeCompletedBehaviors();

        if ("ACCEPT".equals(choice)) {
            game.getCurrentRound().getUsedOncePerTurnSkills().add(SKILL_NAME);
            events.add(new SkillEffectEvent(SKILL_NAME, xuChu.getId(), true, List.of(), null));
            events.add(game.getGameStatusEvent(
                    xuChu.getId() + " 發動裸衣：少摸一張，本回合殺與決鬥傷害 +1"));
        } else if ("SKIP".equals(choice)) {
            events.add(new SkillEffectEvent(SKILL_NAME, xuChu.getId(), false, List.of(), null));
            events.add(game.getGameStatusEvent("放棄裸衣"));
        } else {
            throw new IllegalArgumentException("Invalid LuoYi choice: " + choice);
        }

        game.getCurrentRound().setActivePlayer(xuChu);
        events.addAll(game.proceedDrawPhase(xuChu, contentmentSuccess));
        return events;
    }
}

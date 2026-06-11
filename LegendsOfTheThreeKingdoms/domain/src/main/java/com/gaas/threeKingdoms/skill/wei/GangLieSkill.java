package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.JianXiongCompatibleTopBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.Suit;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.context.DamageContext;
import com.gaas.threeKingdoms.skill.registry.SkillEngine;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;
import com.gaas.threeKingdoms.skill.trigger.OnDamagedSkill;

import java.util.ArrayList;
import java.util.List;

/**
 * 夏侯惇 (WEI003) 剛烈 — 受到傷害後可判定：非紅桃 → 傷害來源選擇「棄兩張手牌」或「受 1 點傷害」（issue #165）。
 *
 * 兩階段：
 *   1. 夏侯惇 ACCEPT → 立即判定；紅桃 → 結束；非紅桃 → push 第二個 WaitingSkillEffect 問來源
 *   2. 來源 choice "DISCARD" + cardIds(2 張手牌) 或 "DAMAGE"（受 1 傷）
 *
 * v1 範圍：非 AOE polling 中觸發；來源手牌 < 2 時只能選 DAMAGE。
 */
public class GangLieSkill implements OnDamagedSkill, ChoiceResolvableSkill {

    public static final String GENERAL_ID = General.夏侯惇.getGeneralId();
    public static final String SKILL_NAME = "剛烈";
    public static final String PARAM_SOURCE_ID = "GANGLIE_SOURCE_ID";
    public static final String PARAM_STAGE = "GANGLIE_STAGE"; // ASK_XIAHOU | ASK_SOURCE
    public static final String PARAM_XIAHOU_ID = "GANGLIE_XIAHOU_ID";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public List<DomainEvent> onDamaged(Game game, DamageContext ctx) {
        Player damaged = ctx.damagedPlayer();
        Player source = ctx.sourcePlayer();
        if (source == null || source.equals(damaged)) {
            return List.of();
        }
        if (!damaged.isHPGreaterThanZero()) {
            return List.of();
        }
        Behavior top = game.isTopBehaviorEmpty() ? null : game.peekTopBehavior();
        if (top != null && !(top instanceof JianXiongCompatibleTopBehavior compatible
                && !compatible.isPollingCaller())) {
            return List.of();
        }

        game.removeCompletedBehaviors();
        WaitingSkillEffectBehavior waiting = new WaitingSkillEffectBehavior(game, damaged, SKILL_NAME);
        waiting.putParam(PARAM_SOURCE_ID, source.getId());
        waiting.putParam(PARAM_STAGE, "ASK_XIAHOU");
        game.updateTopBehavior(waiting);
        game.getCurrentRound().setActivePlayer(damaged);

        return List.of(new AskSkillEffectEvent(SKILL_NAME, damaged.getId(), List.of(), source.getId()));
    }

    @Override
    public List<DomainEvent> resolveChoice(Game game, WaitingSkillEffectBehavior waiting,
                                           String choice, List<String> cardIds, String targetPlayerId) {
        String stage = (String) waiting.getParam(PARAM_STAGE);
        if ("ASK_SOURCE".equals(stage)) {
            return resolveSourceChoice(game, waiting, choice, cardIds);
        }
        return resolveXiaHouChoice(game, waiting, choice);
    }

    private List<DomainEvent> resolveXiaHouChoice(Game game, WaitingSkillEffectBehavior waiting, String choice) {
        Player xiaHou = waiting.getBehaviorPlayer();
        String sourceId = (String) waiting.getParam(PARAM_SOURCE_ID);
        List<DomainEvent> events = new ArrayList<>();

        if ("SKIP".equals(choice)) {
            game.getCurrentRound().setActivePlayer(game.getCurrentRound().getCurrentRoundPlayer());
            events.add(new SkillEffectEvent(SKILL_NAME, xiaHou.getId(), false, List.of(), sourceId));
            events.add(game.getGameStatusEvent("放棄剛烈"));
            return events;
        }
        if (!"ACCEPT".equals(choice)) {
            throw new IllegalArgumentException("Invalid GangLie choice: " + choice);
        }

        // 判定
        HandCard judgement = game.drawCardForCardEffect(1).get(0);
        boolean success = judgement.getSuit() != Suit.HEART;
        events.add(new SkillEffectEvent(SKILL_NAME, xiaHou.getId(), success,
                List.of(judgement.getId()), sourceId));
        events.addAll(SkillEngine.afterJudgement(game, xiaHou, judgement));

        if (!success) {
            game.getCurrentRound().setActivePlayer(game.getCurrentRound().getCurrentRoundPlayer());
            events.add(game.getGameStatusEvent("剛烈判定紅桃，未生效"));
            return events;
        }

        // 判定生效 → 問來源：棄兩張手牌或受 1 傷
        Player source = game.getPlayer(sourceId);
        WaitingSkillEffectBehavior askSource = new WaitingSkillEffectBehavior(game, source, SKILL_NAME);
        askSource.putParam(PARAM_STAGE, "ASK_SOURCE");
        askSource.putParam(PARAM_SOURCE_ID, sourceId);
        askSource.putParam(PARAM_XIAHOU_ID, xiaHou.getId());
        game.updateTopBehavior(askSource);
        game.getCurrentRound().setActivePlayer(source);

        events.add(new AskSkillEffectEvent(SKILL_NAME, sourceId, List.of(), xiaHou.getId()));
        events.add(game.getGameStatusEvent("剛烈判定生效，" + sourceId + " 選擇棄兩張手牌或受 1 點傷害"));
        return events;
    }

    private List<DomainEvent> resolveSourceChoice(Game game, WaitingSkillEffectBehavior waiting,
                                                  String choice, List<String> cardIds) {
        Player source = waiting.getBehaviorPlayer();
        String xiaHouId = (String) waiting.getParam(PARAM_XIAHOU_ID);
        List<DomainEvent> events = new ArrayList<>();
        game.getCurrentRound().setActivePlayer(game.getCurrentRound().getCurrentRoundPlayer());

        if ("DISCARD".equals(choice)) {
            if (cardIds == null || cardIds.size() != 2) {
                throw new IllegalArgumentException("DISCARD requires exactly 2 hand cards");
            }
            for (String cardId : cardIds) {
                HandCard discarded = source.playCard(cardId);
                game.getGraveyard().add(discarded);
            }
            events.add(new SkillEffectEvent(SKILL_NAME, source.getId(), true, cardIds, xiaHouId));
            events.add(game.getGameStatusEvent(source.getId() + " 棄兩張手牌回應剛烈"));
        } else if ("DAMAGE".equals(choice)) {
            int originalHp = source.getHP();
            source.damage(1);
            events.add(new SkillEffectEvent(SKILL_NAME, source.getId(), true, List.of(), xiaHouId));
            events.add(game.getGameStatusEvent(source.getId() + " 受剛烈 1 點傷害（" + originalHp + "→" + source.getHP() + "）"));
            // v1：剛烈反傷不進瀕死流程整合（HP 仍會歸零，但 dying ask 流程為 follow-up）
        } else {
            throw new IllegalArgumentException("Invalid GangLie source choice: " + choice);
        }
        return events;
    }
}

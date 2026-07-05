package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.BeforeAskDodgeSkill;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 大喬 (WU006) 流離 — 成為殺的目標時，可棄一張牌將該殺轉移給距離 1 以內的其他角色
 * （攻擊者除外）（issue #191）。
 *
 * 攔截 AskDodge（mirror 護駕）：push WaitingSkillEffect(流離) 問大喬；
 *   ACCEPT：cardIds[0] = 棄的手牌、targetPlayerId = 新目標 →
 *           棄牌、parent NormalActiveKillBehavior.redirectTo(新目標)
 *   SKIP：fallback AskDodgeEvent(大喬)
 *
 * 「距離 1 以內」採順手牽羊同款公式（座位距離 - 大喬-1馬 + 對方+1馬 ≤ 1）。
 * v1 範圍：普通殺（NormalActiveKillBehavior 精確類別）；方天畫戟/AOE 轉移 follow-up；
 * 棄牌限手牌（裝備 follow-up）。
 */
public class LiuLiSkill implements BeforeAskDodgeSkill, ChoiceResolvableSkill {

    public static final String GENERAL_ID = General.大喬.getGeneralId();
    public static final String SKILL_NAME = "流離";
    public static final String PARAM_ATTACKER_ID = "LIULI_ATTACKER_ID";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public Optional<List<DomainEvent>> beforeAskDodge(Game game, Player damaged, Behavior parentBehavior) {
        if (parentBehavior == null || parentBehavior.getClass() != NormalActiveKillBehavior.class) {
            return Optional.empty(); // v1：只攔普通殺
        }
        if (damaged.getHandSize() == 0) {
            return Optional.empty(); // 沒牌可棄
        }
        Player attacker = parentBehavior.getBehaviorPlayer();
        boolean hasRedirectTarget = game.getPlayers().stream()
                .anyMatch(p -> isValidRedirectTarget(game, damaged, attacker, p));
        if (!hasRedirectTarget) {
            return Optional.empty();
        }

        WaitingSkillEffectBehavior waiting = new WaitingSkillEffectBehavior(game, damaged, SKILL_NAME);
        waiting.putParam(PARAM_ATTACKER_ID, attacker.getId());
        game.updateTopBehavior(waiting);
        game.getCurrentRound().setActivePlayer(damaged);
        return Optional.of(List.of(new AskSkillEffectEvent(SKILL_NAME, damaged.getId(), List.of(), attacker.getId())));
    }

    @Override
    public List<DomainEvent> resolveChoice(Game game, WaitingSkillEffectBehavior waiting,
                                           String choice, List<String> cardIds, String targetPlayerId) {
        Player daQiao = waiting.getBehaviorPlayer();
        String attackerId = (String) waiting.getParam(PARAM_ATTACKER_ID);

        if ("SKIP".equals(choice)) {
            // fallback：大喬自己面對這張殺
            return List.of(new SkillEffectEvent(SKILL_NAME, daQiao.getId(), false, List.of(), attackerId),
                    new AskDodgeEvent(daQiao.getId()));
        }
        if (!"ACCEPT".equals(choice)) {
            throw new IllegalArgumentException("Invalid LiuLi choice: " + choice);
        }
        if (cardIds == null || cardIds.size() != 1) {
            throw new IllegalArgumentException("流離 requires exactly 1 card to discard");
        }
        Player attacker = game.getPlayer(attackerId);
        Player newTarget = game.getPlayer(targetPlayerId);
        if (!isValidRedirectTarget(game, daQiao, attacker, newTarget)) {
            throw new IllegalArgumentException("Invalid redirect target: " + targetPlayerId);
        }

        HandCard discarded = daQiao.playCard(cardIds.get(0));
        game.getGraveyard().add(discarded);

        // waiting 已由 Game 設 isOneRound=true 後 pop；redirect 在 parent 上進行
        Behavior parent = game.peekTopBehaviorSecondElement()
                .orElseThrow(() -> new IllegalStateException("LiuLi parent behavior missing"));
        if (!(parent instanceof NormalActiveKillBehavior killBehavior)) {
            throw new IllegalStateException("LiuLi parent is not NormalActiveKillBehavior");
        }

        List<DomainEvent> events = new ArrayList<>();
        events.add(new SkillEffectEvent(SKILL_NAME, daQiao.getId(), true, cardIds, targetPlayerId));
        events.add(game.getGameStatusEvent(String.format(
                "%s 發動流離，殺轉移給 %s", daQiao.getId(), targetPlayerId)));
        events.addAll(killBehavior.redirectTo(newTarget));
        return events;
    }

    /** 距離 1 以內（順手牽羊公式）、非攻擊者、非大喬本人、存活。 */
    private boolean isValidRedirectTarget(Game game, Player daQiao, Player attacker, Player candidate) {
        if (candidate.equals(daQiao) || candidate.equals(attacker) || candidate.isAlreadyDeath()) {
            return false;
        }
        return game.isInSnatchEffectRange(daQiao, candidate);
    }
}

package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
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
 * 適用所有殺：普通殺、轉化殺（武聖/龍膽）、丈八蛇矛、方天畫戟（不能轉給已是目標的角色）；
 * AOE 不適用（不是殺）。棄牌限手牌（裝備 follow-up）。
 */
public class LiuLiSkill implements ChoiceResolvableSkill {

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

    /**
     * 成為殺的目標時（由 NormalActiveKillBehavior 在鐵騎、雙股劍、防具之前呼叫）詢問大喬是否發動流離。
     * 條件：目標是大喬、手上有牌可棄、攻擊範圍內有可轉移的其他角色（非攻擊者，方天畫戟另排除已有目標）。
     */
    public static Optional<List<DomainEvent>> askIfApplicable(Game game, Player damaged, NormalActiveKillBehavior parentBehavior) {
        boolean hasLiuLi = com.gaas.threeKingdoms.skill.registry.SkillRegistry
                .of(damaged.getGeneralCard().getGeneralId()).stream().anyMatch(sk -> sk instanceof LiuLiSkill);
        if (!hasLiuLi) {
            return Optional.empty();
        }
        if (parentBehavior == null) {
            return Optional.empty();
        }
        if (damaged.getHandSize() == 0) {
            return Optional.empty(); // 沒牌可棄
        }
        Player attacker = parentBehavior.getBehaviorPlayer();
        boolean hasRedirectTarget = game.getPlayers().stream()
                .anyMatch(p -> isValidRedirectTarget(game, damaged, attacker, p, parentBehavior));
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
            // 大喬自己承受這張殺：接回殺的流程（鐵騎 → 雌雄雙股劍 → 防具／閃），不是直接問閃
            NormalActiveKillBehavior kill = popWaitingAndFindKill(game, waiting);
            List<DomainEvent> events = new ArrayList<>();
            events.add(new SkillEffectEvent(SKILL_NAME, daQiao.getId(), false, List.of(), attackerId));
            events.addAll(kill.continueAfterLiuLiDeclined(daQiao));
            return events;
        }
        if (!"ACCEPT".equals(choice)) {
            throw new IllegalArgumentException("Invalid LiuLi choice: " + choice);
        }
        if (cardIds == null || cardIds.size() != 1) {
            throw new IllegalArgumentException("流離 requires exactly 1 card to discard");
        }
        Player attacker = game.getPlayer(attackerId);
        Player newTarget = game.getPlayer(targetPlayerId);
        if (newTarget == null
                || !isValidRedirectTarget(game, daQiao, attacker, newTarget, NormalActiveKillBehavior.findIn(game))) {
            throw new IllegalArgumentException("Invalid redirect target: " + targetPlayerId);
        }

        HandCard discarded = daQiao.playCard(cardIds.get(0));
        game.getGraveyard().add(discarded);

        // 先 pop 本 waiting，redirect 在底下的殺上進行（鬼才 resume 等後續流程預期殺在 top）
        NormalActiveKillBehavior killBehavior = popWaitingAndFindKill(game, waiting);

        List<DomainEvent> events = new ArrayList<>();
        events.add(new SkillEffectEvent(SKILL_NAME, daQiao.getId(), true, cardIds, targetPlayerId));
        events.add(game.getGameStatusEvent(String.format(
                "%s 發動流離，殺轉移給 %s", daQiao.getId(), targetPlayerId)));
        events.addAll(killBehavior.redirectTo(newTarget));
        return events;
    }

    private static NormalActiveKillBehavior popWaitingAndFindKill(Game game, WaitingSkillEffectBehavior waiting) {
        waiting.setIsOneRound(true);
        game.removeCompletedBehaviors();
        return NormalActiveKillBehavior.findIn(game);
    }

    /** 距離 1 以內（順手牽羊公式）、非攻擊者、非大喬本人、存活、且這張殺允許（方天畫戟：不能是已有目標）。 */
    private static boolean isValidRedirectTarget(Game game, Player daQiao, Player attacker, Player candidate,
                                                 NormalActiveKillBehavior kill) {
        if (candidate.equals(daQiao) || candidate.equals(attacker) || candidate.isAlreadyDeath()) {
            return false;
        }
        if (!kill.canLiuLiRedirectTo(candidate)) {
            return false;
        }
        return game.isInSnatchEffectRange(daQiao, candidate);
    }
}

package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 張遼 (WEI004) 突襲（身份標準版）— 摸牌階段，你可以放棄摸牌，改為獲得最多兩名其他角色的各一張手牌。
 * <p>
 * 判定階段之後、摸牌之前由 {@code SkillEngine#tuXiAskActivation} 詢問（AskSkillEffectEvent，
 * options = ACCEPT / SKIP）。
 * <ul>
 *   <li>ACCEPT：cardIds = 目標玩家 id（1~2 名、不重複、不可含自己、需有手牌）→ 各拿一張，<b>不摸牌</b></li>
 *   <li>SKIP：照常摸牌</li>
 * </ul>
 * FAQ：可以只選一名；不能從同一名角色拿 2 張；發動就不能從牌堆摸牌；沒有任何有手牌的角色時不能發動（不詢問）。
 * 手牌為隱藏資訊，取目標第一張手牌等同隨機抽取。
 */
public class TuXiSkill implements ChoiceResolvableSkill {

    public static final String GENERAL_ID = General.張遼.getGeneralId();
    public static final String SKILL_NAME = "突襲";
    public static final String PARAM_CONTENTMENT_SUCCESS = "TUXI_CONTENTMENT_SUCCESS";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public List<DomainEvent> resolveChoice(Game game, WaitingSkillEffectBehavior waiting,
                                           String choice, List<String> cardIds, String targetPlayerId) {
        Player zhangLiao = waiting.getBehaviorPlayer();
        boolean contentmentSuccess = Boolean.parseBoolean((String) waiting.getParam(PARAM_CONTENTMENT_SUCCESS));

        if ("SKIP".equals(choice)) {
            popWaiting(game, waiting);
            List<DomainEvent> events = new ArrayList<>();
            events.add(new SkillEffectEvent(SKILL_NAME, zhangLiao.getId(), false, List.of(), null,
                    String.format("突襲：%s 不發動，照常摸牌", zhangLiao.getGeneralName())));
            game.getCurrentRound().setActivePlayer(zhangLiao);
            events.addAll(game.proceedDrawPhase(zhangLiao, contentmentSuccess));
            return events;
        }
        if (!"ACCEPT".equals(choice)) {
            throw new IllegalArgumentException("突襲 choice must be ACCEPT or SKIP, got: " + choice);
        }

        // 先驗證再改狀態：不合法時 waiting 保留，張遼可重新選擇
        List<String> targetIds = cardIds; // 突襲的 cardIds 充當目標玩家 id 列表
        if (targetIds == null || targetIds.isEmpty() || targetIds.size() > 2) {
            throw new IllegalArgumentException("突襲 targets must be 1~2 players (via cardIds)");
        }
        if (new HashSet<>(targetIds).size() != targetIds.size()) {
            throw new IllegalArgumentException("突襲 不能從同一名角色獲得兩張牌");
        }
        List<Player> targets = new ArrayList<>();
        for (String tid : targetIds) {
            if (tid.equals(zhangLiao.getId())) {
                throw new IllegalArgumentException("Cannot target self");
            }
            Player target = game.getPlayer(tid);
            if (!game.getSeatingChart().getPlayers().contains(target)) {
                throw new IllegalArgumentException(tid + " is not alive");
            }
            if (target.getHandSize() == 0) {
                throw new IllegalArgumentException(tid + " has no hand card");
            }
            targets.add(target);
        }

        popWaiting(game, waiting);
        List<String> takenCardIds = new ArrayList<>();
        for (Player target : targets) {
            HandCard taken = target.getHand().getCards().get(0);
            target.playCard(taken.getId());
            zhangLiao.getHand().addCardToHand(taken);
            takenCardIds.add(taken.getId());
        }
        String message = String.format("突襲：%s 放棄摸牌，獲得 %s 各一張手牌", zhangLiao.getGeneralName(),
                targets.stream().map(Player::getGeneralName).collect(Collectors.joining("、")));
        List<DomainEvent> events = new ArrayList<>();
        events.add(new SkillEffectEvent(SKILL_NAME, zhangLiao.getId(), true, takenCardIds,
                String.join(",", targetIds), message));
        game.getCurrentRound().setActivePlayer(zhangLiao);
        events.addAll(game.skipDrawPhase(zhangLiao, contentmentSuccess, message));
        return events;
    }

    private static void popWaiting(Game game, WaitingSkillEffectBehavior waiting) {
        // 先 pop 本 waiting：接續的摸牌／出牌流程需要乾淨的 stack（同裸衣）
        waiting.setIsOneRound(true);
        game.removeCompletedBehaviors();
    }
}

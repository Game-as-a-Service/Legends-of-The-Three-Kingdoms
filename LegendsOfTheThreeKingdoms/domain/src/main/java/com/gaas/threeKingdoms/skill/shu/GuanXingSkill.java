package com.gaas.threeKingdoms.skill.shu;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;
import com.gaas.threeKingdoms.skill.registry.SkillRegistry;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 諸葛亮 (SHU004) 觀星 — 回合開始階段（準備階段，判定之前），觀看牌堆頂 X 張牌
 * （X = 存活角色數，上限 5），任意重排放回牌堆頂或底（issue #176）。
 *
 * 流程：
 *   1. 諸葛亮回合開始 → {@link #askAtTurnStart} 翻出 X 張（id 記在 waiting params，
 *      經 BehaviorData 泛用 params 還原），push WaitingSkillEffect(觀星)、
 *      AskSkillEffectEvent(dataCardIds = 觀看的牌, options = ARRANGE / SKIP)，回合流程暫停。
 *   2. choice = "ARRANGE"，cardIds = 放回牌堆頂的順序（第一張 = 下一張被抽）；
 *      未列出的牌放牌堆底（依原順序）。choice = "SKIP"：不發動，牌堆不變。
 *   3. 之後接續回合開始流程（判定 → 摸牌）。
 */
public class GuanXingSkill implements ChoiceResolvableSkill {

    public static final String GENERAL_ID = General.諸葛亮.getGeneralId();
    public static final String SKILL_NAME = "觀星";
    public static final String PARAM_PEEKED_CARD_IDS = "GUANXING_PEEKED_CARD_IDS";
    public static final String CHOICE_ARRANGE = "ARRANGE";
    public static final String CHOICE_SKIP = "SKIP";
    public static final List<String> OPTIONS = List.of(CHOICE_ARRANGE, CHOICE_SKIP);

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    /**
     * 回合開始（判定之前）詢問諸葛亮觀星。非諸葛亮或牌堆無牌 → 回 empty list（不暫停）。
     * 有詢問時 push WaitingSkillEffect(觀星)，caller 見 topBehavior 為 WaitingSkillEffectBehavior 即暫停。
     */
    public static List<DomainEvent> askAtTurnStart(Game game, Player roundPlayer) {
        boolean hasGuanXing = SkillRegistry.of(roundPlayer.getGeneralCard().getGeneralId()).stream()
                .anyMatch(s -> s instanceof GuanXingSkill);
        if (!hasGuanXing) {
            return List.of();
        }
        long aliveCount = game.getPlayers().stream().filter(p -> !p.isAlreadyDeath()).count();
        int n = (int) Math.min(5, Math.min(aliveCount, game.getDeck().size()));
        if (n == 0) {
            return List.of();
        }
        List<HandCard> peeked = game.getDeck().deal(n);
        List<String> peekedIds = peeked.stream().map(HandCard::getId).toList();
        // 牌先放回堆頂（玩家未重排前狀態不變），id 順序記在 params
        game.getDeck().putBackOnTop(peeked);

        WaitingSkillEffectBehavior waiting = new WaitingSkillEffectBehavior(game, roundPlayer, SKILL_NAME);
        waiting.putParam(PARAM_PEEKED_CARD_IDS, new ArrayList<>(peekedIds));
        game.updateTopBehavior(waiting);
        game.getCurrentRound().setActivePlayer(roundPlayer);

        String message = String.format("觀星：%s 回合開始，觀看牌堆頂 %d 張並排列（ARRANGE）或不發動（SKIP）",
                roundPlayer.getId(), n);
        return List.of(
                new AskSkillEffectEvent(SKILL_NAME, roundPlayer.getId(), peekedIds, null, OPTIONS, message),
                game.getGameStatusEvent(message));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DomainEvent> resolveChoice(Game game, WaitingSkillEffectBehavior waiting,
                                           String choice, List<String> cardIds, String targetPlayerId) {
        Player self = waiting.getBehaviorPlayer();
        List<DomainEvent> events = new ArrayList<>();
        if (CHOICE_ARRANGE.equals(choice)) {
            List<String> peekedIds = (List<String>) waiting.getParam(PARAM_PEEKED_CARD_IDS);
            List<String> topOrder = cardIds == null ? List.of() : cardIds;
            Set<String> peekedSet = new HashSet<>(peekedIds);
            if (!peekedSet.containsAll(topOrder) || new HashSet<>(topOrder).size() != topOrder.size()) {
                throw new IllegalArgumentException("ARRANGE cardIds must be a distinct subset of peeked cards");
            }

            // 取出 X 張（仍在堆頂），重排
            List<HandCard> peeked = game.getDeck().deal(peekedIds.size());
            List<HandCard> top = new ArrayList<>();
            List<HandCard> bottom = new ArrayList<>();
            for (String id : topOrder) {
                peeked.stream().filter(c -> c.getId().equals(id)).findFirst().ifPresent(top::add);
            }
            for (HandCard card : peeked) {
                if (!topOrder.contains(card.getId())) {
                    bottom.add(card);
                }
            }
            game.getDeck().putBackOnTop(top);
            game.getDeck().putAtBottom(bottom);
            events.add(new SkillEffectEvent(SKILL_NAME, self.getId(), true, topOrder, null));
            events.add(game.getGameStatusEvent(String.format("%s 觀星完成：%d 張回堆頂、%d 張置堆底",
                    self.getId(), top.size(), bottom.size())));
        } else if (CHOICE_SKIP.equals(choice)) {
            events.add(new SkillEffectEvent(SKILL_NAME, self.getId(), false, List.of(), null));
            events.add(game.getGameStatusEvent(self.getId() + " 放棄觀星"));
        } else {
            throw new IllegalArgumentException("觀星 choice must be ARRANGE or SKIP");
        }

        // 先 pop 本 waiting：後續回合開始流程（洛神詢問 / 判定 / 摸牌）需要乾淨的 stack
        waiting.setIsOneRound(true);
        game.removeCompletedBehaviors();
        game.getCurrentRound().setStage(Stage.Normal);
        game.getCurrentRound().setActivePlayer(self);
        events.addAll(game.continueTurnStartAfterGuanXing(self));
        return events;
    }
}

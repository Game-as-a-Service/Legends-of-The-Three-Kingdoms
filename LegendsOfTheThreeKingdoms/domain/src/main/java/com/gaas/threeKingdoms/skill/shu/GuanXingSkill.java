package com.gaas.threeKingdoms.skill.shu;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.ActivePhaseSkill;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 諸葛亮 (SHU004) 觀星 — 觀看牌堆頂 N 張牌（N = 存活角色數，上限 5），
 * 任意重排放回牌堆頂或底（issue #176）。
 *
 * 兩段式（每回合限一次）：
 *   1. activate（choice 任意）→ 翻出 N 張（私訊諸葛亮），push WaitingSkillEffect(觀星)
 *   2. choice = "ARRANGE"，cardIds = 放回牌堆頂的順序（第一張 = 下一張被抽）；
 *      未列出的牌放牌堆底（依原順序）。
 *
 * 備註：issue 的觸發時機為「回合開始階段」；v1 以出牌階段主動發動實作（時機整合 follow-up）。
 */
public class GuanXingSkill implements ActivePhaseSkill, ChoiceResolvableSkill {

    public static final String GENERAL_ID = General.諸葛亮.getGeneralId();
    public static final String SKILL_NAME = "觀星";
    public static final String PARAM_PEEKED_CARD_IDS = "GUANXING_PEEKED_CARD_IDS";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public List<DomainEvent> activate(Game game, Player self, String choice,
                                      List<String> cardIds, String targetPlayerId) {
        if (game.getCurrentRound().getUsedOncePerTurnSkills().contains(SKILL_NAME)) {
            throw new IllegalStateException("觀星 每回合限發動一次");
        }
        long aliveCount = game.getPlayers().stream().filter(p -> !p.isAlreadyDeath()).count();
        int n = (int) Math.min(5, Math.min(aliveCount, game.getDeck().size()));
        if (n == 0) {
            throw new IllegalStateException("牌堆沒有牌可觀看");
        }
        List<HandCard> peeked = game.getDeck().deal(n);
        List<String> peekedIds = peeked.stream().map(HandCard::getId).toList();
        // 牌先放回堆頂（玩家未重排前狀態不變），id 順序記在 params
        game.getDeck().putBackOnTop(peeked);
        game.getCurrentRound().getUsedOncePerTurnSkills().add(SKILL_NAME);

        WaitingSkillEffectBehavior waiting = new WaitingSkillEffectBehavior(game, self, SKILL_NAME);
        waiting.putParam(PARAM_PEEKED_CARD_IDS, peekedIds);
        game.updateTopBehavior(waiting);

        return List.of(new AskSkillEffectEvent(SKILL_NAME, self.getId(), peekedIds, null),
                game.getGameStatusEvent(self.getId() + " 發動觀星，觀看牌堆頂 " + n + " 張"));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DomainEvent> resolveChoice(Game game, WaitingSkillEffectBehavior waiting,
                                           String choice, List<String> cardIds, String targetPlayerId) {
        if (!"ARRANGE".equals(choice)) {
            throw new IllegalArgumentException("觀星 choice must be ARRANGE");
        }
        List<String> peekedIds = (List<String>) waiting.getParam(PARAM_PEEKED_CARD_IDS);
        List<String> topOrder = cardIds == null ? List.of() : cardIds;
        Set<String> peekedSet = new HashSet<>(peekedIds);
        if (!peekedSet.containsAll(topOrder) || new HashSet<>(topOrder).size() != topOrder.size()) {
            throw new IllegalArgumentException("ARRANGE cardIds must be a distinct subset of peeked cards");
        }

        // 取出 N 張（仍在堆頂），重排
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

        Player self = waiting.getBehaviorPlayer();
        return List.of(new SkillEffectEvent(SKILL_NAME, self.getId(), true, topOrder, null),
                game.getGameStatusEvent(String.format("%s 觀星完成：%d 張回堆頂、%d 張置堆底",
                        self.getId(), top.size(), bottom.size())));
    }
}

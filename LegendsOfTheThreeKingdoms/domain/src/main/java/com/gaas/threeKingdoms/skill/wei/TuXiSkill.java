package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.ActivePhaseSkill;

import java.util.ArrayList;
import java.util.List;

/**
 * 張遼 (WEI004) 突襲 — 出牌階段開始時，可獲得至多兩名其他角色各一張手牌（隨機抽取）（issue #166）。
 *
 * v1：出牌階段主動發動、每回合限一次；cardIds 充當目標玩家 id 列表（1~2 名，不可含自己）；
 * 「隨機抽取」以取目標第一張手牌實作（手牌為隱藏資訊，等同隨機）。
 */
public class TuXiSkill implements ActivePhaseSkill {

    public static final String GENERAL_ID = General.張遼.getGeneralId();
    public static final String SKILL_NAME = "突襲";

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
            throw new IllegalStateException("突襲 每回合限發動一次");
        }
        List<String> targetIds = cardIds; // 突襲的 cardIds 充當目標玩家 id 列表
        if (targetIds == null || targetIds.isEmpty() || targetIds.size() > 2) {
            throw new IllegalArgumentException("突襲 targets must be 1~2 players (via cardIds)");
        }
        if (targetIds.contains(self.getId())) {
            throw new IllegalArgumentException("Cannot target self");
        }

        List<String> takenCardIds = new ArrayList<>();
        for (String tid : targetIds) {
            Player target = game.getPlayer(tid);
            if (target.getHandSize() == 0) {
                throw new IllegalArgumentException(tid + " has no hand card");
            }
            HandCard taken = target.getHand().getCards().get(0);
            target.playCard(taken.getId());
            self.getHand().addCardToHand(taken);
            takenCardIds.add(taken.getId());
        }
        game.getCurrentRound().getUsedOncePerTurnSkills().add(SKILL_NAME);

        List<DomainEvent> events = new ArrayList<>();
        events.add(new SkillEffectEvent(SKILL_NAME, self.getId(), true, takenCardIds, String.join(",", targetIds)));
        events.add(game.getGameStatusEvent(self.getId() + " 發動突襲"));
        return events;
    }
}

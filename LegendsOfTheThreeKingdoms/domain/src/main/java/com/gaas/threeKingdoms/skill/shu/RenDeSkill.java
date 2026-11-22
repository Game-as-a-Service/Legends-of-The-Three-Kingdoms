package com.gaas.threeKingdoms.skill.shu;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.skill.trigger.ActivePhaseSkill;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 劉備 (SHU001) 仁德 — 出牌階段可將任意張手牌以任意分配方式交給其他角色；
 * 當本回合累積給出 ≥ 2 張時，回復 1 點體力（每回合限回復一次）（issue #172）。
 *
 * 一次發動可分給多人：targetPlayerIds 與 cardIds 逐張對應（第 i 張給 targetPlayerIds[i]）；
 * 未帶 targetPlayerIds 時全部給 targetPlayerId（舊用法）。也可多次發動。
 * 全部驗證通過才移動任何一張牌；每位接收者發一則 SkillEffectEvent（dataCardIds = 給他的牌）。
 * 給牌數累積在 Round.renDeGivenCount；跨越 2 張門檻時回血（renDeHealed 防重複）。
 */
public class RenDeSkill implements ActivePhaseSkill {

    public static final String GENERAL_ID = General.劉備.getGeneralId();
    public static final String SKILL_NAME = "仁德";

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
        return activate(game, self, choice, cardIds, targetPlayerId, null);
    }

    @Override
    public List<DomainEvent> activate(Game game, Player self, String choice,
                                      List<String> cardIds, String targetPlayerId,
                                      List<String> targetPlayerIds) {
        if (cardIds == null || cardIds.isEmpty()) {
            throw new IllegalArgumentException("仁德 requires at least 1 hand card to give");
        }
        List<String> recipients;
        if (targetPlayerIds != null && !targetPlayerIds.isEmpty()) {
            if (targetPlayerIds.size() != cardIds.size()) {
                throw new IllegalArgumentException("仁德 targetPlayerIds must match cardIds one-to-one");
            }
            recipients = targetPlayerIds;
        } else {
            recipients = java.util.Collections.nCopies(cardIds.size(), targetPlayerId);
        }

        // 先全部驗證（不重複、都在手牌、接收者是存活的其他角色），再移動任何一張牌
        if (new java.util.HashSet<>(cardIds).size() != cardIds.size()) {
            throw new IllegalArgumentException("仁德 cannot give the same card twice");
        }
        for (String cardId : cardIds) {
            if (self.getHand().getCard(cardId).isEmpty()) {
                throw new IllegalArgumentException("仁德 card not in hand: " + cardId);
            }
        }
        // 依接收者分組，保持第一次出現的順序
        Map<String, List<String>> byRecipient = new LinkedHashMap<>();
        for (int i = 0; i < cardIds.size(); i++) {
            String recipientId = recipients.get(i);
            if (recipientId == null || recipientId.equals(self.getId())) {
                throw new IllegalArgumentException("仁德 requires another player as recipient");
            }
            Player recipient = game.getPlayer(recipientId);
            if (recipient == null || recipient.isAlreadyDeath()) {
                throw new IllegalArgumentException("仁德 recipient must be an alive player: " + recipientId);
            }
            byRecipient.computeIfAbsent(recipientId, k -> new ArrayList<>()).add(cardIds.get(i));
        }

        List<DomainEvent> events = new ArrayList<>();
        byRecipient.forEach((recipientId, ids) -> {
            Player recipient = game.getPlayer(recipientId);
            for (String cardId : ids) {
                HandCard given = self.playCard(cardId);
                recipient.getHand().addCardToHand(given);
            }
            events.add(new SkillEffectEvent(SKILL_NAME, self.getId(), true, ids, recipientId));
        });

        Round round = game.getCurrentRound();
        round.setRenDeGivenCount(round.getRenDeGivenCount() + cardIds.size());

        if (!round.isRenDeHealed() && round.getRenDeGivenCount() >= 2
                && self.getHP() < self.getBloodCard().getMaxHp()) {
            self.getBloodCard().setHp(self.getHP() + 1);
            round.setRenDeHealed(true);
            events.add(game.getGameStatusEvent(self.getId() + " 仁德累積給出 2 張，回復 1 點體力"));
        } else {
            events.add(game.getGameStatusEvent(self.getId() + " 發動仁德，給出 " + cardIds.size() + " 張牌"));
        }
        return events;
    }
}

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
import java.util.List;

/**
 * 劉備 (SHU001) 仁德 — 出牌階段可將任意張手牌依次交給一名其他角色；
 * 當本回合累積給出 ≥ 2 張時，回復 1 點體力（每回合限回復一次）（issue #172）。
 *
 * cardIds = 要給的手牌、targetPlayerId = 接收者。可多次發動（每次給不同人也可），
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
        if (cardIds == null || cardIds.isEmpty()) {
            throw new IllegalArgumentException("仁德 requires at least 1 hand card to give");
        }
        if (targetPlayerId == null || targetPlayerId.equals(self.getId())) {
            throw new IllegalArgumentException("仁德 requires another player as recipient");
        }
        Player recipient = game.getPlayer(targetPlayerId);

        for (String cardId : cardIds) {
            HandCard given = self.playCard(cardId);
            recipient.getHand().addCardToHand(given);
        }

        Round round = game.getCurrentRound();
        round.setRenDeGivenCount(round.getRenDeGivenCount() + cardIds.size());

        List<DomainEvent> events = new ArrayList<>();
        events.add(new SkillEffectEvent(SKILL_NAME, self.getId(), true, cardIds, targetPlayerId));

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

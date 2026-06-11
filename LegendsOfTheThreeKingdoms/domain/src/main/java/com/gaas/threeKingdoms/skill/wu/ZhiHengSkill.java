package com.gaas.threeKingdoms.skill.wu;

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
 * 孫權 (WU001) 制衡 — 出牌階段可棄任意張牌（手牌+裝備），摸等量的牌（issue #183）。
 * v1：無次數限制（issue 未註明限一次）。cardIds = 要棄的牌（手牌 id 或裝備 id）。
 */
public class ZhiHengSkill implements ActivePhaseSkill {

    public static final String GENERAL_ID = General.孫權.getGeneralId();
    public static final String SKILL_NAME = "制衡";

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
            throw new IllegalArgumentException("制衡 requires at least 1 card to discard");
        }
        for (String cardId : cardIds) {
            if (self.getHand().getCard(cardId).isPresent()) {
                HandCard discarded = self.playCard(cardId);
                game.getGraveyard().add(discarded);
            } else if (self.getEquipment().hasThisEquipment(cardId)) {
                HandCard equipment = self.getEquipment().getAllEquipmentCards().stream()
                        .filter(c -> c.getId().equals(cardId)).findFirst().orElseThrow();
                self.getEquipment().removeEquipment(cardId);
                game.getGraveyard().add(equipment);
            } else {
                throw new IllegalArgumentException("Card not in hand or equipment: " + cardId);
            }
        }
        List<DomainEvent> events = new ArrayList<>();
        events.add(new SkillEffectEvent(SKILL_NAME, self.getId(), true, cardIds, null));
        events.add(game.drawCardToPlayer(self, false, cardIds.size()));
        events.add(game.getGameStatusEvent(self.getId() + " 發動制衡，棄 " + cardIds.size() + " 摸 " + cardIds.size()));
        return events;
    }
}

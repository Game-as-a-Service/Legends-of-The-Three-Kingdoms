package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.ActivePhaseSkill;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 孫尚香 (WU008) 結姻 — 出牌階段（每回合 1 次）：棄 2 張手牌，
 * 令自己與另一名傷勢最重的角色各回 1 點體力（issue #194）。
 *
 * 「傷勢最重」= 已損失體力（maxHp - hp）最多的其他存活角色；平手取座位順序（自己下家起）最先者。
 */
public class JieYinSkill implements ActivePhaseSkill {

    public static final String GENERAL_ID = General.孫尚香.getGeneralId();
    public static final String SKILL_NAME = "結姻";

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
            throw new IllegalStateException("結姻 每回合限發動一次");
        }
        if (cardIds == null || cardIds.size() != 2) {
            throw new IllegalArgumentException("結姻 requires exactly 2 hand cards to discard");
        }

        // 傷勢最重的其他存活角色（座位順序 tie-break）
        Player mostWounded = null;
        int maxLost = 0;
        Player cursor = self;
        for (int i = 0; i < game.getPlayers().size() - 1; i++) {
            cursor = game.getNextPlayer(cursor);
            if (cursor.isAlreadyDeath()) continue;
            int lost = cursor.getBloodCard().getMaxHp() - cursor.getHP();
            if (lost > maxLost) {
                maxLost = lost;
                mostWounded = cursor;
            }
        }
        if (mostWounded == null) {
            throw new IllegalStateException("結姻：沒有受傷的其他角色");
        }

        for (String cardId : cardIds) {
            HandCard discarded = self.playCard(cardId);
            game.getGraveyard().add(discarded);
        }
        game.getCurrentRound().getUsedOncePerTurnSkills().add(SKILL_NAME);

        healOne(self);
        healOne(mostWounded);

        List<DomainEvent> events = new ArrayList<>();
        events.add(new SkillEffectEvent(SKILL_NAME, self.getId(), true, cardIds, mostWounded.getId()));
        events.add(game.getGameStatusEvent(String.format(
                "%s 發動結姻：與 %s 各回 1 點體力", self.getId(), mostWounded.getId())));
        return events;
    }

    private void healOne(Player player) {
        int maxHp = player.getBloodCard().getMaxHp();
        if (player.getHP() < maxHp) {
            player.getBloodCard().setHp(player.getHP() + 1);
        }
    }
}

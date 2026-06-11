package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.ActivePhaseSkill;

import java.util.ArrayList;
import java.util.List;

/**
 * 黃蓋 (WU004) 苦肉 — 出牌階段可對自己造成 1 點傷害，然後摸 2 張牌（issue #187）。
 * v1：HP ≥ 2 才可發動（HP=1 自殺進瀕死的整合為 follow-up）；無次數限制。
 */
public class KuRouSkill implements ActivePhaseSkill {

    public static final String GENERAL_ID = General.黃蓋.getGeneralId();
    public static final String SKILL_NAME = "苦肉";

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
        if (self.getHP() < 2) {
            throw new IllegalStateException("苦肉 v1 需 HP >= 2 才可發動（瀕死整合 follow-up）");
        }
        self.damage(1);
        List<DomainEvent> events = new ArrayList<>();
        events.add(new SkillEffectEvent(SKILL_NAME, self.getId(), true, List.of(), null));
        events.add(game.drawCardToPlayer(self, false, 2));
        events.add(game.getGameStatusEvent(self.getId() + " 發動苦肉（-1 HP, +2 牌）"));
        return events;
    }
}

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
 * 黃蓋 (WU004) 苦肉 — 出牌階段可失去 1 點體力，然後摸 2 張牌；無次數限制（issue #187）。
 * <ul>
 *   <li>是「失去體力」不是「受到傷害」：沒有傷害來源，不觸發任何受傷後技能。</li>
 *   <li>HP 1 也能發動：失去體力後進入瀕死求桃，被救回才摸 2 張；沒被救回就死亡、不摸牌，
 *       也沒有人能領擊殺獎勵。</li>
 * </ul>
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
        int originalHp = self.getHP();
        // 失去體力：直接扣 HP，不走 getDamagedEvent（不觸發受傷後技能）
        self.damage(1);
        List<DomainEvent> events = new ArrayList<>();
        events.add(new SkillEffectEvent(SKILL_NAME, self.getId(), true, List.of(), null));
        if (self.isHPGreaterThanZero()) {
            events.add(game.drawCardToPlayer(self, false, 2));
            events.add(game.getGameStatusEvent(self.getId() + " 發動苦肉（-1 HP, +2 牌）"));
        } else {
            events.addAll(game.enterDyingByHpLoss(self, originalHp, 2));
            events.add(game.getGameStatusEvent(self.getId() + " 發動苦肉，失去最後 1 點體力進入瀕死，救回後摸 2 張牌"));
        }
        return events;
    }
}

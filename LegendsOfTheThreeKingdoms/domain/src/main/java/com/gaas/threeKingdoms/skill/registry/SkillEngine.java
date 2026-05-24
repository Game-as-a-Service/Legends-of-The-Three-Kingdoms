package com.gaas.threeKingdoms.skill.registry;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;
import com.gaas.threeKingdoms.skill.context.DamageContext;
import com.gaas.threeKingdoms.skill.trigger.BeforeAskDodgeSkill;
import com.gaas.threeKingdoms.skill.trigger.OnDamagedSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SkillEngine {

    private SkillEngine() {
    }

    public static List<DomainEvent> onDamaged(Game game, DamageContext ctx) {
        Player damaged = ctx.damagedPlayer();
        if (damaged == null) {
            return List.of();
        }
        GeneralCard general = damaged.getGeneralCard();
        if (general == null) {
            return List.of();
        }
        List<DomainEvent> events = new ArrayList<>();
        for (Skill skill : SkillRegistry.of(general.getGeneralId())) {
            if (skill instanceof OnDamagedSkill onDamaged) {
                events.addAll(onDamaged.onDamaged(game, ctx));
            }
        }
        return events;
    }

    /**
     * AskDodge 前介入鉤點：iterate damaged 武將綁定的技能，第一個回傳非 empty 的 win。
     * caller 用 {@code intercepted.ifPresentOrElse(events::addAll, () -> events.add(new AskDodgeEvent(...)))}。
     */
    public static Optional<List<DomainEvent>> beforeAskDodge(Game game, Player damaged, Behavior parentBehavior) {
        if (damaged == null) {
            return Optional.empty();
        }
        GeneralCard general = damaged.getGeneralCard();
        if (general == null) {
            return Optional.empty();
        }
        for (Skill skill : SkillRegistry.of(general.getGeneralId())) {
            if (skill instanceof BeforeAskDodgeSkill beforeAsk) {
                Optional<List<DomainEvent>> result = beforeAsk.beforeAskDodge(game, damaged, parentBehavior);
                if (result.isPresent()) {
                    return result;
                }
            }
        }
        return Optional.empty();
    }
}

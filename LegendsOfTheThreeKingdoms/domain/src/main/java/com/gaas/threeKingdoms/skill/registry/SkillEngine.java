package com.gaas.threeKingdoms.skill.registry;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;
import com.gaas.threeKingdoms.skill.context.DamageContext;
import com.gaas.threeKingdoms.skill.trigger.OnDamagedSkill;

import java.util.ArrayList;
import java.util.List;

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
}

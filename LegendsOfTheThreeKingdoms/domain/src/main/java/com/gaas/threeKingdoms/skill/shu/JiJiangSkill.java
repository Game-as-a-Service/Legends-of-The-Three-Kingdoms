package com.gaas.threeKingdoms.skill.shu;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingJiJiangResponseBehavior;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.Faction;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.skill.trigger.BeforeAskKillSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 劉備 (SHU001) 激將 — 主公技。主公劉備需要出殺時，可令其他蜀勢力武將替其打出殺（issue #173）。
 *
 * Mirror 護駕：攔截 AskKillEvent（南蠻入侵 / 決鬥），依座位順序輪詢其他存活蜀將；
 * 任一人 ACCEPT（交出一張殺）視為劉備打出。全部 DECLINE → fallback AskKillEvent(劉備)。
 * v1 範圍：南蠻 / 決鬥 兩處 AskKill emitter；主動出殺與借刀殺人為 follow-up。
 */
public class JiJiangSkill implements BeforeAskKillSkill {

    public static final String GENERAL_ID = General.劉備.getGeneralId();
    public static final String SKILL_NAME = "激將";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public Optional<List<DomainEvent>> beforeAskKill(Game game, Player asked, Behavior parentBehavior) {
        if (asked.getRoleCard().getRole() != Role.MONARCH) {
            return Optional.empty();
        }
        List<Player> shuHelpers = otherAliveShuInSeatingOrder(game, asked);
        if (shuHelpers.isEmpty()) {
            return Optional.empty();
        }
        List<String> order = shuHelpers.stream().map(Player::getId).toList();
        WaitingJiJiangResponseBehavior waiting = new WaitingJiJiangResponseBehavior(game, asked, order);
        game.updateTopBehavior(waiting);
        Player first = shuHelpers.get(0);
        game.getCurrentRound().setActivePlayer(first);
        return Optional.of(List.of(new AskSkillEffectEvent(SKILL_NAME, first.getId(), List.of(), asked.getId())));
    }

    private List<Player> otherAliveShuInSeatingOrder(Game game, Player liuBei) {
        List<Player> helpers = new ArrayList<>();
        Player cursor = liuBei;
        for (int i = 0; i < game.getPlayers().size(); i++) {
            cursor = game.getNextPlayer(cursor);
            if (cursor.equals(liuBei)) break;
            if (cursor.isAlreadyDeath()) continue;
            if (cursor.getFaction() == Faction.SHU) {
                helpers.add(cursor);
            }
        }
        return helpers;
    }
}

package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingHuJiaResponseBehavior;
import com.gaas.threeKingdoms.events.AskHuJiaEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.Faction;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.skill.trigger.BeforeAskDodgeSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 曹操 (WEI001) 護駕 — 主公技。
 *
 * 標準版規則：當主公（曹操）需要使用或打出一張閃時，其他存活的魏勢力角色按行動順序，依次選擇是否為其打出一張閃。
 * 任一人打出則視為曹操打出此閃。
 *
 * 守門：
 *   - 曹操必須是 MONARCH（非主公的曹操不觸發）
 *   - 必須有其他存活的 Wei 武將
 *
 * 觸發後：push {@link WaitingHuJiaResponseBehavior} 並 emit {@link AskHuJiaEffectEvent} 給第一位 Wei；
 * caller 跳過原本的 AskDodgeEvent。所有 Wei 拒絕 → WaitingHuJia 在收最後一個 DECLINE 時自動 fallback
 * emit AskDodgeEvent(曹操)。
 */
public class HuJiaSkill implements BeforeAskDodgeSkill {

    public static final String GENERAL_ID = General.曹操.getGeneralId();
    public static final String SKILL_NAME = "護駕";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public Optional<List<DomainEvent>> beforeAskDodge(Game game, Player damaged, Behavior parentBehavior) {
        if (damaged.getRoleCard().getRole() != Role.MONARCH) {
            return Optional.empty();
        }

        List<Player> weiHelpers = otherAliveWeiInSeatingOrder(game, damaged);
        if (weiHelpers.isEmpty()) {
            return Optional.empty();
        }

        List<String> weiOrder = weiHelpers.stream().map(Player::getId).toList();
        WaitingHuJiaResponseBehavior waiting = new WaitingHuJiaResponseBehavior(game, damaged, weiOrder);
        game.updateTopBehavior(waiting);

        Player firstWei = weiHelpers.get(0);
        game.getCurrentRound().setActivePlayer(firstWei);

        return Optional.of(List.of(new AskHuJiaEffectEvent(
                firstWei.getId(), damaged.getId(),
                WaitingHuJiaResponseBehavior.dodgeCardIdsInHand(firstWei))));
    }

    private List<Player> otherAliveWeiInSeatingOrder(Game game, Player caoCao) {
        // 用 isAlreadyDeath() 作為「未結算死亡」判準（與 Player API 一致）。
        // 注意：DYING 狀態仍視為存活，可以代替主公出閃（標準三國殺 dying 期間仍能打閃 / 桃 / 無懈）。
        List<Player> helpers = new ArrayList<>();
        int total = game.getPlayers().size();
        Player cursor = caoCao;
        for (int i = 0; i < total; i++) {
            cursor = game.getNextPlayer(cursor);
            if (cursor.equals(caoCao)) break;
            if (cursor.isAlreadyDeath()) continue;
            if (cursor.getFaction() == Faction.WEI) {
                helpers.add(cursor);
            }
        }
        return helpers;
    }
}

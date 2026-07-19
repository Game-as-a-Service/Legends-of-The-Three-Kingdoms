package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingHuJiaResponseBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskHuJiaEffectEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.Faction;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.skill.trigger.BeforeAskDodgeSkill;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;

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
 * 主動觸發（issue #217）：護駕非鎖定技，通過守門後先對曹操 emit {@link AskSkillEffectEvent}
 * 詢問是否發動（回應走通用 `player:useSkillEffect`）：
 *   - ACCEPT → push {@link WaitingHuJiaResponseBehavior} 並 emit {@link AskHuJiaEffectEvent} 給第一位 Wei；
 *     所有 Wei 拒絕 → WaitingHuJia 在收最後一個 DECLINE 時自動 fallback emit AskDodgeEvent(曹操)
 *   - SKIP → 直接 emit 原本的 AskDodgeEvent(曹操) 自己出閃
 */
public class HuJiaSkill implements BeforeAskDodgeSkill, ChoiceResolvableSkill {

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

        WaitingSkillEffectBehavior waiting = new WaitingSkillEffectBehavior(game, damaged, SKILL_NAME);
        game.updateTopBehavior(waiting);
        game.getCurrentRound().setActivePlayer(damaged);

        return Optional.of(List.of(new AskSkillEffectEvent(SKILL_NAME, damaged.getId(), List.of(), null)));
    }

    @Override
    public List<DomainEvent> resolveChoice(Game game, WaitingSkillEffectBehavior waiting,
                                           String choice, List<String> cardIds, String targetPlayerId) {
        Player caoCao = waiting.getBehaviorPlayer();

        if ("ACCEPT".equals(choice)) {
            List<Player> weiHelpers = otherAliveWeiInSeatingOrder(game, caoCao);
            if (!weiHelpers.isEmpty()) {
                // pop 詢問層，維持 [host, WaitingHuJia] 的 stack 假設
                // （WaitingHuJia ACCEPT 時 peek parent 必須是 HuJiaCompatibleAskDodgeBehavior）
                game.removeTopBehavior();

                List<String> weiOrder = weiHelpers.stream().map(Player::getId).toList();
                WaitingHuJiaResponseBehavior waitingHuJia = new WaitingHuJiaResponseBehavior(game, caoCao, weiOrder);
                game.updateTopBehavior(waitingHuJia);

                Player firstWei = weiHelpers.get(0);
                game.getCurrentRound().setActivePlayer(firstWei);

                List<DomainEvent> events = new ArrayList<>();
                events.add(new SkillEffectEvent(SKILL_NAME, caoCao.getId(), true, List.of(), null));
                events.add(new AskHuJiaEffectEvent(
                        firstWei.getId(), caoCao.getId(),
                        WaitingHuJiaResponseBehavior.dodgeCardIdsInHand(firstWei)));
                events.add(game.getGameStatusEvent(caoCao.getId() + " 發動護駕，詢問魏勢力代閃"));
                return events;
            }
            // 守門通過後到回應之間魏將全滅的極端情況：視同 SKIP fallback
        } else if (!"SKIP".equals(choice)) {
            throw new IllegalArgumentException("Invalid HuJia choice: " + choice);
        }

        game.getCurrentRound().setActivePlayer(caoCao);
        List<DomainEvent> events = new ArrayList<>();
        events.add(new SkillEffectEvent(SKILL_NAME, caoCao.getId(), false, List.of(), null));
        events.add(new AskDodgeEvent(caoCao.getId()));
        events.add(game.getGameStatusEvent("放棄護駕，回到主公出閃"));
        return events;
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

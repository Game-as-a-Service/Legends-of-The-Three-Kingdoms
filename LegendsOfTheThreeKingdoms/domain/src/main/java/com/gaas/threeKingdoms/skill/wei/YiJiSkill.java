package com.gaas.threeKingdoms.skill.wei;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.JianXiongCompatibleTopBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.context.DamageContext;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;
import com.gaas.threeKingdoms.skill.trigger.OnDamagedSkill;

import java.util.ArrayList;
import java.util.List;

/**
 * 郭嘉 (WEI006) 遺計 — 受到傷害後，可摸兩張牌，或令另一名角色獲得一張牌（issue #169）。
 *
 * choice：
 *   - "ACCEPT"：郭嘉摸 2
 *   - "GIVE" + targetPlayerId：該角色從牌堆獲得 1 張
 *   - "SKIP"：放棄
 *
 * AOE polling（南蠻/萬箭）中亦觸發（mirror 反饋 #221 / 剛烈 #233，issue #250）：
 * 詢問插在 polling caller 之上，choice 結算後由 {@link WaitingSkillEffectBehavior}
 * 的收鏈掃描讀取 caller 的 deferred 標記 resume 輪詢，回到原本的 AOE 詢問輪。
 * 瀕死不觸發。
 */
public class YiJiSkill implements OnDamagedSkill, ChoiceResolvableSkill {

    public static final String GENERAL_ID = General.郭嘉.getGeneralId();
    public static final String SKILL_NAME = "遺計";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public List<DomainEvent> onDamaged(Game game, DamageContext ctx) {
        Player damaged = ctx.damagedPlayer();
        if (!damaged.isHPGreaterThanZero()) {
            return List.of();
        }
        Behavior top = game.isTopBehaviorEmpty() ? null : game.peekTopBehavior();
        if (top != null && !(top instanceof JianXiongCompatibleTopBehavior)) {
            return List.of(); // 非相容 host 不觸發
        }
        // polling caller（南蠻/萬箭）需保留底層 behavior，詢問結算後 resume 輪詢
        // （mirror 反饋 #221 / 剛烈 #233 樣板；收鏈由 WaitingSkillEffectBehavior 掃描處理）
        boolean isPollingCaller = top instanceof JianXiongCompatibleTopBehavior compatible
                && compatible.isPollingCaller();
        if (!isPollingCaller) {
            game.removeCompletedBehaviors();
        }
        WaitingSkillEffectBehavior waiting = new WaitingSkillEffectBehavior(game, damaged, SKILL_NAME);
        if (isPollingCaller) {
            waiting.putParam(WaitingSkillEffectBehavior.PARAM_RESUME_POLLING, "true");
        }
        game.updateTopBehavior(waiting);
        game.getCurrentRound().setActivePlayer(damaged);

        return List.of(new AskSkillEffectEvent(SKILL_NAME, damaged.getId(), List.of(), null));
    }

    @Override
    public List<DomainEvent> resolveChoice(Game game, WaitingSkillEffectBehavior waiting,
                                           String choice, List<String> cardIds, String targetPlayerId) {
        Player guoJia = waiting.getBehaviorPlayer();
        List<DomainEvent> events = new ArrayList<>();
        game.getCurrentRound().setActivePlayer(game.getCurrentRound().getCurrentRoundPlayer());

        switch (choice) {
            case "ACCEPT" -> {
                events.add(new SkillEffectEvent(SKILL_NAME, guoJia.getId(), true, List.of(), null));
                events.add(game.drawCardToPlayer(guoJia, false, 2));
                events.add(game.getGameStatusEvent(guoJia.getId() + " 發動遺計摸兩張"));
            }
            case "GIVE" -> {
                if (targetPlayerId == null || targetPlayerId.equals(guoJia.getId())) {
                    throw new IllegalArgumentException("GIVE requires another player as target");
                }
                Player target = game.getPlayer(targetPlayerId);
                events.add(new SkillEffectEvent(SKILL_NAME, guoJia.getId(), true, List.of(), targetPlayerId));
                events.add(game.drawCardToPlayer(target, false, 1));
                events.add(game.getGameStatusEvent(guoJia.getId() + " 發動遺計令 " + targetPlayerId + " 獲得一張牌"));
            }
            case "SKIP" -> {
                events.add(new SkillEffectEvent(SKILL_NAME, guoJia.getId(), false, List.of(), null));
                events.add(game.getGameStatusEvent("放棄遺計"));
            }
            default -> throw new IllegalArgumentException("Invalid YiJi choice: " + choice);
        }
        return events;
    }
}

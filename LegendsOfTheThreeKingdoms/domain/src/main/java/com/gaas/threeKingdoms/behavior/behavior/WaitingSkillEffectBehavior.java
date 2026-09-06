package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;
import com.gaas.threeKingdoms.skill.registry.SkillRegistry;
import com.gaas.threeKingdoms.skill.trigger.ChoiceResolvableSkill;
import lombok.Getter;

import java.util.List;

/**
 * 通用武將技等待回應 behavior — 配合 `player:useSkillEffect` endpoint。
 *
 * behaviorPlayer = 被詢問者；skillName 決定 resolve 時 dispatch 到哪個
 * {@link ChoiceResolvableSkill}。技能 context 一律放 params（BehaviorData 可持久化）。
 *
 * 與 WaitingJianXiongResponseBehavior 的差異：不綁定特定技能；新技能不需新增
 * behavior class 與 endpoint。
 */
@Getter
public class WaitingSkillEffectBehavior extends Behavior {

    /**
     * 值 "true" 時：本 waiting 是插在 AOE polling caller（南蠻/萬箭）之上的
     * OnDamagedSkill 詢問（如反饋），resolve 後需呼叫底層的
     * {@code resumeJianXiongPolling} 推進輪詢（mirror 奸雄 issue #209 的 reload-safe 樣板；
     * 本 behavior 無 transient callback，一律走 param + resume hook）。
     */
    public static final String PARAM_RESUME_POLLING = "WSE_RESUME_POLLING";

    private final String skillName;

    public WaitingSkillEffectBehavior(Game game, Player respondingPlayer, String skillName) {
        super(game,
                respondingPlayer,
                List.of(respondingPlayer.getId()),
                respondingPlayer,
                null,
                PlayType.SYSTEM_INTERNAL.getPlayType(),
                null,
                false,
                false,
                true);
        this.skillName = skillName;
    }

    public List<DomainEvent> resolveChoice(String respondingPlayerId, String choice,
                                           List<String> cardIds, String targetPlayerId) {
        if (!behaviorPlayer.getId().equals(respondingPlayerId)) {
            throw new IllegalStateException(String.format(
                    "player %s is not the one who should respond to skill %s", respondingPlayerId, skillName));
        }
        ChoiceResolvableSkill skill = findSkill();
        List<DomainEvent> events = new java.util.ArrayList<>(
                skill.resolveChoice(game, this, choice, cardIds, targetPlayerId));

        // AOE polling resume 收鏈掃描：damage 當下 polling caller（南蠻/萬箭）把待推進的
        // reactor 記在自己的 param（DEFERRED_ADVANCE_PLAYER_ID，reload-safe）。本 waiting
        // 完成後由 stack 頂往下跳過已完成者；第一個未完成 behavior 若正是帶標記的 polling
        // caller，表示技能詢問鏈已全部收斂 → resume 輪詢並清除標記。
        // 剛烈兩段式（ASK_XIAHOU → ASK_SOURCE）與鬼才巢狀詢問（issue #165）因此自然收鏈；
        // 舊版只認「flag waiting 的下一層」，鏈上多一個 waiting 就會斷、輪詢卡住。
        isOneRound = true;
        List<com.gaas.threeKingdoms.behavior.Behavior> stack = game.getTopBehavior();
        for (int i = stack.size() - 1; i >= 0; i--) {
            com.gaas.threeKingdoms.behavior.Behavior under = stack.get(i);
            if (under instanceof WaitingSkillEffectBehavior && under.isOneRound()) {
                continue; // 鏈上已完成、待 pop 的 waiting（含本 behavior 自己）
            }
            // 注意不可依 polling behavior 的 isOneRound 判斷 mid-poll（詢問期間可能為 true，
            // resume 內部才會依剩餘 reactor 重設）；defer 與否只看標記本身
            if (under instanceof com.gaas.threeKingdoms.behavior.JianXiongCompatibleTopBehavior compatible
                    && compatible.isPollingCaller()) {
                String deferredReactorId = (String) under.getParam(
                        com.gaas.threeKingdoms.behavior.JianXiongCompatibleTopBehavior.PARAM_DEFERRED_ADVANCE_PLAYER_ID);
                if (deferredReactorId != null) {
                    under.putParam(
                            com.gaas.threeKingdoms.behavior.JianXiongCompatibleTopBehavior.PARAM_DEFERRED_ADVANCE_PLAYER_ID,
                            null);
                    events.addAll(compatible.resumeJianXiongPolling(deferredReactorId));
                }
            }
            break; // 只看第一個未完成 behavior
        }

        // 最終狀態快照：技能 resolve 可能先發 GameStatusEvent 再推進（鬼才 resume 判定、
        // 反饋/剛烈 AOE resume 輪詢），presenter 取最後一個 GameStatusEvent 才能拿到正確的
        // activePlayer / HP（使用者回報：鬼才換牌後 activePlayer 停在司馬懿）
        events.add(game.getGameStatusEvent(firstStatusMessage(events, skillName + " 結算")));

        return events;
    }

    /** 沿用技能自己的訊息（第一則 status），只刷新 round / seats 快照。 */
    private static String firstStatusMessage(List<DomainEvent> events, String fallback) {
        for (DomainEvent e : events) {
            if (e instanceof com.gaas.threeKingdoms.events.GameStatusEvent status) {
                return status.getMessage();
            }
        }
        return fallback;
    }

    private ChoiceResolvableSkill findSkill() {
        // skillName 全域唯一（35 技無重名），由全部已註冊技能中找
        for (Skill skill : SkillRegistry.all()) {
            if (skill instanceof ChoiceResolvableSkill resolvable && skill.getSkillName().equals(skillName)) {
                return resolvable;
            }
        }
        throw new IllegalStateException("No ChoiceResolvableSkill registered with name: " + skillName);
    }
}

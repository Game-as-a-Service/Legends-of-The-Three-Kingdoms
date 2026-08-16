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

        // AOE polling resume：必須在 isOneRound=true 之前跑 —
        // resume 可能把底層 polling behavior 的 isOneRound 改回 false（mid-poll），
        // 需先於本 behavior 標記完成、避免 removeCompletedBehaviors 誤 pop polling behavior。
        if ("true".equals(getParam(PARAM_RESUME_POLLING))) {
            game.peekTopBehaviorSecondElement().ifPresent(under -> {
                if (under instanceof com.gaas.threeKingdoms.behavior.JianXiongCompatibleTopBehavior compatible
                        && compatible.isPollingCaller()) {
                    events.addAll(compatible.resumeJianXiongPolling(behaviorPlayer.getId()));
                }
            });
        }

        isOneRound = true;
        return events;
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

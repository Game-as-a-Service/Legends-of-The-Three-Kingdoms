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
        List<DomainEvent> events = skill.resolveChoice(game, this, choice, cardIds, targetPlayerId);
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

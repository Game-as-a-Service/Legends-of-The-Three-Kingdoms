package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.skill.Skill;

import java.util.List;

/**
 * 透過通用 endpoint `player:useSkillEffect` 回應選擇的技能。
 *
 * 觸發端 push {@link WaitingSkillEffectBehavior}（帶 skillName + context params），
 * 玩家回應後由 Game.playerUseSkillEffect 依 skillName 從 SkillRegistry 找回本
 * skill 並呼叫 {@link #resolveChoice}。所有 context 必須放在 behavior params
 * （可被 BehaviorData 持久化），不可依賴記憶體狀態。
 */
public interface ChoiceResolvableSkill extends Skill {

    /**
     * @param choice         "ACCEPT" / "SKIP" / 技能自訂值
     * @param cardIds        選擇的牌（可為 null / empty）
     * @param targetPlayerId 選擇的目標（可為 null）
     */
    List<DomainEvent> resolveChoice(Game game, WaitingSkillEffectBehavior waiting,
                                    String choice, List<String> cardIds, String targetPlayerId);
}

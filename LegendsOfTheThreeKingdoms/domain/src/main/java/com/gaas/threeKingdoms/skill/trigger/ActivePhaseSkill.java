package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;

import java.util.List;

/**
 * 出牌階段主動發動的技能（制衡 / 突襲 / 苦肉 / 反間 / 結姻 / 仁德 / 觀星）。
 *
 * 玩家在自己出牌階段（topBehavior 為空時）直接呼叫 `player:useSkillEffect`，
 * Game.playerUseSkillEffect 以 proactive 分支 dispatch 到本 interface。
 * 每回合限一次的技能自行檢查 {@code game.getCurrentRound().getUsedOncePerTurnSkills()}。
 */
public interface ActivePhaseSkill extends Skill {

    List<DomainEvent> activate(Game game, Player self, String choice,
                               List<String> cardIds, String targetPlayerId);
}

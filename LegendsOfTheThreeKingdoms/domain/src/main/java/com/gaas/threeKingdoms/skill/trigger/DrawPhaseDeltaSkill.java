package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;

/**
 * 修正摸牌階段抽牌數的技能（例：英姿 +1 鎖定；裸衣 -1 需當回合已發動）。
 */
public interface DrawPhaseDeltaSkill extends Skill {
    int drawCardDelta(Game game, Player player);
}

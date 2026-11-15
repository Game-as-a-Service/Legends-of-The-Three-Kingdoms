package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;

/**
 * 修正摸牌階段抽牌數的技能（例：英姿 +1 鎖定；裸衣 -1 需當回合已發動）。
 */
public interface DrawPhaseDeltaSkill extends Skill {
    int drawCardDelta(Game game, Player player);

    /**
     * 摸牌階段是否另外廣播 SkillEffectEvent 說明技能發動。
     * 鎖定技（英姿）沒有詢問流程，不廣播前端就看不出為什麼多摸；
     * 主動技（裸衣）發動時已有自己的事件，維持預設 false。
     */
    default boolean announcesOnDraw() {
        return false;
    }
}

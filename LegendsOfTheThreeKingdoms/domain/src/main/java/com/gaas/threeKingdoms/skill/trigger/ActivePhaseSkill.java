package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;

import java.util.List;

/**
 * 出牌階段主動發動的技能（制衡 / 苦肉 / 反間 / 結姻 / 仁德）。觀星改為回合開始詢問，見 GuanXingSkill。
 *
 * 玩家在自己出牌階段（topBehavior 為空時）直接呼叫 `player:useSkillEffect`，
 * Game.playerUseSkillEffect 以 proactive 分支 dispatch 到本 interface。
 * 每回合限一次的技能自行檢查 {@code game.getCurrentRound().getUsedOncePerTurnSkills()}。
 */
public interface ActivePhaseSkill extends Skill {

    List<DomainEvent> activate(Game game, Player self, String choice,
                               List<String> cardIds, String targetPlayerId);

    /**
     * 帶逐張接收者的版本（targetPlayerIds 與 cardIds 逐張對應）。預設不支援：帶了 targetPlayerIds 就拒絕，
     * 沒帶則走原本的 {@link #activate(Game, Player, String, List, String)}。仁德 override。
     */
    default List<DomainEvent> activate(Game game, Player self, String choice,
                                       List<String> cardIds, String targetPlayerId,
                                       List<String> targetPlayerIds) {
        if (targetPlayerIds != null && !targetPlayerIds.isEmpty()) {
            throw new IllegalArgumentException(getSkillName() + " 不支援 targetPlayerIds（逐張指定接收者）");
        }
        return activate(game, self, choice, cardIds, targetPlayerId);
    }
}

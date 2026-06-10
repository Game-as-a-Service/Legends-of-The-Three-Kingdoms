package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;

/**
 * 「不能成為某些牌的目標」鎖定技（例：謙遜、空城）。
 *
 * caller 在指定目標 / 建 AOE reactor 列表 / 轉移延遲錦囊時呼叫
 * {@code SkillEngine.isImmuneToCard(game, target, card)}；true 表示此目標必須被排除。
 */
public interface TargetImmunitySkill extends Skill {
    boolean isImmune(Player self, HandCard card);
}

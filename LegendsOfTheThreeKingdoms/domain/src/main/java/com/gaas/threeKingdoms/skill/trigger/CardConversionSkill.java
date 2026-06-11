package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.skill.Skill;

/**
 * 轉化牌技（武聖 / 龍膽 / 傾國 / 奇襲 / 國色）— 把一張來源牌當另一種牌使用或打出。
 *
 * 透過通用 endpoint `player:useSkillEffect`：
 *   choice = 轉化目標型別（"KILL" / "DODGE" / "DISMANTLE" / "CONTENTMENT"）
 *   cardIds = [來源牌 id]、targetPlayerId = 出牌目標（KILL/DISMANTLE/CONTENTMENT 必填）
 *
 * 實際轉化流程由 Game.applyConversion 集中處理；本 interface 只回答
 * 「此來源牌可否被本技能轉成 as 型別」。
 */
public interface CardConversionSkill extends Skill {

    String AS_KILL = "KILL";
    String AS_DODGE = "DODGE";
    String AS_DISMANTLE = "DISMANTLE";
    String AS_CONTENTMENT = "CONTENTMENT";

    boolean canConvert(HandCard source, String as);
}

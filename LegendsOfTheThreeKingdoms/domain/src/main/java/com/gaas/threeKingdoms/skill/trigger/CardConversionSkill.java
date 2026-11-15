package com.gaas.threeKingdoms.skill.trigger;

import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.skill.Skill;

/**
 * 轉化牌技（武聖 / 龍膽 / 傾國 / 奇襲 / 國色）— 把一張來源牌當另一種牌使用或打出。
 *
 * 透過通用 endpoint `player:useSkillEffect`：
 *   choice = 轉化目標型別（"KILL" / "DODGE" / "DISMANTLE" / "CONTENTMENT"）
 *   cardIds = [來源牌 id（手牌；allowsEquipmentSource 的技能也可為裝備區的牌）]、targetPlayerId = 出牌目標（KILL/DISMANTLE/CONTENTMENT 必填）
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

    /**
     * 來源牌可否取自裝備區（標準版：武聖「紅色牌」、奇襲「黑色牌」、國色「方塊牌」皆含裝備區；
     * 傾國限「黑色手牌」、龍膽限殺/閃 → 只能手牌）。
     * 裝備區來源會先離開裝備區（攻擊距離、諸葛連弩等以失去後計算）並觸發失去裝備 hook（梟姬）。
     */
    default boolean allowsEquipmentSource() {
        return false;
    }
}

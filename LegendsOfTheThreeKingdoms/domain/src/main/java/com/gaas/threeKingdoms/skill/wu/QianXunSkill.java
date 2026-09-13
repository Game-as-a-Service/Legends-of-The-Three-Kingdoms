package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.Snatch;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.TargetImmunitySkill;

/**
 * 陸遜 (WU007) 謙遜 — 鎖定技，你不能成為【順手牽羊】和【樂不思蜀】的目標（標準版）。
 *
 * issue #192 的原始文字（南蠻入侵 / 萬箭齊發 / 樂不思蜀 / 閃電）與官方標準版不符，
 * 使用者指示改回標準版；南蠻/萬箭/閃電對陸遜恢復正常結算。
 * 調整範圍只需改本 method 的 instanceof 列表。
 */
public class QianXunSkill implements TargetImmunitySkill {

    public static final String GENERAL_ID = General.陸遜.getGeneralId();
    public static final String SKILL_NAME = "謙遜";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public boolean isImmune(Player self, HandCard card) {
        return card instanceof Snatch || card instanceof Contentment;
    }
}

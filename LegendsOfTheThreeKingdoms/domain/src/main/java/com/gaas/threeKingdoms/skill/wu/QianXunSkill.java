package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.trigger.TargetImmunitySkill;

/**
 * 陸遜 (WU007) 謙遜 — 鎖定技。
 * 照 issue #192 文字實作：不能成為南蠻入侵 / 萬箭齊發 / 樂不思蜀 / 閃電的目標。
 * ⚠️ 與官方標準版規則（不能成為順手牽羊與樂不思蜀的目標）不同；
 * 此處以 issue 為準，調整時只需改本 method 的 instanceof 列表。
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
        return card instanceof BarbarianInvasion
                || card instanceof ArrowBarrage
                || card instanceof Contentment
                || card instanceof Lightning;
    }
}

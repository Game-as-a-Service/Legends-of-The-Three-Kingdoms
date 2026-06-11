package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.skill.trigger.AfterLoseEquipmentSkill;

/**
 * 孫尚香 (WU008) 梟姬 — 每當你失去一張裝備牌後，可摸 2 張牌（issue #195）。
 * v1 自動觸發（嚴格有利不詢問）；覆蓋路徑：被過河拆橋拆裝備、被順手牽羊偷裝備、
 * 反饋取走裝備。主動替換裝備（裝新蓋舊）為 follow-up。
 */
public class XiaoJiSkill implements AfterLoseEquipmentSkill {

    public static final String GENERAL_ID = General.孫尚香.getGeneralId();
    public static final String SKILL_NAME = "梟姬";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public int drawCountAfterLoseEquipment() {
        return 2;
    }
}

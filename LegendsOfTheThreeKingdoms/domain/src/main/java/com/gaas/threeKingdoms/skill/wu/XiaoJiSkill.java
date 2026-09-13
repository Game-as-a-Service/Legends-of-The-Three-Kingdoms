package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.skill.trigger.AfterLoseEquipmentSkill;

/**
 * 孫尚香 (WU008) 梟姬 — 每當你失去一張裝備牌後，可摸 2 張牌（issue #195）。
 * <p>
 * 官方技能只界定「失去裝備區裡的牌」、未限定來源，因此自己主動換裝也觸發。
 * v1 自動觸發（嚴格有利不詢問）。
 * <p>
 * 已覆蓋全部失去裝備的路徑：被過河拆橋拆裝備、被順手牽羊偷裝備、主動換裝（武器/防具/±1 馬）、
 * 被反饋取走裝備、被麒麟弓棄馬（單馬直接移除 / 雙馬由攻擊方選一匹）、
 * 被借刀殺人奪武器（不出殺 / 沒有殺兩條）、貫石斧代價棄裝備、制衡棄裝備、主公殺忠臣棄光裝備。
 * 摸牌張數統一由 {@link com.gaas.threeKingdoms.skill.registry.SkillEngine#afterLoseEquipment}
 * 結算（per-card：一次失去 N 張摸 2N）。
 * <p>
 * 制衡那條 hook 目前在正常牌局不可達（制衡是孫權的技、梟姬是孫尚香的，同一玩家不會兩者兼有），
 * 留著是為了讓「失去裝備」的鉤點在所有棄裝備的程式路徑上一致，避免日後新武將或換將機制漏掉。
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

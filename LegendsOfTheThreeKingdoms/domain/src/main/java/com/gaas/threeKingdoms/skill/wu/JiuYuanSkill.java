package com.gaas.threeKingdoms.skill.wu;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.skill.Skill;

/**
 * 孫權 (WU001) 救援 — 主公技。主公孫權瀕死時，其他吳勢力角色對其使用桃的回復效果 +1（issue #184）。
 *
 * ⚠️ issue 原文「任一其他吳國武將瀕死時，可出一張桃給其」描述的是既有瀕死求桃流程
 * （任何玩家本就可在瀕死輪詢中出桃）；本實作採官方標準版語意（桃效果 +1），
 * 已於 SKILLS_PROGRESS / API_DOC 標註差異。實際加成邏輯在 SkillEngine.jiuYuanExtraHeal。
 */
public class JiuYuanSkill implements Skill {

    public static final String GENERAL_ID = General.孫權.getGeneralId();
    public static final String SKILL_NAME = "救援";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }
}

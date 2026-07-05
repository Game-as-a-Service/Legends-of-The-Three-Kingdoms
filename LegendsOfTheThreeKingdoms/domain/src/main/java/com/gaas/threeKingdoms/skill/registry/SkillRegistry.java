package com.gaas.threeKingdoms.skill.registry;

import com.gaas.threeKingdoms.skill.Skill;
import com.gaas.threeKingdoms.skill.shu.GuanXingSkill;
import com.gaas.threeKingdoms.skill.shu.JiJiangSkill;
import com.gaas.threeKingdoms.skill.shu.LongDanSkill;
import com.gaas.threeKingdoms.skill.shu.WuShengSkill;
import com.gaas.threeKingdoms.skill.shu.JiZhiSkill;
import com.gaas.threeKingdoms.skill.shu.RenDeSkill;
import com.gaas.threeKingdoms.skill.shu.TieQiSkill;
import com.gaas.threeKingdoms.skill.shu.KongChengSkill;
import com.gaas.threeKingdoms.skill.shu.MaShuSkill;
import com.gaas.threeKingdoms.skill.shu.PaoXiaoSkill;
import com.gaas.threeKingdoms.skill.shu.QiCaiSkill;
import com.gaas.threeKingdoms.skill.wei.HuJiaSkill;
import com.gaas.threeKingdoms.skill.wei.JianXiongSkill;
import com.gaas.threeKingdoms.skill.wei.FanKuiSkill;
import com.gaas.threeKingdoms.skill.wei.GangLieSkill;
import com.gaas.threeKingdoms.skill.wei.GuiCaiSkill;
import com.gaas.threeKingdoms.skill.wei.LuoShenSkill;
import com.gaas.threeKingdoms.skill.wei.LuoYiSkill;
import com.gaas.threeKingdoms.skill.wei.TianDuSkill;
import com.gaas.threeKingdoms.skill.wei.QingGuoSkill;
import com.gaas.threeKingdoms.skill.wei.TuXiSkill;
import com.gaas.threeKingdoms.skill.wei.YiJiSkill;
import com.gaas.threeKingdoms.skill.wu.FanJianSkill;
import com.gaas.threeKingdoms.skill.wu.GuoSeSkill;
import com.gaas.threeKingdoms.skill.wu.QiXiSkill;
import com.gaas.threeKingdoms.skill.wu.JieYinSkill;
import com.gaas.threeKingdoms.skill.wu.JiuYuanSkill;
import com.gaas.threeKingdoms.skill.wu.LiuLiSkill;
import com.gaas.threeKingdoms.skill.wu.KeJiSkill;
import com.gaas.threeKingdoms.skill.wu.KuRouSkill;
import com.gaas.threeKingdoms.skill.wu.LianYingSkill;
import com.gaas.threeKingdoms.skill.wu.ZhiHengSkill;
import com.gaas.threeKingdoms.skill.wu.XiaoJiSkill;
import com.gaas.threeKingdoms.skill.wu.QianXunSkill;
import com.gaas.threeKingdoms.skill.wu.YingZiSkill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SkillRegistry {
    private static final Map<String, List<Skill>> BY_GENERAL = new HashMap<>();

    static {
        // 魏
        register(new JianXiongSkill());
        register(new HuJiaSkill());
        register(new LuoYiSkill());
        register(new FanKuiSkill());
        register(new GuiCaiSkill());
        register(new GangLieSkill());
        register(new TianDuSkill());
        register(new YiJiSkill());
        register(new LuoShenSkill());
        register(new TuXiSkill());
        register(new QingGuoSkill());
        // 蜀
        register(new PaoXiaoSkill());
        register(new KongChengSkill());
        register(new MaShuSkill());
        register(new JiZhiSkill());
        register(new QiCaiSkill());
        register(new TieQiSkill());
        register(new RenDeSkill());
        register(new GuanXingSkill());
        register(new WuShengSkill());
        register(new LongDanSkill());
        register(new JiJiangSkill());
        // 吳
        register(new YingZiSkill());
        register(new QianXunSkill());
        register(new LianYingSkill());
        register(new XiaoJiSkill());
        register(new ZhiHengSkill());
        register(new KuRouSkill());
        register(new KeJiSkill());
        register(new FanJianSkill());
        register(new JieYinSkill());
        register(new QiXiSkill());
        register(new GuoSeSkill());
        register(new JiuYuanSkill());
        register(new LiuLiSkill());
    }

    private SkillRegistry() {
    }

    public static List<Skill> of(String generalId) {
        return BY_GENERAL.getOrDefault(generalId, List.of());
    }

    /** 全部已註冊技能（WaitingSkillEffectBehavior 依 skillName dispatch 用）。 */
    public static List<Skill> all() {
        return BY_GENERAL.values().stream().flatMap(List::stream).toList();
    }

    private static void register(Skill skill) {
        BY_GENERAL.computeIfAbsent(skill.getGeneralId(), k -> new ArrayList<>()).add(skill);
    }
}

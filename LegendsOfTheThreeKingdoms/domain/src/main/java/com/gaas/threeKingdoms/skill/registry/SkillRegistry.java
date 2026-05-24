package com.gaas.threeKingdoms.skill.registry;

import com.gaas.threeKingdoms.skill.Skill;
import com.gaas.threeKingdoms.skill.wei.HuJiaSkill;
import com.gaas.threeKingdoms.skill.wei.JianXiongSkill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SkillRegistry {
    private static final Map<String, List<Skill>> BY_GENERAL = new HashMap<>();

    static {
        register(new JianXiongSkill());
        register(new HuJiaSkill());
    }

    private SkillRegistry() {
    }

    public static List<Skill> of(String generalId) {
        return BY_GENERAL.getOrDefault(generalId, List.of());
    }

    private static void register(Skill skill) {
        BY_GENERAL.computeIfAbsent(skill.getGeneralId(), k -> new ArrayList<>()).add(skill);
    }
}

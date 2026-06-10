package com.gaas.threeKingdoms.skill.registry;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.skill.Skill;
import com.gaas.threeKingdoms.skill.context.DamageContext;
import com.gaas.threeKingdoms.skill.trigger.BeforeAskDodgeSkill;
import com.gaas.threeKingdoms.skill.trigger.OnDamagedSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SkillEngine {

    private SkillEngine() {
    }

    public static List<DomainEvent> onDamaged(Game game, DamageContext ctx) {
        Player damaged = ctx.damagedPlayer();
        if (damaged == null) {
            return List.of();
        }
        GeneralCard general = damaged.getGeneralCard();
        if (general == null) {
            return List.of();
        }
        List<DomainEvent> events = new ArrayList<>();
        for (Skill skill : SkillRegistry.of(general.getGeneralId())) {
            if (skill instanceof OnDamagedSkill onDamaged) {
                events.addAll(onDamaged.onDamaged(game, ctx));
            }
        }
        return events;
    }

    /**
     * AskDodge 前介入鉤點：iterate damaged 武將綁定的技能，第一個回傳非 empty 的 win。
     * caller 用 {@code intercepted.ifPresentOrElse(events::addAll, () -> events.add(new AskDodgeEvent(...)))}。
     */
    public static Optional<List<DomainEvent>> beforeAskDodge(Game game, Player damaged, Behavior parentBehavior) {
        if (damaged == null) {
            return Optional.empty();
        }
        GeneralCard general = damaged.getGeneralCard();
        if (general == null) {
            return Optional.empty();
        }
        for (Skill skill : SkillRegistry.of(general.getGeneralId())) {
            if (skill instanceof BeforeAskDodgeSkill beforeAsk) {
                Optional<List<DomainEvent>> result = beforeAsk.beforeAskDodge(game, damaged, parentBehavior);
                if (result.isPresent()) {
                    return result;
                }
            }
        }
        return Optional.empty();
    }

    // ===== Batch 1 被動/鎖定技 helper =====

    private static List<Skill> skillsOf(Player player) {
        if (player == null || player.getGeneralCard() == null) {
            return List.of();
        }
        return SkillRegistry.of(player.getGeneralCard().getGeneralId());
    }

    /** 馬術等：自己與其他角色的距離修正（負值 = 縮短），下限保護由 caller 處理。 */
    public static int distanceDeltaToOthers(Player player) {
        int delta = 0;
        for (Skill skill : skillsOf(player)) {
            if (skill instanceof com.gaas.threeKingdoms.skill.trigger.DistanceDeltaSkill d) {
                delta += d.distanceDeltaToOthers();
            }
        }
        return delta;
    }

    /** 咆哮等：出牌階段使用殺是否無次數限制。 */
    public static boolean isKillCountUnlimited(Player player) {
        return skillsOf(player).stream()
                .anyMatch(s -> s instanceof com.gaas.threeKingdoms.skill.trigger.KillCountUnlimitedSkill);
    }

    /** 英姿(+1) / 裸衣(-1)：摸牌階段抽牌數修正。 */
    public static int drawPhaseDelta(Player player) {
        int delta = 0;
        for (Skill skill : skillsOf(player)) {
            if (skill instanceof com.gaas.threeKingdoms.skill.trigger.DrawPhaseDeltaSkill d) {
                delta += d.drawCardDelta();
            }
        }
        return delta;
    }

    /** 英姿等：手牌上限（預設 = HP）。 */
    public static int handCardLimit(Player player) {
        int limit = player.getHP();
        for (Skill skill : skillsOf(player)) {
            if (skill instanceof com.gaas.threeKingdoms.skill.trigger.HandLimitSkill h) {
                limit = Math.max(limit, h.handLimit(player));
            }
        }
        return limit;
    }

    /** 謙遜 / 空城等：target 是否不能成為 card 的目標。 */
    public static boolean isImmuneToCard(Player target, com.gaas.threeKingdoms.handcard.HandCard card) {
        return skillsOf(target).stream()
                .anyMatch(s -> s instanceof com.gaas.threeKingdoms.skill.trigger.TargetImmunitySkill t
                        && t.isImmune(target, card));
    }

    /** 集智等：使用錦囊後額外摸牌數（0 = 不觸發）。 */
    public static int drawCountAfterScrollUsed(Player player) {
        int count = 0;
        for (Skill skill : skillsOf(player)) {
            if (skill instanceof com.gaas.threeKingdoms.skill.trigger.AfterScrollUsedSkill a) {
                count += a.drawCountAfterScrollUsed();
            }
        }
        return count;
    }

    /** 奇才等：使用錦囊是否無距離限制。 */
    public static boolean isScrollRangeUnlimited(Player player) {
        return skillsOf(player).stream()
                .anyMatch(s -> s instanceof com.gaas.threeKingdoms.skill.trigger.ScrollRangeUnlimitedSkill);
    }

    /** 連營等：失去最後一張手牌後摸牌數（0 = 不觸發）。 */
    public static int drawCountAfterLoseLastHandCard(Player player) {
        int count = 0;
        for (Skill skill : skillsOf(player)) {
            if (skill instanceof com.gaas.threeKingdoms.skill.trigger.AfterLoseLastHandCardSkill a) {
                count += a.drawCountAfterLoseLastHandCard();
            }
        }
        return count;
    }

    /** 裸衣等：attacker 對 sourceCard 造成傷害的加成點數。 */
    public static int extraDamage(Game game, Player attacker, com.gaas.threeKingdoms.handcard.HandCard sourceCard) {
        int extra = 0;
        for (Skill skill : skillsOf(attacker)) {
            if (skill instanceof com.gaas.threeKingdoms.skill.trigger.DamageBoostSkill d) {
                extra += d.extraDamage(game, attacker, sourceCard);
            }
        }
        return extra;
    }
}

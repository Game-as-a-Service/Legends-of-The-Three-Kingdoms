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

    /** 激將等：AskKill 前介入鉤點（mirror beforeAskDodge）。 */
    public static Optional<List<DomainEvent>> beforeAskKill(Game game, Player asked, Behavior parentBehavior) {
        for (Skill skill : skillsOf(asked)) {
            if (skill instanceof com.gaas.threeKingdoms.skill.trigger.BeforeAskKillSkill beforeAsk) {
                Optional<List<DomainEvent>> result = beforeAsk.beforeAskKill(game, asked, parentBehavior);
                if (result.isPresent()) {
                    return result;
                }
            }
        }
        return Optional.empty();
    }

    /** 救援：主公孫權瀕死、其他吳勢力出桃 → 額外回復量（0 = 不加成）。 */
    public static int jiuYuanExtraHeal(Player dyingPlayer, Player peachGiver) {
        if (peachGiver == null || peachGiver.equals(dyingPlayer)) {
            return 0;
        }
        boolean hasJiuYuan = skillsOf(dyingPlayer).stream()
                .anyMatch(sk -> sk instanceof com.gaas.threeKingdoms.skill.wu.JiuYuanSkill);
        if (!hasJiuYuan) {
            return 0;
        }
        if (dyingPlayer.getRoleCard() == null
                || dyingPlayer.getRoleCard().getRole() != com.gaas.threeKingdoms.rolecard.Role.MONARCH) {
            return 0;
        }
        if (peachGiver.getFaction() != com.gaas.threeKingdoms.generalcard.Faction.WU) {
            return 0;
        }
        return 1;
    }

    /** 克己：呂蒙本回合未使用過殺 → 略過棄牌階段。 */
    public static boolean canSkipDiscardPhase(Game game, Player player) {
        boolean hasKeJi = skillsOf(player).stream()
                .anyMatch(s -> s instanceof com.gaas.threeKingdoms.skill.wu.KeJiSkill);
        return hasKeJi && !game.getCurrentRound().isKillPlayedThisTurn();
    }

    // ===== Batch 2 受傷/判定觸發技 helper =====

    /** 天妒等：判定牌生效後的處理（取牌等）。 */
    public static List<DomainEvent> afterJudgement(Game game, Player judgementOwner,
                                                   com.gaas.threeKingdoms.handcard.HandCard judgementCard) {
        List<DomainEvent> events = new ArrayList<>();
        for (Skill skill : skillsOf(judgementOwner)) {
            if (skill instanceof com.gaas.threeKingdoms.skill.trigger.AfterJudgementSkill a) {
                events.addAll(a.afterJudgement(game, judgementOwner, judgementCard));
            }
        }
        return events;
    }

    /** 梟姬等：失去裝備後補摸的張數（0 = 不觸發）。 */
    public static int drawCountAfterLoseEquipment(Player player) {
        int count = 0;
        for (Skill skill : skillsOf(player)) {
            if (skill instanceof com.gaas.threeKingdoms.skill.trigger.AfterLoseEquipmentSkill a) {
                count += a.drawCountAfterLoseEquipment();
            }
        }
        return count;
    }

    /**
     * 洛神：回合開始判定 loop — 黑色（黑桃/梅花）收入手牌續判，紅色停。
     * 非甄姬回 empty list；v1 自動觸發。
     */
    public static List<DomainEvent> luoShenJudgementLoop(Game game, Player roundPlayer) {
        boolean hasLuoShen = skillsOf(roundPlayer).stream()
                .anyMatch(s -> s instanceof com.gaas.threeKingdoms.skill.wei.LuoShenSkill);
        if (!hasLuoShen) {
            return List.of();
        }
        List<DomainEvent> events = new ArrayList<>();
        while (true) {
            com.gaas.threeKingdoms.handcard.HandCard judgement = game.drawCardForCardEffect(1).get(0);
            boolean black = judgement.getSuit() == com.gaas.threeKingdoms.handcard.Suit.SPADE
                    || judgement.getSuit() == com.gaas.threeKingdoms.handcard.Suit.CLUB;
            events.add(new com.gaas.threeKingdoms.events.SkillEffectEvent(
                    com.gaas.threeKingdoms.skill.wei.LuoShenSkill.SKILL_NAME,
                    roundPlayer.getId(), black, List.of(judgement.getId()), null));
            if (!black) {
                break;
            }
            game.getGraveyard().removeCard(judgement.getId())
                    .ifPresent(card -> roundPlayer.getHand().addCardToHand(card));
        }
        return events;
    }

    /**
     * 鐵騎：馬超指定殺目標後自動判定（v1 自動，非紅桃 = 目標不能出閃）。
     * 判定事件一律回傳（成功或失敗都要廣播）；successOut[0] 告知 caller 是否生效
     * （生效 → 跳過 AskDodge 直接結算傷害）。非馬超 → 回 empty list 且 successOut[0]=false。
     */
    public static List<DomainEvent> tieQiJudgementEvents(Game game, Player attacker, Player target,
                                                         boolean[] successOut) {
        boolean hasTieQi = skillsOf(attacker).stream()
                .anyMatch(s -> s instanceof com.gaas.threeKingdoms.skill.shu.TieQiSkill);
        if (!hasTieQi) {
            successOut[0] = false;
            return List.of();
        }
        com.gaas.threeKingdoms.handcard.HandCard judgement = game.drawCardForCardEffect(1).get(0);
        boolean success = judgement.getSuit() != com.gaas.threeKingdoms.handcard.Suit.HEART;
        successOut[0] = success;
        List<DomainEvent> events = new ArrayList<>();
        events.add(new com.gaas.threeKingdoms.events.SkillEffectEvent(
                com.gaas.threeKingdoms.skill.shu.TieQiSkill.SKILL_NAME,
                attacker.getId(), success, List.of(judgement.getId()), target.getId()));
        events.addAll(afterJudgement(game, attacker, judgement));
        return events;
    }
}

package com.gaas.threeKingdoms.skill.shu;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingJiJiangResponseBehavior;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.Faction;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.UserCommand;
import com.gaas.threeKingdoms.behavior.behavior.BorrowedSwordBehavior;
import com.gaas.threeKingdoms.handcard.basiccard.VirtualKill;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.skill.registry.SkillEngine;
import com.gaas.threeKingdoms.skill.registry.SkillRegistry;
import com.gaas.threeKingdoms.skill.trigger.ActivePhaseSkill;
import com.gaas.threeKingdoms.skill.trigger.BeforeAskKillSkill;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 劉備 (SHU001) 激將 — 主公技。主公劉備需要出殺時，可令其他蜀勢力武將替其打出殺（issue #173）。
 *
 * Mirror 護駕：攔截 AskKillEvent（南蠻入侵 / 決鬥），依座位順序輪詢其他存活蜀將；
 * 任一人 ACCEPT（交出一張殺）視為劉備打出。全部 DECLINE → fallback AskKillEvent(劉備)。
 * 覆蓋三種需要殺的情境：
 * <ul>
 *   <li>南蠻 / 決鬥 AskKill：自動攔截（{@link #beforeAskKill}）。</li>
 *   <li>出牌階段主動使用殺：劉備 {@code useSkillEffect(激將, ACCEPT, targetPlayerId)}（{@link #activate}）；
 *       攻擊範圍 / 出殺次數以劉備計算，ACCEPT 後計入劉備本回合的殺。</li>
 *   <li>借刀殺人要求劉備出殺：劉備收到 BorrowedSwordEvent 後 {@code useSkillEffect(激將, ACCEPT)}
 *       （{@link #activateForBorrowedSword}）；目標固定為借刀殺人指定的對象。</li>
 * </ul>
 */
public class JiJiangSkill implements BeforeAskKillSkill, ActivePhaseSkill {

    public static final String GENERAL_ID = General.劉備.getGeneralId();
    public static final String SKILL_NAME = "激將";

    @Override
    public String getGeneralId() {
        return GENERAL_ID;
    }

    @Override
    public String getSkillName() {
        return SKILL_NAME;
    }

    @Override
    public Optional<List<DomainEvent>> beforeAskKill(Game game, Player asked, Behavior parentBehavior) {
        if (asked.getRoleCard().getRole() != Role.MONARCH) {
            return Optional.empty();
        }
        List<Player> shuHelpers = otherAliveShuInSeatingOrder(game, asked);
        if (shuHelpers.isEmpty()) {
            return Optional.empty();
        }
        List<String> order = shuHelpers.stream().map(Player::getId).toList();
        WaitingJiJiangResponseBehavior waiting = new WaitingJiJiangResponseBehavior(game, asked, order);
        game.updateTopBehavior(waiting);
        Player first = shuHelpers.get(0);
        game.getCurrentRound().setActivePlayer(first);
        return Optional.of(List.of(new AskSkillEffectEvent(SKILL_NAME, first.getId(), List.of(), asked.getId())));
    }

    /** 出牌階段主動發動：劉備「使用」一張殺，由蜀將代出。 */
    @Override
    public List<DomainEvent> activate(Game game, Player self, String choice, List<String> cardIds, String targetPlayerId) {
        if (!"ACCEPT".equals(choice)) {
            throw new IllegalArgumentException("激將 active use requires choice ACCEPT");
        }
        requireMonarchWithShuHelpers(game, self);
        if (game.getCurrentRound().getCurrentRoundPlayer() == null
                || !game.getCurrentRound().getCurrentRoundPlayer().getId().equals(self.getId())) {
            throw new IllegalStateException("激將 can only be activated in Liu Bei's own turn");
        }
        if (targetPlayerId == null || targetPlayerId.isEmpty()) {
            throw new IllegalArgumentException("激將 active use requires targetPlayerId");
        }
        if (targetPlayerId.equals(self.getId())) {
            throw new IllegalArgumentException("Cannot target self");
        }
        Player target = game.getPlayer(targetPlayerId);
        if (target == null || target.isAlreadyDeath()) {
            throw new IllegalArgumentException("Invalid target: " + targetPlayerId);
        }
        if (!game.isInAttackRange(self, target)) {
            throw new IllegalStateException(String.format("%s is not in attack range", targetPlayerId));
        }
        if (SkillEngine.isImmuneToCard(target, new VirtualKill())) {
            throw new IllegalStateException(String.format(
                    "%s cannot be targeted by Kill (target immunity skill)", targetPlayerId));
        }
        if (game.getCurrentRound().isShowKill() && !(self.getEquipmentWeaponCard() instanceof RepeatingCrossbowCard)
                && !SkillEngine.isKillCountUnlimited(self)) {
            throw new IllegalStateException("Player already played Kill Card");
        }
        return startPolling(game, self, WaitingJiJiangResponseBehavior.Mode.ACTIVE_KILL, targetPlayerId);
    }

    /** 借刀殺人要求劉備出殺時發動：由蜀將代出，目標為借刀殺人指定的攻擊對象。 */
    public List<DomainEvent> activateForBorrowedSword(Game game, Player self, String choice, BorrowedSwordBehavior borrowedSword) {
        if (!"ACCEPT".equals(choice)) {
            throw new IllegalArgumentException("激將 requires choice ACCEPT");
        }
        requireMonarchWithShuHelpers(game, self);
        String borrowedId = (String) borrowedSword.getParam(UserCommand.BORROWED_SWORD_BORROWED_PLAYER_ID.name());
        String attackTargetId = (String) borrowedSword.getParam(UserCommand.BORROWED_SWORD_ATTACK_TARGET_PLAYER_ID.name());
        if (!self.getId().equals(borrowedId) || attackTargetId == null
                || !self.getId().equals(game.getCurrentRound().getActivePlayer().getId())) {
            throw new IllegalStateException(self.getId() + " is not being asked to kill by BorrowedSword");
        }
        return startPolling(game, self, WaitingJiJiangResponseBehavior.Mode.BORROWED_SWORD, attackTargetId);
    }

    /** 劉備是否能發動激將（擁有激將、身為主公、且場上有其他存活蜀將）。 */
    public static boolean canActivate(Game game, Player player) {
        boolean hasSkill = SkillRegistry.of(player.getGeneralCard().getGeneralId()).stream()
                .anyMatch(sk -> sk instanceof JiJiangSkill);
        return hasSkill && player.getRoleCard().getRole() == Role.MONARCH
                && !otherAliveShuInSeatingOrder(game, player).isEmpty();
    }

    private void requireMonarchWithShuHelpers(Game game, Player self) {
        if (self.getRoleCard().getRole() != Role.MONARCH) {
            throw new IllegalStateException("激將 is a lord skill; " + self.getId() + " is not the monarch");
        }
        if (otherAliveShuInSeatingOrder(game, self).isEmpty()) {
            throw new IllegalStateException("激將 requires another alive Shu general");
        }
    }

    private List<DomainEvent> startPolling(Game game, Player liuBei, WaitingJiJiangResponseBehavior.Mode mode,
                                           String targetPlayerId) {
        List<Player> shuHelpers = otherAliveShuInSeatingOrder(game, liuBei);
        List<String> order = shuHelpers.stream().map(Player::getId).toList();
        WaitingJiJiangResponseBehavior waiting =
                new WaitingJiJiangResponseBehavior(game, liuBei, order, 0, mode, targetPlayerId);
        game.updateTopBehavior(waiting);
        Player first = shuHelpers.get(0);
        game.getCurrentRound().setActivePlayer(first);
        List<DomainEvent> events = new ArrayList<>();
        events.add(new AskSkillEffectEvent(SKILL_NAME, first.getId(), List.of(), liuBei.getId()));
        events.add(game.getGameStatusEvent(liuBei.getId() + " 發動激將，詢問蜀勢力是否代出殺"));
        return events;
    }

    private static List<Player> otherAliveShuInSeatingOrder(Game game, Player liuBei) {
        List<Player> helpers = new ArrayList<>();
        Player cursor = liuBei;
        for (int i = 0; i < game.getPlayers().size(); i++) {
            cursor = game.getNextPlayer(cursor);
            if (cursor.equals(liuBei)) break;
            if (cursor.isAlreadyDeath()) continue;
            if (cursor.getFaction() == Faction.SHU) {
                helpers.add(cursor);
            }
        }
        return helpers;
    }
}

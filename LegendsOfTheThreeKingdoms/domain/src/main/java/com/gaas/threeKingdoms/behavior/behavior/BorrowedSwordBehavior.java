package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.UserCommand;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.VirtualKillResponseSource;
import com.gaas.threeKingdoms.events.BorrowedSwordEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.events.WeaponUsurpationEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.VirtualKill;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.EighteenSpanViperSpearCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.WeaponCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.skill.registry.SkillEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.gaas.threeKingdoms.handcard.PlayCard.isKillCard;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;

public class BorrowedSwordBehavior extends Behavior {
    public BorrowedSwordBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        String targetPlayerId = reactionPlayers.get(0);
        playerPlayCardNotUpdateActivePlayer(behaviorPlayer, cardId); // 初次打出借刀殺人，還沒選人
        List<DomainEvent> events = new ArrayList<>();
        events.add(new PlayCardEvent("出借刀殺人", behaviorPlayer.getId(), targetPlayerId, cardId, playType));
        events.add(game.getGameStatusEvent("出借刀殺人"));
        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        Player player = game.getPlayer(playerId);
        if (isKillCard(cardId)) {
            Behavior behavior = new NormalActiveKillBehavior(game, player, List.of(targetPlayerId), player, cardId, playType, player.getHand().getCard(cardId).get());
            isOneRound = true;
            game.updateTopBehavior(behavior);
            return behavior.playerAction();
        } else if (isSkip(playType)) {
            WeaponCard targetWeaponCard = player.getEquipmentWeaponCard();
            player.getEquipment().setWeapon(null);
            behaviorPlayer.getHand().addCardToHand(targetWeaponCard);
            isOneRound = true;
            List<DomainEvent> events = new ArrayList<>();
            events.add(new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType));
            events.add(new WeaponUsurpationEvent(playerId, behaviorPlayer.getId(), targetWeaponCard.getId()));
            // 梟姬：武器被借刀殺人奪走也算失去裝備 → 摸牌（排在 GameStatusEvent 之前，狀態快照才含這幾張）
            events.addAll(SkillEngine.afterLoseEquipment(game, player, 1));
            Round currentRound = game.getCurrentRound();
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
            events.add(game.getGameStatusEvent("跳過"));
            return events;
        }

        throw new IllegalStateException(String.format("Can't play this card:[%s]", cardId));
    }

    @Override
    public void validateBeforeVirtualKillResponse(String playerId, String targetPlayerId) {
        // BorrowedSword 場景需要 targetPlayerId（攻擊目標 C）— 由請求帶入
        if (targetPlayerId == null || targetPlayerId.isEmpty()) {
            throw new IllegalArgumentException("BorrowedSword virtual kill response requires targetPlayerId");
        }
    }

    @Override
    public List<DomainEvent> acceptVirtualKillResponse(String playerId, String targetPlayerId, HandCard virtualKill,
                                                       List<String> discardedCardIds, VirtualKillResponseSource source) {
        // targetPlayerId 已由 validateBeforeVirtualKillResponse 驗證過
        Player attacker = game.getPlayer(playerId);
        Player target = game.getPlayer(targetPlayerId);

        // 棄牌已在 Game 處理；push 殺 behavior，讓 C 進入閃 / 防具流程
        // 與 active path 一致：BorrowedSword(oneRound=true) 留在底，等殺解析完畢一併 pop
        isOneRound = true;
        if (source.isViperSpear()) {
            Behavior behavior = new ViperSpearKillBehavior(
                    game, attacker, List.of(targetPlayerId), target, virtualKill, discardedCardIds);
            game.updateTopBehavior(behavior);
            return behavior.playerAction();
        }
        // 轉化殺（武聖 / 龍膽）：同主動轉化殺 — cardId = 來源牌（事件顯示真實牌），
        // card = 帶來源牌 id 的 VirtualKill（奸雄取來源牌；BehaviorData 依 cardId 還原）
        String sourceCardId = discardedCardIds.get(0);
        NormalActiveKillBehavior behavior = new NormalActiveKillBehavior(
                game, attacker, new ArrayList<>(List.of(targetPlayerId)), attacker,
                sourceCardId, PlayType.ACTIVE.getPlayType(), new VirtualKill(List.of(sourceCardId)));
        game.updateTopBehavior(behavior);
        return behavior.playerActionWithCardAlreadyPlayed(source.statusMessage(game, playerId));
    }

    @Override
    public List<DomainEvent> doBehaviorAction() {
        List<DomainEvent> events = new ArrayList<>();
        String currentPlayerId = (String) getParam(UserCommand.BORROWED_SWORD_PLAYER_ID.name());
        String borrowedPlayerId = (String) getParam(UserCommand.BORROWED_SWORD_BORROWED_PLAYER_ID.name());
        String attackTargetPlayerId = (String) getParam(UserCommand.BORROWED_SWORD_ATTACK_TARGET_PLAYER_ID.name());

        Player borrowedPlayer = game.getPlayer(borrowedPlayerId);

        // B has no Kill (and 不能用丈八蛇矛當殺) → weapon usurpation
        boolean cannotProduceKill = borrowedPlayer.getHand().getCards().stream().noneMatch(card -> card instanceof Kill)
                && !EighteenSpanViperSpearCard.canUseAsKill(borrowedPlayer)
                // 激將：主公劉備無殺仍可請蜀將代出
                && !com.gaas.threeKingdoms.skill.shu.JiJiangSkill.canActivate(game, borrowedPlayer);
        if (cannotProduceKill) {
            WeaponCard targetWeaponCard = borrowedPlayer.getEquipmentWeaponCard();
            borrowedPlayer.getEquipment().setWeapon(null);
            behaviorPlayer.getHand().addCardToHand(targetWeaponCard);
            isOneRound = true;
            events.add(new PlayCardEvent("不出牌", borrowedPlayerId, currentPlayerId, "", PlayType.SKIP.getPlayType()));
            events.add(new WeaponUsurpationEvent(borrowedPlayerId, behaviorPlayer.getId(), targetWeaponCard.getId()));
            // 梟姬：B 無殺可出、武器被直接奪走 → 同樣是失去裝備
            events.addAll(SkillEngine.afterLoseEquipment(game, borrowedPlayer, 1));
            Round currentRound = game.getCurrentRound();
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
            events.add(game.getGameStatusEvent("跳過"));
            return events;
        }

        // B has Kill → BorrowedSwordEvent (B chooses to kill or skip)
        game.getCurrentRound().setActivePlayer(borrowedPlayer);
        events.add(new BorrowedSwordEvent(cardId, borrowedPlayerId, attackTargetPlayerId));
        events.add(game.getGameStatusEvent(String.format("要求 %s 出殺", borrowedPlayerId)));
        return events;
    }
}

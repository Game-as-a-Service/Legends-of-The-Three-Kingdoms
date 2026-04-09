package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 貫石斧效果等待回應
 * 攻擊者透過 playerUseStonePiercingAxeEffect API 選擇：
 *   - DISCARD_TWO：棄兩張牌（任意手牌或裝備）強制命中
 *   - SKIP：放棄效果，殺被閃抵銷
 */
public class WaitingStonePiercingAxeResponseBehavior extends Behavior {

    public WaitingStonePiercingAxeResponseBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers,
                                                    Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    public List<DomainEvent> resolveChoice(String respondingPlayerId, AskStonePiercingAxeEffectEvent.Choice choice, List<String> discardCardIds) {
        if (!behaviorPlayer.getId().equals(respondingPlayerId)) {
            throw new IllegalStateException(
                    String.format("player %s is not the attacker who should respond to StonePiercingAxe effect", respondingPlayerId));
        }

        List<DomainEvent> events = new ArrayList<>();
        Player attackerPlayer = behaviorPlayer;
        String targetPlayerId = reactionPlayers.get(0);
        Player targetPlayer = game.getPlayer(targetPlayerId);

        if (choice == AskStonePiercingAxeEffectEvent.Choice.SKIP) {
            // 不發動，殺被抵銷
            Round currentRound = game.getCurrentRound();
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());

            // 底下的 NormalActiveKillBehavior 也要 pop
            Behavior killBehavior = game.peekTopBehaviorSecondElement().orElse(null);
            if (killBehavior instanceof NormalActiveKillBehavior) {
                killBehavior.setIsOneRound(true);
            }

            isOneRound = true;
            events.add(game.getGameStatusEvent("貫石斧效果取消"));
            return events;
        }

        if (choice != AskStonePiercingAxeEffectEvent.Choice.DISCARD_TWO) {
            throw new IllegalArgumentException("Invalid StonePiercingAxe choice: " + choice);
        }

        // DISCARD_TWO：驗證兩張 cardIds
        if (discardCardIds == null || discardCardIds.size() != 2) {
            throw new IllegalArgumentException("DISCARD_TWO choice requires exactly 2 cardIds");
        }

        // 驗證兩張牌都屬於 A（手牌或裝備區）
        for (String discardCardId : discardCardIds) {
            if (discardCardId == null || discardCardId.isEmpty()) {
                throw new IllegalArgumentException("discardCardId cannot be empty");
            }
            boolean inHand = attackerPlayer.getHand().getCards().stream()
                    .anyMatch(c -> c.getId().equals(discardCardId));
            boolean inEquipment = attackerPlayer.getEquipment().hasThisEquipment(discardCardId);
            if (!inHand && !inEquipment) {
                throw new RuntimeException("Attacker does not have card: " + discardCardId);
            }
        }

        // 棄兩張牌
        for (String discardCardId : discardCardIds) {
            boolean inHand = attackerPlayer.getHand().getCards().stream()
                    .anyMatch(c -> c.getId().equals(discardCardId));
            if (inHand) {
                HandCard discardedCard = attackerPlayer.playCard(discardCardId);
                game.getGraveyard().add(discardedCard);
            } else {
                // 裝備區
                EquipmentCard equipmentCard = attackerPlayer.getEquipment().getAllEquipmentCards().stream()
                        .filter(e -> e.getId().equals(discardCardId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Equipment not found: " + discardCardId));
                attackerPlayer.getEquipment().removeEquipment(discardCardId);
                game.getGraveyard().add(equipmentCard);
            }
        }

        events.add(new StonePiercingAxeTriggerEvent(attackerPlayer.getId(), targetPlayerId, discardCardIds));

        // 強制命中：直接造成傷害
        isOneRound = true;  // 讓自己被 pop
        Round currentRound = game.getCurrentRound();
        int originalHp = targetPlayer.getHP();
        List<DomainEvent> damagedEvents = game.getDamagedEvent(
                targetPlayerId, attackerPlayer.getId(), cardId, card, playType,
                originalHp, targetPlayer, currentRound, Optional.empty());
        events.addAll(damagedEvents);

        // 底下的 NormalActiveKillBehavior 也要 pop（殺已結束）
        Behavior killBehavior = game.peekTopBehaviorSecondElement().orElse(null);
        if (killBehavior instanceof NormalActiveKillBehavior) {
            killBehavior.setIsOneRound(true);
        }

        events.add(game.getGameStatusEvent("貫石斧發動強制命中"));
        return events;
    }
}

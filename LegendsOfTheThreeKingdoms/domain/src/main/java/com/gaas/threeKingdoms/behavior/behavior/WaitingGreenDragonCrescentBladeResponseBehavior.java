package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * 青龍偃月刀效果等待回應
 * 攻擊者透過 playerUseGreenDragonCrescentBladeEffect API 選擇：
 *   - KILL：再出一張殺對同一目標
 *   - SKIP：放棄效果，殺被閃抵銷
 */
public class WaitingGreenDragonCrescentBladeResponseBehavior extends Behavior {

    public WaitingGreenDragonCrescentBladeResponseBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers,
                                                            Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    public List<DomainEvent> resolveChoice(String respondingPlayerId, AskGreenDragonCrescentBladeEffectEvent.Choice choice, String killCardId) {
        // behaviorPlayer 是攻擊者（出殺的人），reactionPlayers 包含攻擊者（因為他要做選擇）
        if (!behaviorPlayer.getId().equals(respondingPlayerId)) {
            throw new IllegalStateException(
                    String.format("player %s is not the attacker who should respond to GreenDragonCrescentBlade effect", respondingPlayerId));
        }

        List<DomainEvent> events = new ArrayList<>();
        Player attackerPlayer = behaviorPlayer;
        String targetPlayerId = reactionPlayers.get(0);

        if (choice == AskGreenDragonCrescentBladeEffectEvent.Choice.SKIP) {
            // 不發動，殺被抵銷
            Round currentRound = game.getCurrentRound();
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());

            // 底下的 NormalActiveKillBehavior 也要設 isOneRound = true 才會被 pop
            Behavior killBehavior = game.peekTopBehaviorSecondElement().orElse(null);
            if (killBehavior instanceof NormalActiveKillBehavior) {
                killBehavior.setIsOneRound(true);
            }

            isOneRound = true;
            events.add(game.getGameStatusEvent("青龍偃月刀效果取消"));
            return events;
        }

        if (choice != AskGreenDragonCrescentBladeEffectEvent.Choice.KILL) {
            throw new IllegalArgumentException("Invalid GreenDragonCrescentBlade choice: " + choice);
        }

        // KILL 選擇：需要 cardId
        if (killCardId == null || killCardId.isEmpty()) {
            throw new IllegalArgumentException("KILL choice requires a killCardId");
        }

        // 驗證該 cardId 是殺且在攻擊者手牌中
        HandCard killCard = attackerPlayer.getHand().getCards().stream()
                .filter(c -> c.getId().equals(killCardId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Attacker does not have card: " + killCardId));
        if (!(killCard instanceof Kill)) {
            throw new RuntimeException("Card " + killCardId + " is not a Kill");
        }

        // 從手牌移除殺 → 墓地
        attackerPlayer.playCard(killCardId);
        game.getGraveyard().add(killCard);

        // 發出 PlayCardEvent + GreenDragonCrescentBladeTriggerEvent
        events.add(new PlayCardEvent("出牌", attackerPlayer.getId(), targetPlayerId, killCardId, com.gaas.threeKingdoms.handcard.PlayType.ACTIVE.getPlayType()));
        events.add(new GreenDragonCrescentBladeTriggerEvent(attackerPlayer.getId(), targetPlayerId, killCardId));

        // 更新底下的 NormalActiveKillBehavior 的 cardId 和 card
        Behavior killBehavior = game.peekTopBehaviorSecondElement().orElse(null);
        if (!(killBehavior instanceof NormalActiveKillBehavior)) {
            throw new IllegalStateException("Expected NormalActiveKillBehavior below WaitingGreenDragonCrescentBladeResponseBehavior");
        }
        NormalActiveKillBehavior normalKill = (NormalActiveKillBehavior) killBehavior;
        normalKill.setCardId(killCardId);
        normalKill.setCard(killCard);
        normalKill.setIsOneRound(false);

        // 目標變回 AskDodge（若有防具先問防具）
        Player targetPlayer = game.getPlayer(targetPlayerId);
        Round currentRound = game.getCurrentRound();
        currentRound.setActivePlayer(targetPlayer);

        if (NormalActiveKillBehavior.isEquipmentHasSpecialEffect(targetPlayer)) {
            currentRound.setStage(Stage.Wait_Equipment_Effect);
            events.add(new AskPlayEquipmentEffectEvent(targetPlayer.getId(), targetPlayer.getEquipment().getArmor(), List.of(targetPlayer.getId())));
        } else {
            events.add(new AskDodgeEvent(targetPlayerId));
        }

        events.add(game.getGameStatusEvent("青龍偃月刀發動"));
        isOneRound = true;  // 讓這個 behavior 從 stack pop，留下 NormalActiveKillBehavior
        return events;
    }
}

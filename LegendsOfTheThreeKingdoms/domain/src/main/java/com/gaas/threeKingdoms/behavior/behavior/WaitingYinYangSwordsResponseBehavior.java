package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * 雌雄雙股劍效果等待回應
 * 目標透過 playerUseYinYangSwordsEffect API 選擇：
 *   - TARGET_DISCARDS：棄一張手牌
 *   - ATTACKER_DRAWS：讓攻擊者摸一張牌
 */
public class WaitingYinYangSwordsResponseBehavior extends Behavior {

    public WaitingYinYangSwordsResponseBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers,
                                                 Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    public List<DomainEvent> resolveChoice(String respondingPlayerId, YinYangSwordsEffectEvent.Choice choice, String discardCardId) {
        if (!reactionPlayers.contains(respondingPlayerId)) {
            throw new IllegalStateException(
                    String.format("player %s is not the one who should respond to YinYangSwords effect", respondingPlayerId));
        }

        List<DomainEvent> events = new ArrayList<>();
        Player targetPlayer = game.getPlayer(respondingPlayerId); // 被殺的人
        Player attackerPlayer = behaviorPlayer; // 出殺的人

        if (choice == YinYangSwordsEffectEvent.Choice.ATTACKER_DRAWS) {
            // Target lets attacker draw 1 card
            DomainEvent drawEvent = game.drawCardToPlayer(attackerPlayer, false, 1);
            events.add(drawEvent);
            events.add(new YinYangSwordsEffectEvent(
                    attackerPlayer.getId(), respondingPlayerId,
                    YinYangSwordsEffectEvent.Choice.ATTACKER_DRAWS, null));
        } else if (choice == YinYangSwordsEffectEvent.Choice.TARGET_DISCARDS) {
            if (discardCardId == null || discardCardId.isEmpty()) {
                throw new IllegalArgumentException("TARGET_DISCARDS choice requires a cardId");
            }
            // playCard will throw if cardId not in hand
            HandCard discardedCard = targetPlayer.playCard(discardCardId);
            game.getGraveyard().add(discardedCard);
            events.add(new YinYangSwordsEffectEvent(
                    attackerPlayer.getId(), respondingPlayerId,
                    YinYangSwordsEffectEvent.Choice.TARGET_DISCARDS, discardCardId));
            // 連營：目標棄掉最後一張手牌 → 先摸牌，再接回殺（摸到的閃可以拿來回應）
            events.addAll(com.gaas.threeKingdoms.skill.registry.SkillEngine.afterLoseHandCard(game, targetPlayer));
        } else {
            throw new IllegalArgumentException("Invalid YinYangSwords choice: " + choice);
        }

        // 雙股劍結算完 → 交回底下的殺：鐵騎已生效則直接結算，否則問防具／閃
        events.addAll(NormalActiveKillBehavior.findIn(game).continueAfterYinYangSwords(targetPlayer));

        events.add(game.getGameStatusEvent("雌雄雙股劍效果結束"));
        isOneRound = true;
        return events;
    }
}

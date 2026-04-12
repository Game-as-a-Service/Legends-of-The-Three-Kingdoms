package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 等待攻擊者回應是否發動雌雄雙股劍效果。
 *
 * ACTIVATE → 進入雌雄雙股劍效果流程（問目標棄牌或讓攻擊者摸牌）
 * SKIP → 跳過效果，進入正常出閃/防具流程
 */
public class WaitingYinYangSwordsActivationBehavior extends Behavior {

    public WaitingYinYangSwordsActivationBehavior(Game game, Player behaviorPlayer,
                                                   List<String> reactionPlayers,
                                                   Player currentReactionPlayer,
                                                   String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    public List<DomainEvent> resolveChoice(String playerId, AskActivateYinYangSwordsEvent.Choice choice) {
        if (!behaviorPlayer.getId().equals(playerId)) {
            throw new IllegalStateException(
                    String.format("Player %s is not the attacker. Attacker is %s", playerId, behaviorPlayer.getId()));
        }

        String targetPlayerId = reactionPlayers.get(0);
        Player targetPlayer = game.getPlayer(targetPlayerId);
        List<DomainEvent> events = new ArrayList<>();

        if (choice == AskActivateYinYangSwordsEvent.Choice.ACTIVATE) {
            if (targetPlayer.getHandSize() == 0) {
                // 目標沒手牌，自動讓攻擊者摸一張牌，進入閃/防具流程
                DomainEvent drawEvent = game.drawCardToPlayer(behaviorPlayer, false, 1);
                events.add(drawEvent);
                addAskDodgeOrEquipmentEffect(events, targetPlayer);
            } else {
                // 目標有手牌，push WaitingYinYangSwordsResponseBehavior 問目標
                game.getCurrentRound().setActivePlayer(targetPlayer);
                game.updateTopBehavior(new WaitingYinYangSwordsResponseBehavior(
                        game, behaviorPlayer, Collections.singletonList(targetPlayerId),
                        targetPlayer, cardId, PlayType.ACTIVE.getPlayType(), card));
                events.add(new AskYinYangSwordsEffectEvent(behaviorPlayer.getId(), targetPlayerId));
            }
        } else {
            // SKIP — 跳過效果，進入正常閃/防具流程
            addAskDodgeOrEquipmentEffect(events, targetPlayer);
        }

        events.add(game.getGameStatusEvent("雌雄雙股劍發動選擇"));
        isOneRound = true;
        return events;
    }

    private void addAskDodgeOrEquipmentEffect(List<DomainEvent> events, Player targetPlayer) {
        game.getCurrentRound().setActivePlayer(targetPlayer);
        if (NormalActiveKillBehavior.isEquipmentHasSpecialEffect(targetPlayer)) {
            game.getCurrentRound().setStage(Stage.Wait_Equipment_Effect);
            events.add(new AskPlayEquipmentEffectEvent(
                    targetPlayer.getId(),
                    targetPlayer.getEquipment().getArmor(),
                    List.of(targetPlayer.getId())));
        } else {
            events.add(new AskDodgeEvent(targetPlayer.getId()));
        }
    }
}

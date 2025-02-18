package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.UserCommand;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.DismantleEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.ArrayList;
import java.util.List;

public class DismantleBehavior extends Behavior {
    public DismantleBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();
        String currentReactionPlayerId = currentReactionPlayer.getId();
        playerPlayCardNotUpdateActivePlayer(behaviorPlayer, cardId);

        events.add(new PlayCardEvent(
                "出牌",
                behaviorPlayer.getId(),
                currentReactionPlayerId,
                cardId,
                playType));
        events.add(game.getGameStatusEvent("發動過河拆橋"));
        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        Integer handCardIndex = (Integer) getParam(UserCommand.CHOOSE_HAND_CARD_INDEX.name());
        Player targetPlayer = game.getPlayer(targetPlayerId);
        List<DomainEvent> events = new ArrayList<>();

        reactionPlayers.remove(0);

        if (handCardIndex != null) {
            List<HandCard> cards = targetPlayer.getHand().getCards();
            if (handCardIndex >= cards.size()) {
                throw new IllegalArgumentException("Hand card index over size");
            }
            HandCard handCard = cards.remove(handCardIndex.intValue());
            events.add(new DismantleEvent(playerId, targetPlayerId, handCard.getId(), String.format("%s 拆掉了 %s 的手牌 %s", playerId, targetPlayerId, handCard.getId())));
            events.add(game.getGameStatusEvent("過河拆橋效果"));
        } else {
            if (!targetPlayer.getEquipment().hasThisEquipment(cardId) && !targetPlayer.hasThisDelayScrollCard(cardId)) {
                throw new IllegalArgumentException("Player doesn't have this cardId in equipment or delayScrollCard");
            }

            if (targetPlayer.getEquipment().hasThisEquipment(cardId)) {
                targetPlayer.getEquipment().removeEquipment(cardId);
                game.getGraveyard().add(card);
                events.add(new DismantleEvent(playerId, targetPlayerId, cardId, String.format("%s 拆掉了 %s 的裝備 %s", playerId, targetPlayerId, cardId)));
                events.add(game.getGameStatusEvent("過河拆橋效果"));
            } else {
                targetPlayer.removeDelayScrollCard(cardId);
                game.getGraveyard().add(card);
                events.add(new DismantleEvent(playerId, targetPlayerId, cardId, String.format("%s 拆掉了 %s 判定區的 %s", playerId, targetPlayerId, cardId)));
                events.add(game.getGameStatusEvent("過河拆橋效果"));
            }
        }
        isOneRound = true;
        return events;
    }


}

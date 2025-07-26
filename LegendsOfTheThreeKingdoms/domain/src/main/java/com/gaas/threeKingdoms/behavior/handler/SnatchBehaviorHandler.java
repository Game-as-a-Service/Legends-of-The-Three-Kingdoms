package com.gaas.threeKingdoms.behavior.handler;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.behavior.behavior.DismantleBehavior;
import com.gaas.threeKingdoms.behavior.behavior.SnatchBehavior;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Snatch;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SnatchBehaviorHandler extends PlayCardBehaviorHandler {
    public SnatchBehaviorHandler(PlayCardBehaviorHandler next, Game game) {
        super(next, game);
    }

    @Override
    protected boolean match(String playerId, String cardId, List<String> targetPlayerId, String playType) {
        Player player = getPlayer(playerId);
        Optional<HandCard> card = getCard(cardId, player);
        return card.filter(handCard -> handCard instanceof Snatch).isPresent();
    }

    @Override
    protected Behavior doHandle(String playerId, String cardId, List<String> targetPlayerIdList, String playType) {
        Player player = game.getPlayer(playerId);
        Player currentReactionPlayer = game.getPlayer(targetPlayerIdList.get(0));

        HandCard card = player.getHand().getCard(cardId).orElseThrow(NoSuchElementException::new);

        String errorMessage = "對象沒手牌或者裝備牌";
        if (currentReactionPlayer.getHandSize() == 0 && !currentReactionPlayer.getEquipment().hasAnyEquipment()) {
            throw new IllegalArgumentException(errorMessage);
        }

        targetPlayerIdList.add(0, playerId);

        return new SnatchBehavior(game, player, targetPlayerIdList, currentReactionPlayer, cardId, playType, card);
    }
}

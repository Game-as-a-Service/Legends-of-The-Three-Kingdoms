package com.gaas.threeKingdoms.behavior.handler;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.behavior.behavior.LightningBehavior;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.player.Player;

import java.util.*;

public class LightningBehaviorHandler extends PlayCardBehaviorHandler {
    public LightningBehaviorHandler(PlayCardBehaviorHandler next, Game game) {
        super(next, game);
    }

    @Override
    protected boolean match(String playerId, String cardId, List<String> targetPlayerId, String playType) {
        Player player = getPlayer(playerId);
        Optional<HandCard> card = getCard(cardId, player);
        return card.filter(handCard -> handCard instanceof Lightning).isPresent();
    }

    @Override
    protected Behavior doHandle(String playerId, String cardId, List<String> targetPlayerIdList, String playType) {
        Player player = game.getPlayer(playerId);
        HandCard card = player.getHand().getCard(cardId).orElseThrow(NoSuchElementException::new);
        targetPlayerIdList = Collections.singletonList(playerId);
        return new LightningBehavior(game, player, targetPlayerIdList, player, cardId, playType, card);
    }
}

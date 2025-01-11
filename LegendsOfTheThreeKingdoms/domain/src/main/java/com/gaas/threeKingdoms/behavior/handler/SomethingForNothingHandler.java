package com.gaas.threeKingdoms.behavior.handler;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.behavior.behavior.SomethingForNothingBehavior;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.scrollcard.SomethingForNothing;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class SomethingForNothingHandler extends PlayCardBehaviorHandler {
    public SomethingForNothingHandler(PlayCardBehaviorHandler next, Game game) {
        super(next, game);
    }

    @Override
    protected boolean match(String playerId, String cardId, List<String> targetPlayerId, String playType) {
        Player player = getPlayer(playerId);
        Optional<HandCard> card = getCard(cardId, player);
        return card.filter(handCard -> handCard instanceof SomethingForNothing).isPresent();
    }

    @Override
    protected Behavior doHandle(String playerId, String cardId, List<String> reactionPlayers, String playType) {
        Player player = game.getPlayer(playerId);

        List<String> players = List.of(playerId);

        HandCard card = player.getHand().getCard(cardId).orElseThrow(NoSuchElementException::new);

        return new SomethingForNothingBehavior(game, player, players, player, cardId, playType, card);
    }
}

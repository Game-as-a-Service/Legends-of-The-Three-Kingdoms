package com.gaas.threeKingdoms.behavior.handler;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.behavior.behavior.BarbarianInvasionBehavior;
import com.gaas.threeKingdoms.behavior.behavior.DuelBehavior;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class DuelBehaviorHandler extends PlayCardBehaviorHandler {

    public DuelBehaviorHandler(PlayCardBehaviorHandler next, Game game) {
        super(next, game);
    }

    @Override
    protected boolean match(String playerId, String cardId, List<String> targetPlayerId, String playType) {
        Player player = getPlayer(playerId);
        Optional<HandCard> card = getCard(cardId, player);
        return card.filter(handCard -> handCard instanceof Duel).isPresent();
    }

    @Override
    protected Behavior doHandle(String playerId, String cardId, List<String> targetPlayerId, String playType) {
        Player player = game.getPlayer(playerId);
        Player currentReactionPlayer = game.getPlayer(targetPlayerId.get(0));

        HandCard card = player.getHand().getCard(cardId).orElseThrow(NoSuchElementException::new);

        targetPlayerId.add(playerId); // 自己也是需要反應者

        return new DuelBehavior(game, player, targetPlayerId, currentReactionPlayer, cardId, playType, card);
    }
}

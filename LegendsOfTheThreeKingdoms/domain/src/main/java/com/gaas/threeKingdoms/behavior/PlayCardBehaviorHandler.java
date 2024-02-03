package com.gaas.threeKingdoms.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public abstract class PlayCardBehaviorHandler {

    protected PlayCardBehaviorHandler next;
    protected Game game;

    public Behavior handle(String playerId, String cardId, List<String> targetPlayerId, String playType) {
        if (match(playerId, cardId, targetPlayerId, playType)) {
            return doHandle(playerId, cardId, targetPlayerId, playType);
        } else if (next != null) {
            return next.handle(playerId,cardId,targetPlayerId,playType);
        }
        return null;
    }

    protected abstract boolean match(String playerId, String cardId, List<String> targetPlayerId, String playType);

    protected abstract Behavior doHandle(String playerId, String cardId, List<String> targetPlayerId, String playType);


    protected Player getPlayer(String playerId) {
        return game.getPlayer(playerId);
    }

    protected Optional<HandCard> getCard(String cardId, Player player) {
        return player.getHand().getCard(cardId);
    }
}
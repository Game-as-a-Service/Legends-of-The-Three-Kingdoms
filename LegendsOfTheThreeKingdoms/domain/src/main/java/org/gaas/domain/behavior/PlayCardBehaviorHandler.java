package org.gaas.domain.behavior;

import org.gaas.domain.Game;
import lombok.AllArgsConstructor;
import java.util.List;

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
}
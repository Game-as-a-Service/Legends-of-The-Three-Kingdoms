package com.gaas.threeKingdoms.effect;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.EffectEvent;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public abstract class EffectHandler {
    protected EffectHandler next;
    protected Game game;

    public List<DomainEvent> handle(String playerId, String cardId, String targetPlayerId, PlayType playType) {
        if (match(playerId, cardId, targetPlayerId, playType)) {
            return doHandle(playerId, cardId, targetPlayerId, playType);
        } else if (next != null) {
            return next.handle(playerId, cardId, targetPlayerId, playType);
        }
        return new ArrayList<>();
    }

    protected abstract boolean match(String playerId, String cardId, String targetPlayerId, PlayType playType);

    protected abstract List<DomainEvent> doHandle(String playerId, String cardId, String targetPlayerId, PlayType playType);

    protected Player getPlayer(String playerId) {
        return game.getPlayer(playerId);
    }
}

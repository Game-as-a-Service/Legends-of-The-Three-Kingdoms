package com.gaas.threeKingdoms.effect;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
public abstract class EquipmentEffectHandler {
    protected EquipmentEffectHandler next;
    protected Game game;

    public List<DomainEvent> handle(String playerId, String cardId, String targetPlayerId, EquipmentPlayType playType) {
        if (match(playerId, cardId, targetPlayerId, playType)) {
            return doHandle(playerId, cardId, targetPlayerId, playType);
        } else if (next != null) {
            return next.handle(playerId, cardId, targetPlayerId, playType);
        }
        return new ArrayList<>();
    }

    protected abstract boolean match(String playerId, String cardId, String targetPlayerId, EquipmentPlayType playType);

    protected abstract List<DomainEvent> doHandle(String playerId, String cardId, String targetPlayerId, EquipmentPlayType playType);

    protected Player getPlayer(String playerId) {
        return game.getPlayer(playerId);
    }
}

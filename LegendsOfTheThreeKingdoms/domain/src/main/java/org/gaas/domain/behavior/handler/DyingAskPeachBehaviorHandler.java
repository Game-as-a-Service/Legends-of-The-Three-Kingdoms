package org.gaas.domain.behavior.handler;

import org.gaas.domain.Game;
import org.gaas.domain.behavior.Behavior;
import org.gaas.domain.behavior.PlayCardBehaviorHandler;
import org.gaas.domain.behavior.behavior.DyingAskPeachBehavior;
import org.gaas.domain.gamephase.GeneralDying;
import org.gaas.domain.player.Player;

import java.util.List;
import java.util.stream.Collectors;

public class DyingAskPeachBehaviorHandler extends PlayCardBehaviorHandler {
    public DyingAskPeachBehaviorHandler(PlayCardBehaviorHandler next, Game game) {
        super(next, game);
    }

    @Override
    protected boolean match(String playerId, String cardId, List<String> targetPlayerId, String playType) {
        return game.getGamePhase() instanceof GeneralDying;
    }

    @Override
    protected Behavior doHandle(String playerId, String cardId, List<String> reactionPlayers, String playType) {
        Player player = game.getPlayer(playerId);

        List<String> players = game.getPlayers().stream()
                .map(Player::getId)
                .collect(Collectors.toList());

        return new DyingAskPeachBehavior(game, player, players, player, cardId, playType, null);
    }
}

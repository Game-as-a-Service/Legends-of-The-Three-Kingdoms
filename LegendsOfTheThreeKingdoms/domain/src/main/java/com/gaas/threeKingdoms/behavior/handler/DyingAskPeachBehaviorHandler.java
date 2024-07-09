package com.gaas.threeKingdoms.behavior.handler;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.DyingAskPeachBehavior;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.gamephase.GeneralDying;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class DyingAskPeachBehaviorHandler extends PlayCardBehaviorHandler {
    public DyingAskPeachBehaviorHandler(PlayCardBehaviorHandler next, Game game) {
        super(next, game);
    }

    @Override
    protected boolean match(String playerId, String cardId, List<String> targetPlayerId, String playType) {
        if (!game.isTopBehaviorEmpty()) {
            Behavior behavior = game.peekTopBehavior();
            if (behavior instanceof DyingAskPeachBehavior) {
                return false;
            }
        }

        return game.getGamePhase() instanceof GeneralDying;
    }

    @Override
    protected Behavior doHandle(String playerId, String cardId, List<String> reactionPlayers, String playType) {
        Player player = getPlayer(playerId);

        List<String> players = game.getPlayers().stream()
                .map(Player::getId)
                .collect(Collectors.toList());

        return new DyingAskPeachBehavior(game, player, players, player, cardId, playType, null);
    }
}

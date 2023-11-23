package com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.handler;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.Behavior;
import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.PlayCardBehaviorHandler;
import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.behavior.DyingAskPeachBehavior;
import com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase.GeneralDying;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;

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

        List<String> otherPlayers = game.getPlayers().stream()
                .map(Player::getId)
                .filter(id -> !id.equals(playerId))
                .collect(Collectors.toList());

        return new DyingAskPeachBehavior(game, player, otherPlayers, player, cardId, playType, null);
    }
}

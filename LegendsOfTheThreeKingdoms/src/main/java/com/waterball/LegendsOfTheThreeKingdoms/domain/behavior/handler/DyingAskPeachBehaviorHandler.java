package com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.handler;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.Behavior;
import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.PlayCardBehaviorHandler;
import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.behavior.DyingAskPeachBehavior;
import com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase.GeneralDying;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;

import java.util.List;

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
        return new DyingAskPeachBehavior(game, player, reactionPlayers, player, cardId, playType, null);
    }
}

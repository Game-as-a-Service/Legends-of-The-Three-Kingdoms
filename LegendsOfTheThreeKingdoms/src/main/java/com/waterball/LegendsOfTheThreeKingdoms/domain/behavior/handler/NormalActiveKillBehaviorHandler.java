package com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.handler;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.Behavior;
import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.PlayCardBehaviorHandler;
import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.behavior.NormalActiveKillBehavior;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;

import java.util.List;

public class NormalActiveKillBehaviorHandler extends PlayCardBehaviorHandler {


    public NormalActiveKillBehaviorHandler(PlayCardBehaviorHandler next, Game game) {
        super(next, game);
    }

    //這個牌型是不是殺、有沒出過殺、是不是當前人員、targetPlayerIds 是不是他可以殺的距離
    @Override
    protected boolean match(String playerId, String cardId, List<String> reactionPlayers, String playType) {
        throwExceptionWhenDistanceTooLong(game.getPlayer(playerId), game.getPlayer(reactionPlayers.get(0)));
        return isPlayedValidCard(cardId);
    }

    private void throwExceptionWhenDistanceTooLong(Player player, Player targetPlayer) {
        if (isDistanceTooLong(player, targetPlayer)) {
            throw new IllegalStateException("Players are not within range.");
        }
    }

    @Override
    protected Behavior doHandle(String playerId, String cardId, List<String> reactionPlayers, String playType) {
        Player player = game.getPlayer(playerId);
        return new NormalActiveKillBehavior(game, player, reactionPlayers, player, cardId, playType, player.getHand().getCard(cardId));
    }


    private boolean isPlayedValidCard(String cardId) {
        return game.getCurrentRound().isPlayedValidCard(cardId);
    }


    private boolean isDistanceTooLong(Player player, Player targetPlayer) {
        return !game.isWithinDistance(player, targetPlayer);
    }
}

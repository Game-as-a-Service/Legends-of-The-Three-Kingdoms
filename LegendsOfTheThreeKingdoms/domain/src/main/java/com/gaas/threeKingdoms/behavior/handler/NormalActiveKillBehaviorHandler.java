package com.gaas.threeKingdoms.behavior.handler;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.PlayCardBehaviorHandler;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;

public class NormalActiveKillBehaviorHandler extends PlayCardBehaviorHandler {


    public NormalActiveKillBehaviorHandler(PlayCardBehaviorHandler next, Game game) {
        super(next, game);
    }

    //這個牌型是不是殺、有沒出過殺、是不是當前人員、targetPlayerIds 是不是他可以殺的距離
    @Override
    protected boolean match(String playerId, String cardId, List<String> reactionPlayers, String playType) {
//        return isPlayedValidCard(cardId) && !isDistanceTooLong(game.getPlayer(playerId), game.getPlayer(reactionPlayers.get(0)));
        return isPlayedValidCard(cardId) && isDistanceTooLong(game.getPlayer(playerId), game.getPlayer(reactionPlayers.get(0)));
    }


    @Override
    protected Behavior doHandle(String playerId, String cardId, List<String> reactionPlayers, String playType) {
        Player player = game.getPlayer(playerId);

        return new NormalActiveKillBehavior(game, player, reactionPlayers, player, cardId, playType, player.getHand().getCard(cardId).get());
    }


    private boolean isPlayedValidCard(String cardId) {
        return game.getCurrentRound().isPlayedValidCard(cardId);
    }

    private boolean isDistanceTooLong(Player player, Player targetPlayer) {
        if (!game.isWithinDistance(player, targetPlayer)) {
            throw new IllegalStateException("Players are not within range.");
        }
        return true;
    }
}

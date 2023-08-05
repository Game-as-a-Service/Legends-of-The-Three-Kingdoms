package com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;

public class Normal extends GamePhase {

    public Normal(Game game) {
        super(game);
    }

    @Override
    public void execute(String playerId, String cardId, String targetPlayerId, String playType) {
        Player player = game.getPlayer(playerId);
        Player targetPlayer = game.getPlayer(targetPlayerId);
        if (!game.isWithinDistance(player, targetPlayer)) {
            throw new IllegalStateException("Players are not within range.");
        }
        if (game.getCurrentRound().checkPlayedCardIsValid(cardId)) {
            HandCard handCard = player.playCard(cardId);
            handCard.effect(targetPlayer);
            game.getGraveyard().add(handCard);
            game.judgementHealthStatus(targetPlayer);
        }
    }
}

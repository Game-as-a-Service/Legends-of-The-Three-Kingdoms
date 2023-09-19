package com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.*;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Normal extends GamePhase {

    public Normal(Game game) {
        super(game);
        this.phaseName  = "Normal";
    }

    @Override
    public List<DomainEvent> playCard(String playerId, String cardId, String targetPlayerId, String playType) {
        Player player = game.getPlayer(playerId);
        Player targetPlayer = game.getPlayer(targetPlayerId);
        if (!game.isWithinDistance(player, targetPlayer)) {
            throw new IllegalStateException("Players are not within range.");
        }

        if (game.getCurrentRound().checkPlayedCardIsValid(cardId)) {

            List<PlayerEvent> playerEvents =  game.getPlayers().stream().map(PlayerEvent::new).toList();
            RoundEvent roundEvent = new RoundEvent(game.getCurrentRound());

            HandCard handCard = player.playCard(cardId);
            game.getGraveyard().add(handCard);
            return List.of(new PlayCardEvent(playerId, targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName()));

            //handCard.effect(targetPlayer);
            //game.judgementHealthStatus(targetPlayer);
        }
        throw new IllegalStateException(String.format("GamePhase Normal execute but player[%s] played card id :[%s] to targetPlay[%s] is not Valid.", playerId, cardId, targetPlayerId));
    }
}

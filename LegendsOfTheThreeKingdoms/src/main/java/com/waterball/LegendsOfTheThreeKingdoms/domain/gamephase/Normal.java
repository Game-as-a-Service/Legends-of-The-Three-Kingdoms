package com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Round;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.*;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayType;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;

import java.util.List;

public class Normal extends GamePhase {

    public Normal(Game game) {
        super(game);
        this.phaseName = "Normal";
    }

    @Override
    public List<DomainEvent> playCard(String playerId, String cardId, String targetPlayerId, String playType) {
        Player player = game.getPlayer(playerId);
        Player targetPlayer = game.getPlayer(targetPlayerId);

        if (isDistanceTooLong(player, targetPlayer)) {
            throw new IllegalStateException("Players are not within range.");
        }

//        if (isSkip(playType)) {
//            game.updateRoundInformation(targetPlayer, cardId);
//            RoundEvent roundEvent = new RoundEvent(game.getCurrentRound());
//            List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
//            PlayCard playCard = game.getCurrentRound().
//            PlayCardEvent playCardEvent = new PlayCardEvent(playerId, targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName());
//            return List.of(playCardEvent, playerDamagedEvent);
//        }

        if (isPlayedValidCard(cardId)) {
            playerPlayCard(player, targetPlayer, cardId);
            Round currentRound = game.getCurrentRound();

            RoundEvent roundEvent = new RoundEvent(currentRound);

            List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
            return List.of(new PlayCardEvent(playerId, targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName()));
        }
        throw new IllegalStateException(String.format("GamePhase Normal execute but player[%s] played card id :[%s] to targetPlay[%s] is not valid.", playerId, cardId, targetPlayerId));
    }

    private boolean isSkip(String playType) {
        return PlayType.SKIP.getPlayType().equals(playType);
    }

    private boolean isDistanceTooLong(Player player, Player targetPlayer) {
        return !game.isWithinDistance(player, targetPlayer);
    }

    private boolean isPlayedValidCard(String cardId) {
        return game.getCurrentRound().isPlayedValidCard(cardId);
    }

    private void playerPlayCard(Player player, Player targetPlayer, String cardId) {
        HandCard handCard = player.playCard(cardId);
        game.updateRoundInformation(targetPlayer, cardId);
        game.getGraveyard().add(handCard);
    }

}

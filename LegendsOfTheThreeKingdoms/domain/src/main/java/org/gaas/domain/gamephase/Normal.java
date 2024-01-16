package org.gaas.domain.gamephase;

import org.gaas.domain.Game;
import org.gaas.domain.Round;
import org.gaas.domain.events.*;
import org.gaas.domain.handcard.HandCard;
import org.gaas.domain.handcard.PlayType;
import org.gaas.domain.player.Player;

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

        // 殺 -> 閃 or Skip
        // 錦囊舖? -> 殺 or 閃 or ??
        // 武將效果 ->


        // 責任鏈的條件是前一張牌是哪張(動作)


        if (isPlayedValidCard(cardId)) {
            playerPlayCard(player, targetPlayer, cardId);
            Round currentRound = game.getCurrentRound();

            RoundEvent roundEvent = new RoundEvent(currentRound);

            List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
            return List.of(new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName()));
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
        game.updateRoundInformation(targetPlayer, handCard);
        game.getGraveyard().add(handCard);
    }

}

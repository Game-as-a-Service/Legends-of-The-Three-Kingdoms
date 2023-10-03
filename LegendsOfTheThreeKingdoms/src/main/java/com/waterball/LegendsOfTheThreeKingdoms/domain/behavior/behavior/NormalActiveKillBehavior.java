package com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.behavior;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Round;
import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.Behavior;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.DomainEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.PlayCardEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.PlayerEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.RoundEvent;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayType;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;

import java.util.List;


public class NormalActiveKillBehavior extends Behavior {
    public NormalActiveKillBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType);
    }

    @Override
    public List<DomainEvent> askTargetPlayerPlayCard() {
        String targetPlayerId = reactionPlayers.get(0);
        playerPlayCard(behaviorPlayer, game.getPlayer(targetPlayerId), cardId);
        Round currentRound = game.getCurrentRound();

        RoundEvent roundEvent = new RoundEvent(currentRound);

        List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
        return List.of(new PlayCardEvent(behaviorPlayer.getId(), targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName()));
    }

    @Override
    public List<DomainEvent> acceptedTargetPlayerPlayCard(String playerId, String targetPlayerIdString, String cardId, String playType) {
        throwExceptionWhenPlayerIsNotInReactionPlayers(playerId);
        return null;
    }

    private void throwExceptionWhenPlayerIsNotInReactionPlayers(String playerId) {
        if (!reactionPlayers.contains(playerId)) {
            throw new IllegalStateException();
        }
    }

    private void playerPlayCard(Player player, Player targetPlayer, String cardId) {
        HandCard handCard = player.playCard(cardId);
        game.updateRoundInformation(targetPlayer, handCard);
        game.getGraveyard().add(handCard);
    }

    private boolean isSkip(String playType) {
        return PlayType.SKIP.getPlayType().equals(playType);
    }
}

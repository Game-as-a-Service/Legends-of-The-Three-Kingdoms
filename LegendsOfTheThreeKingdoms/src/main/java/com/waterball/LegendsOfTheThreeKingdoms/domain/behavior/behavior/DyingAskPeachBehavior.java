package com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.behavior;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Round;
import com.waterball.LegendsOfTheThreeKingdoms.domain.behavior.Behavior;
import com.waterball.LegendsOfTheThreeKingdoms.domain.events.*;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayType;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;

import java.util.List;

import static com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayCard.isPeachCard;

public class DyingAskPeachBehavior extends Behavior {
    public DyingAskPeachBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, false);
    }

    @Override
    public List<DomainEvent> askTargetPlayerPlayCard() {
        return null;
    }

    @Override
    protected List<DomainEvent> doAcceptedTargetPlayerPlayCard(String playerId, String targetPlayerId, String cardId, String playType) {
        Player dyingPlayer = game.getPlayer(targetPlayerId);
        Player currentPlayer = game.getPlayer(playerId);

        if (isSkip(playType)) {
            List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
            AskPeachEvent askPeachEvent = createAskPeachEvent(game.getNextPlayer(currentPlayer));
            if (reactionPlayers.get(reactionPlayers.size() - 1).equals(playerId)) {
                isNeedToPop = true;
            }
            Round currentRound = game.getCurrentRound();
            currentRound.setActivePlayer(game.getNextPlayer(currentPlayer));
            RoundEvent roundEvent = new RoundEvent(currentRound);
            PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName());

            return List.of(playCardEvent, askPeachEvent);
        } else if (isPeachCard(cardId)) {
//            dyingPlayer.playCard(cardId);
//            RoundEvent roundEvent = new RoundEvent(game.getCurrentRound());
//            PlayerDamagedEvent playerDamagedEvent = createPlayerDamagedEvent(originalHp, dyingPlayer);
//            List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
//            PlayCardEvent playCardEvent = new PlayCardEvent("出牌", playerId, targetPlayerId, cardId, playType, game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName());
//            return List.of(playCardEvent, playerDamagedEvent);
            return null;
        } else {
            //TODO:怕有其他效果或殺的其他case
            return null;
        }
    }

    private static String findNextPlayer(List<String> players, String currentPlayer) {
        int currentIndex = players.indexOf(currentPlayer);
        if (currentIndex != -1 && currentIndex < players.size() - 1) {
            return players.get(currentIndex + 1);
        }
        return null;
    }

    private AskPeachEvent createAskPeachEvent(Player player) {
        return new AskPeachEvent(player.getId());
    }

    private void playerPlayCard(Player player, Player targetPlayer, String cardId) {
        HandCard handCard = player.playCard(cardId);
        card = handCard;
        game.updateRoundInformation(targetPlayer, handCard);
        game.getGraveyard().add(handCard);
    }

    private boolean isSkip(String playType) {
        return PlayType.SKIP.getPlayType().equals(playType);
    }
}

package com.gaas.threeKingdoms.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.events.DomainEvent;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public abstract class Behavior {
    protected Game game;
    protected Player behaviorPlayer;
    protected List<String> reactionPlayers;
    protected Player currentReactionPlayer;
    protected String cardId;
    protected String playType;
    protected HandCard card;
    protected boolean isNeedToPush = true;
    protected boolean isNeedToPop = true;
    public abstract List<DomainEvent> askTargetPlayerPlayCard();

    public List<DomainEvent> acceptedTargetPlayerPlayCard(String playerId, String targetPlayerIdString, String cardId, String playType){
       throwExceptionWhenPlayerIsNotInReactionPlayers(playerId);
       return doAcceptedTargetPlayerPlayCard(playerId,targetPlayerIdString,cardId,playType);
    }

    protected void throwExceptionWhenPlayerIsNotInReactionPlayers(String playerId) {
        if (!reactionPlayers.contains(playerId)) {
            throw new IllegalStateException();
        }
    }

    protected abstract List<DomainEvent> doAcceptedTargetPlayerPlayCard(String playerId, String targetPlayerId, String cardId, String playType);

    protected void playerPlayCard(Player player, Player targetPlayer, String cardId) {
        HandCard handCard = player.playCard(cardId);
        card = handCard;
        game.updateRoundInformation(targetPlayer, handCard);
        game.getGraveyard().add(handCard);
    }

    public boolean isNeedToPop() {
        return isNeedToPop;
    }

    public boolean isNeedToPush() {
        return isNeedToPush;
    }
}

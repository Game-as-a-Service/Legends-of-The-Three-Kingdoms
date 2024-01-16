package org.gaas.domain.behavior;

import org.gaas.domain.Game;
import org.gaas.domain.events.DomainEvent;
import org.gaas.domain.handcard.HandCard;
import org.gaas.domain.player.Player;
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

    protected abstract List<DomainEvent> doAcceptedTargetPlayerPlayCard(String playerId, String targetPlayerIdString, String cardId, String playType);

    public boolean isNeedToPop() {
        return isNeedToPop;
    }

}

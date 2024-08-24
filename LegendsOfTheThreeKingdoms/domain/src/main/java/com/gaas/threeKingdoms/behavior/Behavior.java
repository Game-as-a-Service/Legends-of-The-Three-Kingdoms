package com.gaas.threeKingdoms.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.events.DomainEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class Behavior {
    protected Game game;
    protected Player behaviorPlayer; // 發動這個效果的人
    protected List<String> reactionPlayers; // 受到效果、需要反應者
    protected Player currentReactionPlayer; // 當前需要反應者
    protected String cardId;
    protected String playType;  //com.gaas.threeKingdoms.handcard.PlayType
    protected HandCard card;
    protected boolean isTargetPlayerNeedToResponse = true; // 別人要不要反應
    protected boolean isOneRound = true; // 是不是一回合就結束

    // hook
    public List<DomainEvent> playerAction() {
        return null;
    };

    // hook
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        return null;
    }

    public List<DomainEvent> responseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType){
        throwExceptionWhenPlayerIsNotInReactionPlayers(playerId);
        return doResponseToPlayerAction(playerId,targetPlayerId,cardId,playType);
    }

    private void throwExceptionWhenPlayerIsNotInReactionPlayers(String playerId) {
        if (!reactionPlayers.contains(playerId)) {
            throw new IllegalStateException();
        }
    }

    protected void playerPlayCard(Player player, Player targetPlayer, String cardId) {
        HandCard handCard = player.playCard(cardId);
        card = handCard;
        game.updateRoundInformation(targetPlayer, handCard);
        game.getGraveyard().add(handCard);
    }

    protected void playerPlayCardNotUpdateActivePlayer(Player player, String cardId) {
        HandCard handCard = player.playCard(cardId);
        card = handCard;
        game.getCurrentRound().setCurrentPlayCard(card);
        game.getGraveyard().add(handCard);
    }

    public boolean isOneRound() {
        return isOneRound;
    }

    public void setIsOneRound(boolean isOneRound) {
        this.isOneRound = isOneRound;
    }

    public boolean isTargetPlayerNeedToResponse() {
        return isTargetPlayerNeedToResponse;
    }
}

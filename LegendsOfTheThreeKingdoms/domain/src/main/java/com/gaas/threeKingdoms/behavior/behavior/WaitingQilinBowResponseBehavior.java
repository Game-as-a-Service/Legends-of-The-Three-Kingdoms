package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WaitingQilinBowResponseBehavior extends Behavior {

    public WaitingQilinBowResponseBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, false, false, false);
    }

    @Override
    public List<DomainEvent> playerAction() {

        return Collections.emptyList();
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        PlayCard playCard = PlayCard.valueOf(cardId);
        if (!playCard.isMountsCard()) {
            throw new IllegalStateException("playCard is not mounts playCard type");
        }

        Player damagedPlayer = game.getPlayer(targetPlayerId);
        if (damagedPlayer.getEquipmentMinusOneMountsCard().getId().equals(cardId) && damagedPlayer.getEquipmentPlusOneMountsCard().getId().equals(cardId)) {
            throw new IllegalStateException("player doesn't equip this mount playCard");
        }
        if (!this.getBehaviorPlayer().getId().equals(playerId)) {
            throw new IllegalStateException("player is not QilinBowBehavior player");
        }
        HandCard mountsCard = damagedPlayer.getMountsCard(cardId);
        DomainEvent removeHorseEvent = damagedPlayer.removeMountsCard(playerId, cardId);
        game.getGraveyard().add(mountsCard);
        game.getCurrentRound().setStage(Stage.Normal);
        isOneRound = true;
        return Collections.singletonList(removeHorseEvent);
    }

}

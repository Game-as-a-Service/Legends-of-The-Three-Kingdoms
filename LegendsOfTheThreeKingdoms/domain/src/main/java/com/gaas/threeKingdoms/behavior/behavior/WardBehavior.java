package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.AskPlayWardEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.WaitForWardEvent;
import com.gaas.threeKingdoms.events.WardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WardBehavior extends Behavior {
    public WardBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card, boolean isTargetPlayerNeedToResponse, boolean isOneRound) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, isTargetPlayerNeedToResponse, isOneRound);
    }

    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();
        game.getPlayers().forEach(player -> {
                    DomainEvent wardEvent;
                    if (player.getHand().getCards().stream().anyMatch(card -> card instanceof Ward)) {
                        wardEvent = new AskPlayWardEvent(player.getId());
                    } else {
                        wardEvent = new WaitForWardEvent(player.getId());
                    }
                    events.add(wardEvent);
                }
        );
        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        Round currentRound = game.getCurrentRound();
        Stage currentStage = currentRound.getStage();
        if (!currentStage.equals(Stage.Wait_Accept_Ward_Effect)) {
            throw new IllegalStateException(String.format("CurrentRound stage not Wait_Accept_Ward_Effect but [%s]", currentStage));
        }
        currentRound.setStage(Stage.Normal);
        WardEvent wardEvent = new WardEvent(playerId, targetPlayerId, this.cardId, cardId);

        return List.of(wardEvent);
    }


}

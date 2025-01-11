package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.DrawCardEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.events.SomethingForNothingEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.Collections;
import java.util.List;

public class SomethingForNothingBehavior extends Behavior {

    public SomethingForNothingBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, false, true);
    }

    @Override
    public List<DomainEvent> playerAction() {
        playerPlayCardNotUpdateActivePlayer(behaviorPlayer, cardId);
        SomethingForNothingEvent somethingForNothingEvent = new SomethingForNothingEvent(behaviorPlayer.getId());
        DomainEvent drawCardEvent = game.drawCardToPlayer(behaviorPlayer, false);
        PlayCardEvent playCardEvent = new PlayCardEvent(
                "出牌",
                behaviorPlayer.getId(),
                behaviorPlayer.getId(),
                cardId,
                playType);
        return List.of(playCardEvent, somethingForNothingEvent, drawCardEvent, game.getGameStatusEvent(somethingForNothingEvent.getMessage()));
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        return Collections.emptyList();
    }
}

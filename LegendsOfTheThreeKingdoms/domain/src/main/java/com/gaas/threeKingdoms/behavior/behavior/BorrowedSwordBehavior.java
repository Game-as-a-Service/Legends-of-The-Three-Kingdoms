package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.ArrayList;
import java.util.List;

public class BorrowedSwordBehavior extends Behavior {
    public BorrowedSwordBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false);
    }


    @Override
    public List<DomainEvent> playerAction() {
        String targetPlayerId = reactionPlayers.get(0);
        playerPlayCardNotUpdateActivePlayer(behaviorPlayer, cardId);
        List<DomainEvent> events = new ArrayList<>();
        events.add(new PlayCardEvent("出借刀殺人", behaviorPlayer.getId(), targetPlayerId, cardId, playType));
        events.add(game.getGameStatusEvent("出借刀殺人"));
        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {

        return super.doResponseToPlayerAction(playerId, targetPlayerId, cardId, playType);
    }
}

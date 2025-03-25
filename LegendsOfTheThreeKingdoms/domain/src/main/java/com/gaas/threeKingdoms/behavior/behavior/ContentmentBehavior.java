package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.scrollcard.ScrollCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.ArrayList;
import java.util.List;

public class ContentmentBehavior extends Behavior {

    public ContentmentBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, false, true);
    }

    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();
        String currentReactionPlayerId = currentReactionPlayer.getId();
        playerPlayCardNotUpdateActivePlayer(behaviorPlayer, cardId);
        currentReactionPlayer.addDelayScrollCard((ScrollCard) card);
        events.add(new PlayCardEvent(
                "出牌",
                behaviorPlayer.getId(),
                currentReactionPlayerId,
                cardId,
                playType));
        events.add(game.getGameStatusEvent("發動樂不思蜀"));
        return events;
    }

}

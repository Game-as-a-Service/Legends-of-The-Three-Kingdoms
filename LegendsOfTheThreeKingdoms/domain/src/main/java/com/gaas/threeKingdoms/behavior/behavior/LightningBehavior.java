package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.LightningTransferredEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.scrollcard.ScrollCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.ArrayList;
import java.util.List;

public class LightningBehavior extends Behavior {

    public LightningBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, false, true, false);
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
        events.add(new LightningTransferredEvent(
                behaviorPlayer.getId(),
                currentReactionPlayerId,
                cardId, String.format("閃電從 %s 轉移至 %s", behaviorPlayer.getGeneralName(), behaviorPlayer.getGeneralName())));
        events.add(game.getGameStatusEvent("發動閃電"));
        return events;
    }
}
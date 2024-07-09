package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.GeneralDying;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;

import java.util.ArrayList;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.isKillCard;
import static com.gaas.threeKingdoms.handcard.PlayCard.isSkip;

public class BarbarianInvasionBehavior extends Behavior {
    public BarbarianInvasionBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();
        String currentReactionPlayerId = currentReactionPlayer.getId();
        playerPlayCard(behaviorPlayer, currentReactionPlayer, cardId);

        events.add(new PlayCardEvent(
                "出牌",
                behaviorPlayer.getId(),
                currentReactionPlayerId,
                cardId,
                playType));
        events.add(new AskKillEvent(currentReactionPlayerId));
        events.add(game.getGameStatusEvent("發動南蠻入侵"));

        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {

        if (isSkip(playType)) {
            int originalHp = currentReactionPlayer.getHP();
            List<DomainEvent> damagedEvent = game.getDamagedEvent(playerId, targetPlayerId, cardId, card, playType, originalHp, currentReactionPlayer, game.getCurrentRound(), this);
            // Remove the current player to next player
            currentReactionPlayer = game.getNextPlayer(currentReactionPlayer);

            List<DomainEvent> events = new ArrayList<>(damagedEvent);

            if (!game.getGamePhase().getPhaseName().equals("GeneralDying")) {
                events.add(new AskKillEvent(currentReactionPlayer.getId()));
            }
            return events;
        } else if (isKillCard(card.getId())) {
            List<DomainEvent> events = new ArrayList<>();
            String currentReactionPlayerId = currentReactionPlayer.getId();
            currentReactionPlayer = game.getNextPlayer(currentReactionPlayer);
            events.add(new AskKillEvent(currentReactionPlayerId));
            events.add(game.getGameStatusEvent(playerId + "出skip"));
            return events;
        } else {
            //TODO:怕有其他效果或殺的其他case
        }
        return null;
    }

}

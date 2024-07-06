package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
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

        Round currentRound = game.getCurrentRound();
        RoundEvent roundEvent = new RoundEvent(currentRound);
        List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
        events.add(new PlayCardEvent(
                "出牌",
                behaviorPlayer.getId(),
                currentReactionPlayerId,
                cardId,
                playType,
                game.getGameId(),
                playerEvents,
                roundEvent,
                game.getGamePhase().getPhaseName()));
        events.add(new AskKillEvent(currentReactionPlayerId));
        events.add(game.getGameStatusEvent("發動南蠻入侵"));

        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {

        if (isSkip(playType)) {
            int originalHp = currentReactionPlayer.getHP();
            card.effect(currentReactionPlayer);
            List<DomainEvent> events = new ArrayList<>();
            PlayerDamagedEvent playerDamagedEvent = createPlayerDamagedEvent(originalHp, currentReactionPlayer);
            events.add(playerDamagedEvent);

            // Remove the current player to next player
            currentReactionPlayer = game.getNextPlayer(currentReactionPlayer);
            events.add(new AskKillEvent(currentReactionPlayer.getId()));
            events.add(game.getGameStatusEvent(playerId + "出skip"));
            PlayCardEvent playCardEvent = new PlayCardEvent("不出牌", playerId, targetPlayerId, cardId, playType, game.getGameId(), null, null, game.getGamePhase().getPhaseName());
            events.add(playCardEvent);
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

    private PlayerDamagedEvent createPlayerDamagedEvent(int originalHp, Player damagedPlayer) {
        return new PlayerDamagedEvent(damagedPlayer.getId(), originalHp, damagedPlayer.getHP());
    }
}

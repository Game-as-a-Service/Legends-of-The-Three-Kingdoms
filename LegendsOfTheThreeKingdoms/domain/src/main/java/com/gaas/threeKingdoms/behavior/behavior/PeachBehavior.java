package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.Round;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.events.PlayerEvent;
import com.gaas.threeKingdoms.events.RoundEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;

public class PeachBehavior extends Behavior {

    public PeachBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true);
    }

    @Override
    public List<DomainEvent> askTargetPlayerPlayCard() {
        playerPlayCard(behaviorPlayer, behaviorPlayer, cardId);
        card.effect(behaviorPlayer);
        Round currentRound = game.getCurrentRound();
        RoundEvent roundEvent = new RoundEvent(currentRound);
        List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
        return List.of(new PlayCardEvent("出牌",
                                                 behaviorPlayer.getId(),
                                                 behaviorPlayer.getId(),
                                                 cardId,
                                                 playType,
                                                 game.getGameId(),
                                                 playerEvents,
                                                 roundEvent,
                                                 game.getGamePhase().getPhaseName()));
    }

    @Override
    protected List<DomainEvent> doAcceptedTargetPlayerPlayCard(String playerId, String targetPlayerIdString, String cardId, String playType) {
        return null;
    }

}

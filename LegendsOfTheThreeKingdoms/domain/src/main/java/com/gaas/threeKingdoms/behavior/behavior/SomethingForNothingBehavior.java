package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.events.SomethingForNothingEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SomethingForNothingBehavior extends Behavior {

    public SomethingForNothingBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, true);
    }

    @Override
    public List<DomainEvent> playerAction() {
        playerPlayCardNotUpdateActivePlayer(behaviorPlayer, cardId);
        PlayCardEvent playCardEvent = new PlayCardEvent(
                "出牌",
                behaviorPlayer.getId(),
                behaviorPlayer.getId(),
                cardId,
                playType);

        List<DomainEvent> domainEvents = new ArrayList<>();
        domainEvents.add(playCardEvent);

        if (game.doesAnyPlayerHaveWard()) {

            game.getCurrentRound().setStage(Stage.Wait_Accept_Ward_Effect);
            setIsOneRound(false);

            Behavior wardBehavior = new WardBehavior(
                    game,
                    null,
                    game.whichPlayersHaveWard().stream().map(Player::getId).collect(Collectors.toList()),
                    null,
                    cardId,
                    PlayType.INACTIVE.getPlayType(),
                    card,
                    true
            );

            game.updateTopBehavior(wardBehavior);
            domainEvents.addAll(wardBehavior.playerAction());
        } else {
            domainEvents.addAll(doBehaviorAction());
        }
        domainEvents.add(game.getGameStatusEvent(""));
        return domainEvents;
    }

    @Override
    public List<DomainEvent> doBehaviorAction() {
        List<DomainEvent> domainEvents = new ArrayList<>();
        domainEvents.add(new SomethingForNothingEvent(behaviorPlayer.getId()));
        domainEvents.add(game.drawCardToPlayer(behaviorPlayer, false));
        return domainEvents;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        return Collections.emptyList();
    }

}

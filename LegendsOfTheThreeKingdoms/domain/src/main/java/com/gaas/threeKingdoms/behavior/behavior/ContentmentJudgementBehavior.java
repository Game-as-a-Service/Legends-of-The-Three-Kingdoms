package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.ContentmentEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TARGET_PLAYER_IDS;
import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TRIGGER_PLAYER_ID;

public class ContentmentJudgementBehavior extends Behavior {

    public ContentmentJudgementBehavior(Game game, Player affectedPlayer, List<String> reactionPlayers,
                                        Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, affectedPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();

        game.getCurrentRound().setStage(Stage.Wait_Accept_Ward_Effect);
        setIsOneRound(false);

        Behavior wardBehavior = new WardBehavior(
                game, null,
                game.whichPlayersHaveWard(null).stream()
                        .map(Player::getId).collect(Collectors.toList()),
                null, cardId, PlayType.INACTIVE.getPlayType(), card, true
        );
        wardBehavior.putParam(WARD_TRIGGER_PLAYER_ID, null);
        wardBehavior.putParam(WARD_TARGET_PLAYER_IDS, List.of(behaviorPlayer.getId()));
        game.updateTopBehavior(wardBehavior);

        events.addAll(wardBehavior.playerAction());
        return events;
    }

    @Override
    public List<DomainEvent> doBehaviorAction() {
        // Ward count is EVEN -> effect proceeds -> execute judgement
        List<DomainEvent> events = new ArrayList<>();
        ContentmentEvent contentmentEvent = game.handleContentmentJudgement(behaviorPlayer);
        events.add(contentmentEvent);

        // Remove self from stack before resuming judgement flow
        isOneRound = true;
        game.removeCompletedBehaviors();

        game.getCurrentRound().setStage(Stage.Normal);
        game.getCurrentRound().setActivePlayer(behaviorPlayer);

        events.addAll(game.continueJudgementAndDraw(behaviorPlayer, contentmentEvent.isSuccess()));

        return events;
    }

    @Override
    public List<DomainEvent> doWardCancelledAction() {
        // Ward count is ODD -> effect cancelled -> skip judgement
        // Contentment card was already popped from delayCards in judgePlayerShouldDelay()
        List<DomainEvent> events = new ArrayList<>();

        // Remove self from stack before resuming judgement flow
        isOneRound = true;
        game.removeCompletedBehaviors();

        game.getCurrentRound().setStage(Stage.Normal);
        game.getCurrentRound().setActivePlayer(behaviorPlayer);

        events.addAll(game.continueJudgementAndDraw(behaviorPlayer, false));

        return events;
    }
}

package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.LightningTransferredEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.scrollcard.ScrollCard;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TARGET_PLAYER_IDS;
import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TRIGGER_PLAYER_ID;

/**
 * Behavior for Lightning judgement during the Judgement phase.
 * Mirrors ContentmentJudgementBehavior but with Lightning-specific logic:
 * - Ward cancelled (odd): Lightning is NOT discarded, instead transferred to next player
 * - Ward proceeds (even) / all skip: Normal Lightning judgement executes
 */
public class LightningJudgementBehavior extends Behavior {

    public LightningJudgementBehavior(Game game, Player affectedPlayer, List<String> reactionPlayers,
                                       Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, affectedPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();

        // All players (including the affected player) can play Ward on Lightning
        // Pass null to not exclude anyone
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
        // Ward count is EVEN or all skipped -> effect proceeds -> execute Lightning judgement
        List<DomainEvent> events = new ArrayList<>();
        List<DomainEvent> lightningEvents = game.handleLightningJudgement((ScrollCard) card, behaviorPlayer);
        events.addAll(lightningEvents);

        // Remove self from stack before resuming judgement flow
        isOneRound = true;
        game.removeCompletedBehaviors();

        game.getCurrentRound().setStage(Stage.Normal);
        game.getCurrentRound().setActivePlayer(behaviorPlayer);

        // Continue remaining delay scroll judgements and draw phase
        // Lightning judgement does not cause a Contentment success, so pass false
        events.addAll(game.continueJudgementAndDraw(behaviorPlayer, false));

        return events;
    }

    @Override
    public List<DomainEvent> doWardCancelledAction() {
        // Ward count is ODD -> effect cancelled
        // Key difference from Contentment: Lightning is NOT discarded, it transfers to next player
        List<DomainEvent> events = new ArrayList<>();

        Player nextPlayer = game.getNextPlayer(behaviorPlayer);
        nextPlayer.addDelayScrollCard((ScrollCard) card);
        events.add(new LightningTransferredEvent(
                behaviorPlayer.getId(),
                nextPlayer.getId(),
                card.getId(),
                String.format("閃電從 %s 轉移至 %s", behaviorPlayer.getGeneralName(), nextPlayer.getGeneralName())
        ));

        // Remove self from stack before resuming judgement flow
        isOneRound = true;
        game.removeCompletedBehaviors();

        game.getCurrentRound().setStage(Stage.Normal);
        game.getCurrentRound().setActivePlayer(behaviorPlayer);

        // Continue remaining delay scroll judgements and draw phase
        events.addAll(game.continueJudgementAndDraw(behaviorPlayer, false));

        return events;
    }
}

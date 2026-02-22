package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PeachGardenBehavior extends Behavior {

    public PeachGardenBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, false, true, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        playerPlayCardNotUpdateActivePlayer(behaviorPlayer, cardId);

        List<DomainEvent> events = new ArrayList<>();
        events.add(new PlayCardEvent("出牌", behaviorPlayer.getId(), "", cardId, playType));

        // Check if any player (excluding card player) has Ward
        if (game.doesAnyPlayerHaveWard(behaviorPlayer.getId())) {
            game.getCurrentRound().setStage(Stage.Wait_Accept_Ward_Effect);
            setIsOneRound(false);
            game.updateTopBehavior(this);

            Behavior wardBehavior = new WardBehavior(
                    game,
                    null,
                    game.whichPlayersHaveWard(behaviorPlayer.getId()).stream().map(Player::getId).collect(Collectors.toList()),
                    null,
                    cardId,
                    PlayType.INACTIVE.getPlayType(),
                    card,
                    true
            );
            wardBehavior.putParam(WardBehavior.WARD_TRIGGER_PLAYER_ID, behaviorPlayer.getId());
            game.updateTopBehavior(wardBehavior);

            events.addAll(wardBehavior.playerAction());
            events.add(game.getGameStatusEvent("發動桃園結義"));
            return events;
        }

        // No Ward holders → execute effect immediately
        events.add(game.getGameStatusEvent("出牌"));
        events.add(executePeachGardenEffect());
        return events;
    }

    @Override
    public List<DomainEvent> doBehaviorAction() {
        List<DomainEvent> events = new ArrayList<>();
        isOneRound = true;
        events.add(executePeachGardenEffect());
        return events;
    }

    private PeachGardenEvent executePeachGardenEffect() {
        List<PeachEvent> peachEvents = game.getPlayers().stream().map(player -> {
            int originHp = player.getHP();
            card.effect(player);
            return new PeachEvent(player.getId(), originHp, player.getHP());
        }).collect(Collectors.toList());

        return new PeachGardenEvent(behaviorPlayer, peachEvents);
    }
}

package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.UserCommand;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DismantleEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.events.SnatchEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TRIGGER_PLAYER_ID;

public class SnatchBehavior extends Behavior {
    public SnatchBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card,true, false, true);
    }


    @Override
    public List<DomainEvent> playerAction() {
        List<DomainEvent> events = new ArrayList<>();
        String currentReactionPlayerId = currentReactionPlayer.getId();
        playerPlayCardNotUpdateActivePlayer(behaviorPlayer, cardId);

        events.add(new PlayCardEvent(
                "出牌",
                behaviorPlayer.getId(),
                currentReactionPlayerId,
                cardId,
                playType));
        events.add(game.getGameStatusEvent("發動順手牽羊"));
        return events;
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        List<DomainEvent> events = new ArrayList<>();

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
            wardBehavior.putParam(WARD_TRIGGER_PLAYER_ID, behaviorPlayer.getId());

            game.updateTopBehavior(wardBehavior);
            events.addAll(wardBehavior.playerAction());
        } else {
            events.addAll(doBehaviorAction());
        }
        events.add(game.getGameStatusEvent("發動順手牽羊"));

        return events;
    }

    @Override
    public List<DomainEvent> doBehaviorAction() {
        List<DomainEvent> events = new ArrayList<>();
        String handCardId = (String) getParam(UserCommand.CHOOSE_HAND_CARD.name());
        HandCard handCard = PlayCard.findById(handCardId);
        String playerId = (String) getParam(UserCommand.SNATCH_BEHAVIOR_PLAYER_ID.name());
        String targetPlayerId = (String) getParam(UserCommand.SNATCH_BEHAVIOR_TARGET_PLAYER_ID.name());
        String cardId = (String) getParam(UserCommand.SNATCH_BEHAVIOR_USE_DISMANTLE_EFFECT_CARD_ID.name());
        reactionPlayers.remove(0);

        Player behaviorPlayer = game.getPlayer(playerId);
        String playerGeneralName = behaviorPlayer.getGeneralName();
        String targetPlayerGeneralName = game.getPlayer(targetPlayerId).getGeneralName();
        Player targetPlayer = game.getPlayer(targetPlayerId);

        if (handCard != null) {
            List<HandCard> cards = targetPlayer.getHand().getCards();
            cards.remove(handCard);
            behaviorPlayer.getHand().addCardToHand(handCard);
            events.add(new SnatchEvent(playerId, targetPlayerId, handCard.getId(), String.format("%s 偷走了 %s 的手牌 %s", playerGeneralName, targetPlayerGeneralName, handCard.getName())));
            events.add(game.getGameStatusEvent("順手牽羊效果"));
        } else {
            HandCard card = PlayCard.findById(cardId);
            if (!targetPlayer.getEquipment().hasThisEquipment(cardId)) {
                throw new IllegalArgumentException("Player doesn't have this cardId in equipment");
            }

            targetPlayer.getEquipment().removeEquipment(cardId);
            behaviorPlayer.getHand().addCardToHand(handCard);
            events.add(new SnatchEvent(playerId, targetPlayerId, cardId, String.format("%s 偷走了 %s 的裝備 %s", playerGeneralName, targetPlayerGeneralName, PlayCard.getCardName(cardId))));
            events.add(game.getGameStatusEvent("順手牽羊效果"));
        }
        isOneRound = true;
        return events;
    }


}

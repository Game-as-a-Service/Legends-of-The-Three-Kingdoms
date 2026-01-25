package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.UserCommand;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.DismantleEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.round.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.behavior.behavior.WardBehavior.WARD_TRIGGER_PLAYER_ID;

public class DismantleBehavior extends Behavior {
    public DismantleBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, true, false, true);
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
        events.add(game.getGameStatusEvent("發動過河拆橋"));
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
                    game.whichPlayersHaveWard(behaviorPlayer.getId()).stream().map(Player::getId).collect(Collectors.toList()),
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
        events.add(game.getGameStatusEvent("發動過河拆橋"));

        return events;
    }

    @Override
    public List<DomainEvent> doBehaviorAction() {
        List<DomainEvent> events = new ArrayList<>();
        String handCardId = (String) getParam(UserCommand.CHOOSE_HAND_CARD.name());
        HandCard handCard = PlayCard.findById(handCardId);
        String playerId = (String) getParam(UserCommand.DISMANTLE_BEHAVIOR_PLAYER_ID.name());
        String targetPlayerId = (String) getParam(UserCommand.DISMANTLE_BEHAVIOR_TARGET_PLAYER_ID.name());
        String cardId = (String) getParam(UserCommand.DISMANTLE_BEHAVIOR_USE_DISMANTLE_EFFECT_CARD_ID.name());
        reactionPlayers.remove(0);

        String playerGeneralName = game.getPlayer(playerId).getGeneralName();
        String targetPlayerGeneralName = game.getPlayer(targetPlayerId).getGeneralName();
        Player targetPlayer = game.getPlayer(targetPlayerId);

        if (handCard != null) {
            List<HandCard> cards = targetPlayer.getHand().getCards();
            cards.remove(handCard);
            game.getGraveyard().add(handCard);
            events.add(new DismantleEvent(playerId, targetPlayerId, handCard.getId(), String.format("%s 拆掉了 %s 的手牌 %s", playerGeneralName, targetPlayerGeneralName, handCard.getName())));
            events.add(game.getGameStatusEvent("過河拆橋效果"));
        } else {
            HandCard card = PlayCard.findById(cardId);
            if (!targetPlayer.getEquipment().hasThisEquipment(cardId) && !targetPlayer.hasThisDelayScrollCard(cardId)) {
                throw new IllegalArgumentException("Player doesn't have this cardId in equipment or delayScrollCard");
            }

            if (targetPlayer.getEquipment().hasThisEquipment(cardId)) {
                targetPlayer.getEquipment().removeEquipment(cardId);
                events.add(new DismantleEvent(playerId, targetPlayerId, cardId, String.format("%s 拆掉了 %s 的裝備 %s", playerGeneralName, targetPlayerGeneralName, PlayCard.getCardName(cardId))));
            } else {
                targetPlayer.removeDelayScrollCard(cardId);
                events.add(new DismantleEvent(playerId, targetPlayerId, cardId, String.format("%s 拆掉了 %s 判定區的 %s", playerGeneralName, targetPlayerGeneralName, PlayCard.getCardName(cardId))));
            }
            game.getGraveyard().add(card);
            events.add(game.getGameStatusEvent("過河拆橋效果"));
        }
        isOneRound = true;
        return events;
    }


}

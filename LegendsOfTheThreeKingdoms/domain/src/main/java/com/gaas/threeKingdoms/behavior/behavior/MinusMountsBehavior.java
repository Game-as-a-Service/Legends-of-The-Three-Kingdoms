package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.MinusMountsCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;

public class MinusMountsBehavior extends Behavior {

    public MinusMountsBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, false, true);
    }

    @Override
    public List<DomainEvent> playerAction() {
        playerPlayCard(behaviorPlayer, behaviorPlayer, cardId);
        MinusMountsCard mountsCard = behaviorPlayer.getEquipment().getMinusOne();
        String originEquipmentId = "";
        if (mountsCard != null) {
            originEquipmentId = mountsCard.getId();
        }
        card.effect(behaviorPlayer);
        return List.of(game.getGameStatusEvent("出牌"),
                new PlayCardEvent(
                "出牌",
                behaviorPlayer.getId(),
                behaviorPlayer.getId(),
                cardId,
                playType), new PlayEquipmentCardEvent(behaviorPlayer.getId(), cardId, originEquipmentId));
    }

    @Override
    protected List<DomainEvent> doResponseToPlayerAction(String playerId, String targetPlayerId, String cardId, String playType) {
        return null;
    }
}

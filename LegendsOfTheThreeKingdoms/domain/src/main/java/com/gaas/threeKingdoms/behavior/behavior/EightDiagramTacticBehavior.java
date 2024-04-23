package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.Round;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.ArmorCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;

public class EightDiagramTacticBehavior extends Behavior {
    public EightDiagramTacticBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, false, true);
    }

    @Override
    public List<DomainEvent> askTargetPlayerPlayCard() {
        playerPlayCard(behaviorPlayer, behaviorPlayer, cardId);
        ArmorCard armorCard = behaviorPlayer.getEquipment().getArmor();
        String originEquipmentId = "";
        if (armorCard != null) {
            originEquipmentId = armorCard.getId();
        }
        card.effect(behaviorPlayer);

        Round currentRound = game.getCurrentRound();
        RoundEvent roundEvent = new RoundEvent(currentRound);
        List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
        return List.of(new PlayCardEvent(
                "出牌",
                behaviorPlayer.getId(),
                behaviorPlayer.getId(),
                cardId,
                playType,
                game.getGameId(),
                playerEvents,
                roundEvent,
                game.getGamePhase().getPhaseName()
                ),
                new PlayEquipmentCardEvent(behaviorPlayer.getId(), cardId, originEquipmentId));
    }

    @Override
    protected List<DomainEvent> doAcceptedTargetPlayerPlayCard(String playerId, String targetPlayerId, String cardId, String playType) {
        return null;
    }
}

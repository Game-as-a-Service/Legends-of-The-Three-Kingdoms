package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.MinusMountsCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.ArrayList;
import java.util.List;

public class MinusMountsBehavior extends Behavior {

    public MinusMountsBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, false, true, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        playerPlayEquipmentCard(behaviorPlayer, behaviorPlayer, cardId);
        MinusMountsCard originMounts = behaviorPlayer.getEquipment().getMinusOne();
        String originEquipmentId = "";
        if (originMounts != null) {
            originEquipmentId = originMounts.getId();
        }
        card.effect(behaviorPlayer);

        // 梟姬摸牌要先結算：presenter 取第一個 GameStatusEvent 當快照，
        // 摸牌後才建快照，seats 的 hand 才含補摸的牌（使用者回報）
        List<DomainEvent> loseEquipmentEvents = discardReplacedEquipment(originMounts);

        List<DomainEvent> events = new ArrayList<>(List.of(game.getGameStatusEvent("出牌"),
                new PlayCardEvent(
                "出牌",
                behaviorPlayer.getId(),
                behaviorPlayer.getId(),
                cardId,
                playType), new PlayEquipmentCardEvent(behaviorPlayer.getId(), cardId, originEquipmentId)));
        events.addAll(loseEquipmentEvents);
        return events;
    }

}

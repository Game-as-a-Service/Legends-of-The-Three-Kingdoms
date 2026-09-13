package com.gaas.threeKingdoms.behavior.behavior;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.WeaponCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.ArrayList;
import java.util.List;

public class EquipWeaponBehavior extends Behavior {
    public EquipWeaponBehavior(Game game, Player behaviorPlayer, List<String> reactionPlayers, Player currentReactionPlayer, String cardId, String playType, HandCard card) {
        super(game, behaviorPlayer, reactionPlayers, currentReactionPlayer, cardId, playType, card, false, true, false);
    }

    @Override
    public List<DomainEvent> playerAction() {
        playerPlayEquipmentCard(behaviorPlayer, behaviorPlayer, cardId);
        WeaponCard originWeapon = behaviorPlayer.getEquipment().getWeapon();
        String originEquipmentId = "";
        if (originWeapon != null) {
            originEquipmentId = originWeapon.getId();
        }
        card.effect(behaviorPlayer);

        List<DomainEvent> events = new ArrayList<>(List.of(game.getGameStatusEvent("出牌"),
                new PlayCardEvent(
                        "出牌",
                        behaviorPlayer.getId(),
                        behaviorPlayer.getId(),
                        cardId,
                        playType),
                new PlayEquipmentCardEvent(behaviorPlayer.getId(), cardId, originEquipmentId)));
        events.addAll(discardReplacedEquipment(originWeapon));
        return events;
    }

}

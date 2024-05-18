package com.gaas.threeKingdoms.handcard.equipmentcard.mountscard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.equipmentcard.EquipmentCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;

public class PlusMountsCard extends EquipmentCard {

    public PlusMountsCard(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public List<DomainEvent> equipmentEffect(Game game) {
        return null;
    }

    @Override
    public void effect(Player player) {
        player.getEquipment().setPlusOne(this);
    }
}

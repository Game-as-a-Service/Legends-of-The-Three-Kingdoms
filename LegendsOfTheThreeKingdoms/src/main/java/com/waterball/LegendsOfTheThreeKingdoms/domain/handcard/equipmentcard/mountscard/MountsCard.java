package com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.equipmentcard.mountscard;

import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.equipmentcard.EquipmentCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;

public abstract class MountsCard extends EquipmentCard {
    public MountsCard(PlayCard playCard) {
        super(playCard);
    }
}

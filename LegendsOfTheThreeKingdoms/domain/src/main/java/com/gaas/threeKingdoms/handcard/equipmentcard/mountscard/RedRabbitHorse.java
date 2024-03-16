package com.gaas.threeKingdoms.handcard.equipmentcard.mountscard;

import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;

/**
 * 赤兔馬
 */
public class RedRabbitHorse extends MountsCard {

    public RedRabbitHorse(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public void effect(Player player) {
        player.getEquipment().setMinusOne(this);
    }
}

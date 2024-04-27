package com.gaas.threeKingdoms.handcard.equipmentcard.armorcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.PlayCard;

import java.util.List;

/**
 * 諸葛連弩
 * */
public class RepeatingCrossbowArmorCard extends ArmorCard {
    public RepeatingCrossbowArmorCard(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public List<DomainEvent> equipmentEffect(Game game) {
        return null;
    }

}

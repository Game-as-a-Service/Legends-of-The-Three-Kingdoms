package com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.PlayCard;

import java.util.List;

/**
 * 諸葛連弩
 * */
public class RepeatingCrossbowCard extends WeaponCard {
    public RepeatingCrossbowCard(PlayCard playCard) {
        super(playCard);
    }

    @Override
    public List<DomainEvent> equipmentEffect(Game game) {
        return null;
    }

}

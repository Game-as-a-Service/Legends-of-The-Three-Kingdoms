package com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.PlayCard;

import java.util.List;

/**
 * 麒麟弓
 * */
public class QilinBowCard extends WeaponCard {

    public QilinBowCard(PlayCard playCard) {
        super(playCard, 5);
    }

    @Override
    public List<DomainEvent> equipmentEffect(Game game) {
        return null;
    }

}

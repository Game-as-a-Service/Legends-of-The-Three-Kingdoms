package com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.PlayCard;

import java.util.List;

/**
 * 青釭劍 - 攻擊範圍2，裝備後殺無視目標防具
 */
public class BlackPommelCard extends WeaponCard {

    public BlackPommelCard(PlayCard playCard) {
        super(playCard, 2);
    }

    @Override
    public List<DomainEvent> equipmentEffect(Game game) {
        return null;
    }
}

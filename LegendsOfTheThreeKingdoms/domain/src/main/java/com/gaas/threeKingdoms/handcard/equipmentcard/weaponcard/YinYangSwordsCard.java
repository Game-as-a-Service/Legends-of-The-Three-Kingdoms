package com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.PlayCard;

import java.util.List;

/**
 * 雌雄雙股劍
 */
public class YinYangSwordsCard extends WeaponCard {

    public YinYangSwordsCard(PlayCard playCard) {
        super(playCard, 2);
    }

    @Override
    public List<DomainEvent> equipmentEffect(Game game) {
        return null;
    }
}

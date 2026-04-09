package com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.PlayCard;

import java.util.List;

/**
 * 貫石斧 Stone Piercing Axe
 * 攻擊範圍 3，殺被閃抵銷時可棄兩張牌強制命中
 */
public class StonePiercingAxeCard extends WeaponCard {

    public StonePiercingAxeCard(PlayCard playCard) {
        super(playCard, 3);
    }

    @Override
    public List<DomainEvent> equipmentEffect(Game game) {
        return null;
    }
}

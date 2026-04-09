package com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.PlayCard;

import java.util.List;

/**
 * 青龍偃月刀 Green Dragon Crescent Blade
 * 攻擊範圍 3，殺被閃抵銷時可再出一張殺（可重複）
 */
public class GreenDragonCrescentBladeCard extends WeaponCard {

    public GreenDragonCrescentBladeCard(PlayCard playCard) {
        super(playCard, 3);
    }

    @Override
    public List<DomainEvent> equipmentEffect(Game game) {
        return null;
    }
}

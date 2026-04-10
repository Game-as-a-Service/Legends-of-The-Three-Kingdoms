package com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.PlayCard;

import java.util.List;

/**
 * 方天畫戟 Heavenly Double Halberd
 * 攻擊範圍 4，當玩家使用殺且該殺是最後一張手牌時，可為此殺額外指定至多 2 名目標。
 */
public class HeavenlyDoubleHalberdCard extends WeaponCard {

    public HeavenlyDoubleHalberdCard(PlayCard playCard) {
        super(playCard, 4);
    }

    @Override
    public List<DomainEvent> equipmentEffect(Game game) {
        return null;
    }
}

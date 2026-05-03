package com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;

/**
 * 丈八蛇矛 Eighteen-span Viper Spear
 * 攻擊範圍 3，裝備後可將兩張手牌當作殺使用
 */
public class EighteenSpanViperSpearCard extends WeaponCard {

    public EighteenSpanViperSpearCard(PlayCard playCard) {
        super(playCard, 3);
    }

    @Override
    public List<DomainEvent> equipmentEffect(Game game) {
        return null;
    }

    /** 玩家是否能將兩張手牌當殺使用/打出（裝備丈八蛇矛 + 手牌 ≥ 2）。 */
    public static boolean canUseAsKill(Player player) {
        return player.getEquipmentWeaponCard() instanceof EighteenSpanViperSpearCard
                && player.getHand().getCards().size() >= 2;
    }
}

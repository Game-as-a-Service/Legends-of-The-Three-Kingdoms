package com.gaas.threeKingdoms.skill.context;

import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.player.Player;

public record DamageContext(
        Player damagedPlayer,
        Player sourcePlayer,
        HandCard sourceCard,
        int points
) {
}

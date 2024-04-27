package com.gaas.threeKingdoms.handcard.equipmentcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.EffectEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import lombok.Getter;

import java.util.List;

@Getter
public abstract class EquipmentCard extends HandCard {
    protected boolean hasSpecialEffect = false;

    public EquipmentCard(PlayCard playCard) {
        super(playCard);
    }

    public abstract List<DomainEvent> equipmentEffect(Game game);
}

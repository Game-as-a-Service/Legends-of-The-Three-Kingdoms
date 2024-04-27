package com.gaas.threeKingdoms.handcard.equipmentcard.armorcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.EffectEvent;
import com.gaas.threeKingdoms.events.EightDiagramTacticEffectEvent;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.Suit;

import java.util.List;

public class EightDiagramTactic extends ArmorCard {

    public EightDiagramTactic(PlayCard playCard) {
        super(playCard);
        hasSpecialEffect = true;
    }

    @Override
    public List<DomainEvent> equipmentEffect(Game game) {
        HandCard card = game.drawCardForEightDiagramTactic();
        boolean isEffectSuccess = isEffectSuccess(card);
        return List.of(new EightDiagramTacticEffectEvent("發動八卦陣效果", card.getId(), isEffectSuccess));
    }

    private boolean isEffectSuccess(HandCard card) {
        return Suit.DIAMOND == card.getSuit() || Suit.HEART == card.getSuit();
    }
}

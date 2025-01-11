package com.gaas.threeKingdoms.handcard.equipmentcard.armorcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.Suit;

import java.util.ArrayList;
import java.util.List;

public class EightDiagramTactic extends ArmorCard {

    public EightDiagramTactic(PlayCard playCard) {
        super(playCard);
        hasSpecialEffect = true;
    }

    @Override
    public List<DomainEvent> equipmentEffect(Game game) {
        List<HandCard> cards = game.drawCardForCardEffect(1);
        HandCard card = cards.get(0);
        boolean isEffectSuccess = isEffectSuccess(card);
        List<DomainEvent> events = new ArrayList<>();
        Round currentRound = game.getCurrentRound();
        if (isEffectSuccess) {
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
        }
        GameStatusEvent gameStatusEvent = game.getGameStatusEvent("發動八卦陣效果");
        events.add(new EightDiagramTacticEffectEvent("發動八卦陣效果", isEffectSuccess, card.getId()));
        events.add(gameStatusEvent);
        return events;
    }

    private boolean isEffectSuccess(HandCard card) {
        return Suit.DIAMOND == card.getSuit() || Suit.HEART == card.getSuit();
    }
}

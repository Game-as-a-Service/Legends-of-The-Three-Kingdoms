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
        return resolveEquipmentEffect(game, cards.get(0));
    }

    /** 以指定判定牌結算八卦陣（鬼才替換後 / 無鬼才直接）。 */
    public List<DomainEvent> resolveEquipmentEffect(Game game, HandCard card) {
        boolean isEffectSuccess = isEffectSuccess(card);
        List<DomainEvent> events = new ArrayList<>();
        Round currentRound = game.getCurrentRound();
        if (isEffectSuccess) {
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
        }
        events.add(new EightDiagramTacticEffectEvent("發動八卦陣效果", isEffectSuccess, card.getId()));
        return events;
    }

    private boolean isEffectSuccess(HandCard card) {
        return Suit.DIAMOND == card.getSuit() || Suit.HEART == card.getSuit();
    }
}

package com.gaas.threeKingdoms.handcard.equipmentcard.armorcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.Round;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.EightDiagramTacticEffectEvent;
import com.gaas.threeKingdoms.events.PlayerEvent;
import com.gaas.threeKingdoms.events.RoundEvent;
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
        HandCard card = game.drawCardForEightDiagramTactic();
        boolean isEffectSuccess = isEffectSuccess(card);
        List<DomainEvent> events = new ArrayList<>();
        List<PlayerEvent> playerEvents = game.getPlayers().stream().map(PlayerEvent::new).toList();
        Round currentRound = game.getCurrentRound();
        if (isEffectSuccess) {
            currentRound.setActivePlayer(currentRound.getCurrentRoundPlayer());
        }
        RoundEvent roundEvent = new RoundEvent(currentRound);
        events.add(new EightDiagramTacticEffectEvent("發動八卦陣效果", isEffectSuccess, card.getId(),game.getGameId(), playerEvents, roundEvent, game.getGamePhase().getPhaseName()));
        return events;
    }

    private boolean isEffectSuccess(HandCard card) {
        return Suit.DIAMOND == card.getSuit() || Suit.HEART == card.getSuit();
    }
}
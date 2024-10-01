package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckData {

    private List<String> cardDeck = new ArrayList<>();  // Store card IDs as strings

    public Deck toDomain() {
        Deck deck = new Deck();
        for (int i = cardDeck.size() - 1; i >= 0; i--) {
            deck.getCardDeck().push(PlayCard.findById(cardDeck.get(i)));
        }
        return deck;
    }


    public static DeckData fromDomain(Deck deck) {
        DeckData deckData = new DeckData();
        List<String> cardIds = new ArrayList<>();
        for (HandCard handCard : deck.getCardDeck()) {
            cardIds.add(handCard.getId());
        }
        deckData.setCardDeck(cardIds);
        return deckData;
    }
}
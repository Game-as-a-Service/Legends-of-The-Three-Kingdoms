package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Stack;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckData {

    private Stack<String> cardDeck = new Stack<>();  // Store card IDs as strings

    public Deck toDomain() {
        Deck deck = new Deck();
        for (String cardId : this.cardDeck) {
            deck.getCardDeck().push(PlayCard.findById(cardId));
        }
        return deck;
    }

    public static DeckData fromDomain(Deck deck) {
        DeckData deckData = new DeckData();
        Stack<String> cardIds = new Stack<>();
        for (HandCard handCard : deck.getCardDeck()) {
            cardIds.push(handCard.getId());  // Assuming HandCard has getId method
        }
        deckData.setCardDeck(cardIds);
        return deckData;
    }


}
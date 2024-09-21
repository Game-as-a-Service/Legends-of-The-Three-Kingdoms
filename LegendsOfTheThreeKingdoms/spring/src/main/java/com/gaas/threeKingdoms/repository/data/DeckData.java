package com.gaas.threeKingdoms.repository.data;

import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Stack;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeckData {

    private Stack<String> cardDeck = new Stack<>();  // Store card IDs as strings

    // Convert this PO to the domain object (Deck)
    public Deck toDomain() {
        Deck deck = new Deck();
        // Here, cardDeck can be converted from card IDs back to HandCard instances using appropriate lookup
        for (String cardId : this.cardDeck) {
            // Assuming a method like HandCard.findById(cardId) that fetches the actual HandCard
            deck.getCardDeck().push(PlayCard.findById(cardId));
        }
        return deck;
    }

    // Method to convert domain Deck to DeckData
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
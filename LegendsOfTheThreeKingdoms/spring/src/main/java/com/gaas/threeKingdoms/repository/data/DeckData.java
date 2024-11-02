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
        for (int i = 0; i < cardDeck.size(); i++) {
            deck.getCardDeck().push(PlayCard.findById(cardDeck.get(i)));
        }
        return deck;
    }


    public static DeckData fromDomain(Deck deck) {
        DeckData deckData = new DeckData();
        List<String> cardIds = new ArrayList<>();
        List<HandCard> handCards = deck.getCardDeck();
        for (int i =0; i < handCards.size(); i++) {
            HandCard handCard = handCards.get(i);
            if (handCard == null) {
                System.out.println("Null handCard: " + i);
            }

            try {
                cardIds.add(handCard.getId());
            } catch (Exception e) {
                System.out.println("Error processing handCard at index: " + i + ". HandCard: " + handCard);
                e.printStackTrace(); // Log stack trace to see the actual error
            }
        }
        deckData.setCardDeck(cardIds);
        return deckData;
    }
}
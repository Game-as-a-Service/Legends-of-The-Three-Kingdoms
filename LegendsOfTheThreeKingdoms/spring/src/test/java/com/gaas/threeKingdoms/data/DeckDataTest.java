package com.gaas.threeKingdoms.data;

import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.repository.data.DeckData;
import org.junit.jupiter.api.Test;

import java.util.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeckDataTest {


    @Test
    void testToData() {
        //Given
        Deck deck = new Deck();
        deck.init();

        //When
        DeckData deckData = DeckData.fromDomain(deck);

        //Then
        assertEquals(deck.getCardDeck().size(), deckData.getCardDeck().size());

        for (int i = 0; i < deck.getCardDeck().size(); i++) {
            assertEquals(deck.getCardDeck().get(i).getId(), deckData.getCardDeck().get(i));
        }
    }

    @Test
    void testToDomain() {

        //Given
        DeckData deckData = new DeckData();
        Stack<String> cardDeck = new Stack<>();
        cardDeck.push("BS8008");
        cardDeck.push("BH4030");
        cardDeck.push("BHK039");
        deckData.setCardDeck(cardDeck);

        //When
        Deck deck = deckData.toDomain();

        //Then
        assertEquals(deckData.getCardDeck().size(), deck.getCardDeck().size());

        assertEquals("BHK039", deck.getCardDeck().get(2).getId());
        assertEquals("BH4030", deck.getCardDeck().get(1).getId());
        assertEquals("BS8008", deck.getCardDeck().get(0).getId());

    }

}

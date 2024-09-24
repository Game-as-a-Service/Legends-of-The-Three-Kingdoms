package com.gaas.threeKingdoms.data;

import com.gaas.threeKingdoms.handcard.Graveyard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.repository.data.GraveyardData;
import org.junit.jupiter.api.Test;

import java.util.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraveyardDataTest {

    @Test
    void testToData() {
        // Given
        Graveyard graveyard = new Graveyard();
        graveyard.getGraveYardCards().add(PlayCard.findById("BS8008"));
        graveyard.getGraveYardCards().add(PlayCard.findById("BH4030"));
        graveyard.getGraveYardCards().add(PlayCard.findById("BHK039"));

        // When
        GraveyardData graveyardData = GraveyardData.fromDomain(graveyard);

        // Then
        assertEquals(graveyard.getGraveYardCards().size(), graveyardData.getGraveYardDeck().size());

        for (int i = 0; i < graveyard.getGraveYardCards().size(); i++) {
            assertEquals(graveyard.getGraveYardCards().get(i).getId(), graveyardData.getGraveYardDeck().get(i));
        }
    }

    @Test
    void testToDomain() {
        // Given
        GraveyardData graveyardData = new GraveyardData();
        Stack<String> graveYardDeck = new Stack<>();
        graveYardDeck.push("BS8008");
        graveYardDeck.push("BH4030");
        graveYardDeck.push("BHK039");
        graveyardData.setGraveYardDeck(graveYardDeck);

        // When
        Graveyard graveyard = graveyardData.toDomain();

        // Then
        assertEquals(graveyardData.getGraveYardDeck().size(), graveyard.getGraveYardDeck().size());

        for (int i = 0; i < graveyardData.getGraveYardDeck().size(); i++) {
            assertEquals(graveyardData.getGraveYardDeck().get(i), graveyard.getGraveYardDeck().get(i).getId());
        }
    }
}


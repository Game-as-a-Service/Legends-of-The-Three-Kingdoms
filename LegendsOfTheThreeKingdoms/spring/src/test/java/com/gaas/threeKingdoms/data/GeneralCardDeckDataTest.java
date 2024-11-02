package com.gaas.threeKingdoms.data;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.generalcard.GeneralCardDeck;
import com.gaas.threeKingdoms.repository.data.GeneralCardDeckData;
import org.junit.jupiter.api.Test;

import java.util.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeneralCardDeckDataTest {

    @Test
    public void testToDomainConversion() {
        // Arrange
        Stack<String> generalStackIds = new Stack<>();
        generalStackIds.push(General.劉備.generalId);  // 劉備
        generalStackIds.push(General.曹操.generalId);  // 曹操
        GeneralCardDeckData deckData = new GeneralCardDeckData(generalStackIds);

        // Act
        GeneralCardDeck deck = deckData.toDomain();

        // Assert
        assertEquals(2, deck.getGeneralStack().size());
        assertEquals("SHU001", deck.getGeneralStack().get(0).getGeneralId());
        assertEquals("WEI001", deck.getGeneralStack().get(1).getGeneralId());
    }

    @Test
    public void testFromDomainConversion() {
        // Arrange
        GeneralCardDeck deck = new GeneralCardDeck();
        deck.getGeneralStack().push(new GeneralCard(General.findById("SHU001")));  // 劉備
        deck.getGeneralStack().push(new GeneralCard(General.findById("WEI001")));  // 曹操

        // Act
        GeneralCardDeckData deckData = GeneralCardDeckData.fromDomain(deck);

        // Assert
        assertEquals(2, deckData.getGeneralStack().size());
        assertTrue(deckData.getGeneralStack().contains("SHU001"));
        assertTrue(deckData.getGeneralStack().contains("WEI001"));
    }
}

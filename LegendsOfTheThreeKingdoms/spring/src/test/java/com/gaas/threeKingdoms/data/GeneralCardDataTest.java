package com.gaas.threeKingdoms.data;

import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.repository.data.GeneralCardData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneralCardDataTest {

    @Test
    public void testToDomainConversion() {
        // Arrange
        GeneralCardData generalCardData = new GeneralCardData("SHU001", "劉備", 4);

        // Act
        GeneralCard generalCard = generalCardData.toDomain();

        // Assert
        assertEquals("SHU001", generalCard.getGeneralId());
        assertEquals("劉備", generalCard.getGeneralName());
        assertEquals(4, generalCard.getHealthPoint());
    }

    @Test
    public void testFromDomainConversion() {
        // Arrange
        GeneralCard generalCard = new GeneralCard();
        generalCard.setGeneralId("WEI001");
        generalCard.setGeneralName("曹操");
        generalCard.setHealthPoint(3);

        // Act
        GeneralCardData generalCardData = GeneralCardData.fromDomain(generalCard);

        // Assert
        assertEquals("WEI001", generalCardData.getGeneralId());
        assertEquals("曹操", generalCardData.getGeneralName());
        assertEquals(3, generalCardData.getHealthPoint());
    }
}

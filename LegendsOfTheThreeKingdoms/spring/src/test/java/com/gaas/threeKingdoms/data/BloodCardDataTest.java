package com.gaas.threeKingdoms.data;

import com.gaas.threeKingdoms.player.BloodCard;
import com.gaas.threeKingdoms.repository.data.BloodCardData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BloodCardDataTest {

    @Test
    public void testToDomainConversion() {
        // Arrange
        BloodCardData bloodCardData = new BloodCardData(10, 5);

        // Act
        BloodCard bloodCard = bloodCardData.toDomain();

        // Assert
        assertEquals(10, bloodCard.getMaxHp());
        assertEquals(5, bloodCard.getHp());
    }

    @Test
    public void testFromDomainConversion() {
        // Arrange
        BloodCard bloodCard = new BloodCard(5);
        bloodCard.setMaxHp(10);

        // Act
        BloodCardData bloodCardData = BloodCardData.fromDomain(bloodCard);

        // Assert
        assertEquals(10, bloodCardData.getMaxHp());
        assertEquals(5, bloodCardData.getHp());
    }
}

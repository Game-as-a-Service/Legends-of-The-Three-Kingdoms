package com.gaas.threeKingdoms.data;

import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.Hand;
import com.gaas.threeKingdoms.repository.data.HandData;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HandDataTest {

    @Test
    public void testHandDataToDomainConversion() {
        // Arrange
        List<String> cardIds = Arrays.asList("BS8008", "BH4030", "BHK039");
        HandData handData = new HandData(cardIds);

        // Act
        Hand hand = handData.toDomain();

        // Assert
        assertEquals(3, hand.getCards().size());
        assertEquals("BS8008", hand.getCards().get(0).getId());
        assertEquals("BH4030", hand.getCards().get(1).getId());
        assertEquals("BHK039", hand.getCards().get(2).getId());
    }

    @Test
    public void testHandDataFromDomainConversion() {
        // Arrange
        Hand hand = new Hand();
        hand.setCards(Arrays.asList(
                new Kill(BS8009),
                new Peach(BH7033),
                new Dodge(BHK039)
        ));

        // Act
        HandData handData = HandData.fromDomain(hand);

        // Assert
        assertEquals(3, handData.getCards().size());
        assertTrue(handData.getCards().contains("BS8009"));
        assertTrue(handData.getCards().contains("BH7033"));
        assertTrue(handData.getCards().contains("BHK039"));
    }
}
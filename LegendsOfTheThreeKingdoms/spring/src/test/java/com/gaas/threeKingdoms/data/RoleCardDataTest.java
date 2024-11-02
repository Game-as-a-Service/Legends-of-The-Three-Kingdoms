package com.gaas.threeKingdoms.data;

import com.gaas.threeKingdoms.repository.data.RoleCardData;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RoleCardDataTest {

    @Test
    public void testToDomainConversion() {
        // Arrange
        RoleCardData roleCardData = new RoleCardData("MONARCH");

        // Act
        RoleCard roleCard = roleCardData.toDomain();

        // Assert
        assertEquals(Role.MONARCH, roleCard.getRole());
    }

    @Test
    public void testFromDomainConversion() {
        // Arrange
        RoleCard roleCard = new RoleCard(Role.MINISTER);

        // Act
        RoleCardData roleCardData = RoleCardData.fromDomain(roleCard);

        // Assert
        assertEquals("MINISTER", roleCardData.getRoleName());
    }
}

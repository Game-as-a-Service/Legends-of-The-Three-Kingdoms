package com.gaas.threeKingdoms.data;

import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.repository.data.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.generalcard.General.曹操;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlayerDataTest {

    @Test
    public void testPlayerDataToDomainConversion() {
        // Arrange
        HandData handData = new HandData(Arrays.asList("BS8008", "BH4030"));
        RoleCardData roleCardData = new RoleCardData("MINISTER");
        GeneralCardData generalCardData = new GeneralCardData("SHU001", "劉備", 4);
        BloodCardData bloodCardData = new BloodCardData(4, 4);
        EquipmentData equipmentData = EquipmentData.builder()
                .plusOneCardId("ES5018")
                .minusOneCardId("EH5044")
                .armorCardId("ES2015")
                .weaponCardId("EH5031")
                .build();

        PlayerData playerData = PlayerData.builder()
                .hand(handData)
                .id("player1")
                .role(roleCardData)
                .general(generalCardData)
                .blood(bloodCardData)
                .healthStatus("ALIVE")
                .equipment(equipmentData)
                .delayScrolls(List.of("SC6071"))
                .build();

        // Act
        Player player = playerData.toDomain();

        // Assert
        assertEquals("player1", player.getId());
        assertEquals(2, player.getHand().getCards().size());
        assertEquals("BS8008", player.getHand().getCards().get(0).getId());
        assertEquals("MINISTER", player.getRoleCard().getRole().name());
        assertEquals("SHU001", player.getGeneralCard().getGeneralId());
        assertEquals(4, player.getGeneralCard().getHealthPoint());
        assertEquals(4, player.getBloodCard().getMaxHp());
        assertEquals(4, player.getBloodCard().getHp());
        assertEquals(HealthStatus.ALIVE, player.getHealthStatus());
        assertEquals("ES5018", player.getEquipment().getPlusOne().getId());
        assertEquals(1, player.getDelayScrollCards().size());
        assertEquals("SC6071", player.getDelayScrollCards().get(0).getId());
    }

    @Test
    public void testPlayerDataFromDomainConversion() {
        // Arrange
        Hand hand = new Hand(Arrays.asList(new Kill(BS8009), new Peach(BH7033)));
        RoleCard roleCard = new RoleCard(Role.TRAITOR);
        GeneralCard generalCard = new GeneralCard(曹操);
        BloodCard bloodCard = new BloodCard(3);
        Equipment equipment = new Equipment();
        equipment.setPlusOne(new ShadowHorse(ES5018));
        equipment.setMinusOne(new RedRabbitHorse(BH3029));
        equipment.setArmor(new EightDiagramTactic(ES2015));
        equipment.setWeapon(new QilinBowCard(EH5031));

        Player player = new Player(hand, "player2", roleCard, generalCard, bloodCard, HealthStatus.DYING, equipment);

        player.addDelayScrollCard(new Contentment(SC6071));

        // Act
        PlayerData playerData = PlayerData.fromDomain(player);

        // Assert
        assertEquals("player2", playerData.getId());
        assertEquals(2, playerData.getHand().getCards().size());
        assertTrue(playerData.getHand().getCards().contains("BS8009"));
        assertEquals("TRAITOR", playerData.getRole().getRoleName());
        assertEquals("WEI001", playerData.getGeneral().getGeneralId());
        assertEquals(曹操.healthPoint, playerData.getGeneral().getHealthPoint());
        assertEquals(3, playerData.getBlood().getMaxHp());
        assertEquals(3, playerData.getBlood().getHp());
        assertEquals("DYING", playerData.getHealthStatus());
        assertEquals("ES5018", playerData.getEquipment().getPlusOneCardId());
        assertEquals(1, playerData.getDelayScrolls().size());
        assertEquals("SC6071", playerData.getDelayScrolls().get(0));

    }
}
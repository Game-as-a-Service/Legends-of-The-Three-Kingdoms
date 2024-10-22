package com.gaas.threeKingdoms.data;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.SeatingChart;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.repository.data.*;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeatingChartDataTest {

    @Test
    public void testSeatingChartDataToDomainConversion() {
        Game game = new Game();
        Player player1 = createPlayer(
                "player1",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(PlayCard.BS8008), new Peach(PlayCard.BH3029)
        );

        Player player2 = createPlayer(
                "player2",
                3,
                General.張飛,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(PlayCard.BS8009), new Peach(PlayCard.BH4030)
        );

        List<Player> players = Arrays.asList(player1, player2);
        game.setPlayers(players);

        // Arrange
        PlayerData playerData1 = PlayerData.builder()
                .hand(new HandData(Arrays.asList("BS8008", "BH4030")))
                .id("player1")
                .role(new RoleCardData("MINISTER"))
                .general(new GeneralCardData("SHU001", "劉備", 4))
                .blood(new BloodCardData(4, 4))
                .healthStatus("ALIVE")
                .equipment(EquipmentData.builder()
                        .plusOneCardId("ES5018")
                        .minusOneCardId("EH5044")
                        .armorCardId("ES2015")
                        .weaponCardId("EH5031")
                        .build())
                .build();

        PlayerData playerData2 = PlayerData.builder()
                .hand(new HandData(Arrays.asList("BS8009", "BH7033")))
                .id("player2")
                .role(new RoleCardData("TRAITOR"))
                .general(new GeneralCardData("WEI001", "曹操", 3))
                .blood(new BloodCardData(3, 3))
                .healthStatus("DYING")
                .equipment(EquipmentData.builder()
                        .plusOneCardId("ES5018")
                        .minusOneCardId("EH5044")
                        .armorCardId("ES2015")
                        .weaponCardId("EH5031")
                        .build())
                .build();

        SeatingChartData seatingChartData = SeatingChartData.builder()
                .playerList(new ArrayList<>(Arrays.asList("player1", "player2")))
                .build();

        // Act
        SeatingChart seatingChart = seatingChartData.toDomain(game);

        // Assert
        assertEquals(2, seatingChart.getPlayers().size());
        assertEquals("player1", seatingChart.getPlayers().get(0).getId());
        assertEquals("player2", seatingChart.getPlayers().get(1).getId());
    }

    @Test
    public void testSeatingChartDataFromDomainConversion() {
        // Arrange
        Player playerA = createPlayer(
                "player-a",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Player playerB = createPlayer("player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)
        );

        Player playerC = createPlayer(
                "player-c",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Player playerD = createPlayer(
                "player-d",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        ArrayList<Player> players = new ArrayList<>(Arrays.asList(playerA, playerB, playerC, playerD));
        SeatingChart seatingChart = new SeatingChart(players);

        // Act
        SeatingChartData seatingChartData = SeatingChartData.fromDomain(seatingChart);

        // Assert
        assertEquals(4, seatingChartData.getPlayerList().size());
        assertEquals("player-a", seatingChartData.getPlayerList().get(0));
        assertEquals("player-b", seatingChartData.getPlayerList().get(1));
    }
}

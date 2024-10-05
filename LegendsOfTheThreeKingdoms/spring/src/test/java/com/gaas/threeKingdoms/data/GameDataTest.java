package com.gaas.threeKingdoms.data;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.BarbarianInvasionBehavior;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.repository.data.*;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.Test;

import java.util.*;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GameDataTest {

    @Test
    public void testToDomainConversion() {
        // Arrange
        HandData handData1 = new HandData(Arrays.asList("BS8008", "BH4030")); // Kill and Peach for player1
        RoleCardData roleCardData1 = new RoleCardData("MINISTER");
        GeneralCardData generalCardData1 = new GeneralCardData("SHU001", "劉備", 4);
        BloodCardData bloodCardData1 = new BloodCardData(4, 4);
        EquipmentData equipmentData1 = EquipmentData.builder()
                .plusOneCardId("ES5018")   // Plus mount
                .minusOneCardId("EH5044")  // Minus mount
                .armorCardId("ES2015")     // Armor
                .weaponCardId("EH5031")    // Weapon
                .build();

        PlayerData playerData1 = PlayerData.builder()
                .hand(handData1)
                .id("player1")
                .role(roleCardData1)
                .general(generalCardData1)
                .blood(bloodCardData1)
                .healthStatus("ALIVE")
                .equipment(equipmentData1)
                .build();

        HandData handData2 = new HandData(Arrays.asList("BS8009", "BH4031")); // Kill and Peach for player2
        RoleCardData roleCardData2 = new RoleCardData("TRAITOR");
        GeneralCardData generalCardData2 = new GeneralCardData("WEI001", "曹操", 4);
        BloodCardData bloodCardData2 = new BloodCardData(3, 3);
        EquipmentData equipmentData2 = EquipmentData.builder()
                .plusOneCardId("ES5019")   // Plus mount
                .minusOneCardId("EH5045")  // Minus mount
                .armorCardId("ES2016")     // Armor
                .weaponCardId("EH5032")    // Weapon
                .build();

        PlayerData playerData2 = PlayerData.builder()
                .hand(handData2)
                .id("player2")
                .role(roleCardData2)
                .general(generalCardData2)
                .blood(bloodCardData2)
                .healthStatus("DYING")
                .equipment(equipmentData2)
                .build();

        // Add the players to a list
        List<PlayerData> players = Arrays.asList(playerData1, playerData2);

        // Create a mock deck
        DeckData deckData = new DeckData();
        Stack<String> cardDeck = new Stack<>();
        cardDeck.push("BS8008");
        cardDeck.push("BH4030");
        cardDeck.push("BHK039");
        deckData.setCardDeck(cardDeck);

        // Create a mock graveyard
        GraveyardData graveyardData = new GraveyardData();
        Stack<String> graveYardDeck = new Stack<>();
        graveYardDeck.push("BS8008");
        graveYardDeck.push("BH4030");
        graveYardDeck.push("BHK039");
        graveyardData.setGraveYardDeck(graveYardDeck);

        // Create a mock seating chart
        SeatingChartData seatingChartData = SeatingChartData.builder()
                .playerDataList(new ArrayList<>(Arrays.asList(playerData1, playerData2)))
                .build();

        // Create a mock round
        RoundData roundData = RoundData.builder()
                .roundPhase("Judgement")
                .currentRoundPlayer(playerData1)
                .activePlayer(playerData2)
                .dyingPlayer(playerData1)
                .currentPlayCard("BS8008")
                .isShowKill(true)
                .stage("Normal")
                .build();

        // Create a mock behavior
        BehaviorData behaviorData = BehaviorData.builder()
                .behaviorName("BarbarianInvasionBehavior")
                .behaviorPlayer(playerData1)
                .currentReactionPlayer(playerData2)
                .reactionPlayers(new ArrayList<>(Arrays.asList("player2")))
                .cardId("BS8008")
                .playType("active")
                .isTargetPlayerNeedToResponse(true)
                .isOneRound(false)
                .build();

        // Initialize GameData
        GameData gameData = GameData.builder()
                .gameId("game-001")
                .gamePhase("Normal")
                .players(players)
                .deck(deckData)
                .graveyard(graveyardData)
                .seatingChart(seatingChartData)
                .round(roundData)
                .topBehaviors(new ArrayList<>(Collections.singletonList(behaviorData)))
                .build();

        // Act
        Game game = gameData.toDomain();

        // Assert
        assertNotNull(game);
        assertEquals("game-001", game.getGameId());
        assertEquals(2, game.getPlayers().size());
        assertEquals("Normal", game.getGamePhase().getPhaseName());
        assertEquals("player1", game.getCurrentRound().getCurrentRoundPlayer().getId());
        assertEquals(1, game.getTopBehavior().size());
        assertEquals("BS8008", game.getTopBehavior().peek().getCardId());
        assertEquals("player1", game.getPlayers().get(0).getId());
        assertEquals("player2", game.getPlayers().get(1).getId());
    }


    @Test
    public void testGameToGameDataConversion() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(PlayCard.BS8008), new Peach(PlayCard.BH3029)
        );

        Player playerB = createPlayer(
                "player-b",
                3,
                General.張飛,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(PlayCard.BS8009), new Peach(PlayCard.BH4030)
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

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame("my-game", players, playerB);


        List<String> reactionPlayers = Arrays.asList("player-b");

        Kill currentPlayCard = new Kill(PlayCard.BS8008);
        Behavior behavior = new BarbarianInvasionBehavior(
                game,
                playerA,
                reactionPlayers,
                playerB,
                "BS8008",
                PlayType.ACTIVE.getPlayType(),
                currentPlayCard
        );
        game.updateTopBehavior(behavior);

        // Act
        GameData gameData = GameData.fromDomain(game);

        // Assert
        assertEquals("my-game", gameData.getGameId());
        assertEquals(4, gameData.getPlayers().size());
        assertEquals("player-a", gameData.getPlayers().get(0).getId());
        assertEquals("Normal", gameData.getGamePhase());
        assertEquals(1, gameData.getTopBehaviors().size());
        assertEquals("player-b", game.getCurrentRound().getCurrentRoundPlayer().getId());

    }

}

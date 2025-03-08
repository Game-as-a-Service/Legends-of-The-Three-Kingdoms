package com.gaas.threeKingdoms.e2e.scrollcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.BountifulHarvest;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BountifulHarvestTest extends AbstractBaseIntegrationTest {

    @Test
    public void givenPlayerABC_WhenPlayerAPlayBountifulHarvest_ThenPlayerABCReceiveBountifulHarvestEvent() throws Exception {
        //   Given
        //   玩家ABCD
        //   A的回合
        givenPlayerAHaveBountifulHarvest();
        //   When
        //   A玩家出五穀豐登
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SH3042", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        //   Then
        //   ABCD 玩家收到五穀豐登的 event
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/BountifulHarvestBehavior/player_a_play_bountifulHarvest_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

    }

    @Test
    public void givenPlayerBTurn_PlayerBPlaysBountifulHarvestAndDrawsCards_WhenPlayerBChoosesES2002_ThenAllPlayersReceiveSelectionEventAndNextPlayerChooses() throws Exception {
        //    Given
        //    玩家ABCD
        //    B的回合
        //    B玩家出五穀豐登，抽出 (BH3029、BH2028、BHK039、SS7007)
        givenPlayerBHaveBountifulHarvest();
        mockMvcUtil.playCard(gameId, "player-b", "", "SH3042", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();
        //   When
        //   B 選擇 SS7007 南蠻入侵
        mockMvcUtil.chooseCardFromBountifulHarvest(gameId, "player-b", "SS7007");

        //   Then
        //   ABCD 玩家收到 B 選擇 SS7007 的 event
        //   ABCD 玩家收到五穀豐登的 event 並且是輪到 C 選擇
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/BountifulHarvestBehavior/player_b_chooseCardFromBountifulHarvest_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        //   C 選擇 BH3029
        mockMvcUtil.chooseCardFromBountifulHarvest(gameId, "player-c", "BH3029");
        websocketUtil.popAllPlayerMessage();

        //   D 選擇 BH2028
        mockMvcUtil.chooseCardFromBountifulHarvest(gameId, "player-d", "BH2028");
        websocketUtil.popAllPlayerMessage();

        //   A 選擇 BHK039
        mockMvcUtil.chooseCardFromBountifulHarvest(gameId, "player-a", "BHK039");

        //   Then
        //   ABCD 玩家收到 A 選擇 BHK039 的 event
        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/BountifulHarvestBehavior/player_a_chooseCardFromBountifulHarvest_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    private void givenPlayerBHaveBountifulHarvest() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Peach(BH2028)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH2028), new BountifulHarvest(SH3042), new Peach(BH2028)
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.呂布,
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
        Game game = new Game(gameId, players);
        game.setCurrentRound(new Round(playerB));
        game.enterPhase(new Normal(game));
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new BarbarianInvasion(SS7007)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerAHaveBountifulHarvest() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new BountifulHarvest(SH3042)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH2028), new Peach(BH2028), new Peach(BH2028)
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
        Game game = new Game(gameId, players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new BarbarianInvasion(SS7007)));
        game.setDeck(deck);
        repository.save(game);
    }
}
package com.gaas.threeKingdoms.e2e.scrollcard.ward;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.e2e.JsonFileWriterUtil;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WardWithDismantleTest extends AbstractBaseIntegrationTest {
    @DisplayName("""
        Given
        有 玩家 A B C D
        A 有過河拆橋
        B 有無懈可擊

        When
        A 出過河拆橋，指定 B
        A 指定 index 0

        Then
        A B C D 等待發動無懈可擊 的 event
        event 裡有可以發動無懈可擊 event 的 B
    """)
    @Test
    public void givenPlayerAHasDismantleAndPlayerBHasWard_WhenPlayerAPlaysDismantle_ThenBReceivesAskPlayWardEventAndABDReceiveWaitForWardEvent() throws Exception {
        givenPlayerAHaveDismantle();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();
        playerA.getHand().addCardToHand(List.of(
                new Kill(BS8008),
                new Peach(BH3029),
                new Dismantle(SS3003)
        ));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(List.of(
                new Ward(SSJ011)
        ));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        List<Player> players = List.of(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        game.setDeck(new Deck(List.of(
                new BarbarianInvasion(SSK013),
                new Dismantle(SS3003)
        )));
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));
        repository.save(game);

        // When
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SS3003", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Dismantle/player_a_play_dismantle_1_for_%s.json";

        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
          String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Dismantle/player_a_play_dismantle_2_for_%s.json";
        mockMvcUtil.useDismantleEffect(gameId, "player-a", "player-b", null, 0).andExpect(status().isOk()).andReturn();
        // Then

        for (String testPlayerId : playerIds) {
            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
//            testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @DisplayName("""
    Given
    有 玩家 A B C D
    A 有過河拆橋、無懈可擊
    B 有無懈可擊
    C 有無懈可擊

    When
    A 出過河拆橋，指定 B
    A 指定 index 0

    Then
    A B C D 等待發動無懈可擊 的 event
    event 裡有可以發動無懈可擊 event 的 A B C
""")
    @Test
    public void givenABCHaveWard_WhenAPlaysDismantle_ThenABCAreAskedToPlayWardAndDWaits() throws Exception {
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(List.of(
                new Dismantle(SS3003),
                new Ward(SSJ011)
        ));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = List.of(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        game.setDeck(new Deck(List.of(
                new BarbarianInvasion(SSK013),
                new Dismantle(SS3003)
        )));
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));
        repository.save(game);

        // When
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SS3003", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.useDismantleEffect(gameId, "player-a", "player-b", "", 0).andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Dismantle/player_a_play_dismantle_for_%s_v2.json";

        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @DisplayName("""
    Given
    有 玩家 ABCD
    A 有過河拆橋
    B 有無懈可擊

    A 出過河拆橋
    B 收到是否要發動無懈可擊的 event
    A C D 收到等待別人發動無懈可擊 的 event

    When
    B 出無懈可擊

    Then
    A B C D 收到 event 無懈可擊 抵銷了 過河拆橋
""")
    @Test
    public void givenPlayerAHasDismantleAndPlayerBHasWard_WhenPlayerAPlaysDismantleAndPlayerBPlaysWard_ThenAllPlayersReceiveWardEvent() throws Exception {
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008),
                new Peach(BH3029),
                new Dismantle(SS3003)
        ));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011), new Kill(BS8008), new Peach(BH3029), new Peach(BH2028), new Peach(BH2028), new Peach(BH2028)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        game.setDeck(new Deck(Arrays.asList(
                new BarbarianInvasion(SSK013),
                new Dismantle(SS3003)
        )));
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));
        repository.save(game);
//
        // Given 中的動作
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SS3003", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.useDismantleEffect(gameId, "player-a", "player-b", null, 0);
        popAllPlayerMessage();
        // When - B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andDo(print())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Dismantle/player_b_play_ward_to_counter_dismantle_for_%s.json";

        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @DisplayName("""
    Given
    有 玩家 ABCD
    A 有過河拆橋
    B 有無懈可擊
    C 有無懈可擊

    A 出過河拆橋

    When
    B 出無懈可擊

    Then
    A B C D 收到 PlayCard Event
    C 收到是否要發動無懈可擊的 event
""")
    @Test
    public void givenPlayerAHasDismantleAndPlayerBAndCHasWard_WhenPlayerAPlaysDismantleAndPlayerBPlaysWard_ThenCReceivesAskWard() throws Exception {
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(List.of(
                new Kill(BS8008),
                new Peach(BH3029),
                new Dismantle(SS3003)
        ));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.呂布))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = List.of(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        game.setDeck(new Deck(List.of(
                new BarbarianInvasion(SSK013),
                new Dismantle(SS3003)
        )));
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));
        repository.save(game);

        // Given: A 出過河拆橋
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SS3003", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.useDismantleEffect(gameId, "player-a", "player-b", "", 0).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();

        // Then: 檢查所有玩家收到正確事件
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Dismantle/player_b_play_ward_and_player_c_receive_ask_ward_event_for_%s.json";

        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @DisplayName("""
    Given
    有 玩家 ABCD
    A 有過河拆橋
    B 有無懈可擊
    C 有無懈可擊

    A 出過河拆橋
    B 出無懈可擊

    When
    C 出無懈可擊

    Then
    A B C D 收到 event >> C 的無懈可擊抵銷了 B 的無懈可擊
    A B C D 收到 A 過河拆橋發動 的 event
""")
    @Test
    public void givenPlayerAHasDismantleAndPlayerBAndCHasWard_WhenPlayerAPlaysDismantleAndPlayerBAndCPlaysWard_ThenCWardCounterBWardAndDismantleResolves() throws Exception {
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(List.of(
                new Kill(BS8008),
                new Peach(BH3029),
                new Dismantle(SS3003)
        ));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.呂布))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = List.of(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        game.setDeck(new Deck(List.of(
                new BarbarianInvasion(SSK013),
                new Dismantle(SS3003)
        )));
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));
        repository.save(game);

        // Given: A 出過河拆橋 + 指定效果
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SS3003", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.useDismantleEffect(gameId, "player-a", "player-b", "", 0).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: C 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-c", "SSJ011", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();

        // Then: 比對 JSON 檔案
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Dismantle/player_c_play_ward_to_counter_b_ward_and_trigger_dismantle_for_%s.json";

        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @DisplayName("""
    Given
    有 玩家 ABCD
    A 有過河拆橋
    A 有無懈可擊
    B 有無懈可擊
    C 有無懈可擊

    A 出過河拆橋
    B 出無懈可擊
    A 出無懈可擊

    When
    C 出無懈可擊

    Then
    A B C D 收到 event >> C 的無懈可擊抵銷了 A 的無懈可擊
    A B C D 收到 event >> B 的無懈可擊抵銷了 A 的過河拆橋
    A B C D 不會收到過河拆橋發動 的 event
""")
    @Test
    public void givenPlayerAHasDismantleAndPlayerAAndBAndCHasWard_WhenAllPlayWard_ThenFinalCounterLeadsToDismantleCancelled() throws Exception {
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(List.of(
                new Kill(BS8008),
                new Peach(BH3029),
                new Dismantle(SS3003),
                new Ward(SSJ011)
        ));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = List.of(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        game.setDeck(new Deck(List.of(
                new BarbarianInvasion(SSK013),
                new Dismantle(SS3003)
        )));
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));
        repository.save(game);

        // Given
        mockMvcUtil.playCard(gameId, playerA.getId(), playerB.getId(), "SS3003", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();

        popAllPlayerMessage();
        mockMvcUtil.useDismantleEffect(gameId, "player-a", "player-b", "", 0).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        mockMvcUtil.playWardCard(gameId, "player-a", "SSJ011", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When
        mockMvcUtil.playWardCard(gameId, "player-c", "SSJ011", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Dismantle/player_c_play_ward_to_counter_a_ward_and_cancel_dismantle_for_%s.json";

        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @DisplayName("""
    Given
    有 玩家 ABCD
    A 有過河拆橋
    A 有無懈可擊
    B 有無懈可擊
    C 有無懈可擊

    A 出過河拆橋
    B 出 skip 無懈可擊
    
    When
    C 出 skip 無懈可擊

    Then
    A B C D 收到過河拆橋發動 的 event
""")
    @Test
    public void givenPlayerAHasDismantleAndPlayerAAndBAndCHasWard_WhenAllSkipWard_ThenDismantleTriggers() throws Exception {
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(List.of(
                new Kill(BS8008),
                new Peach(BH3029),
                new Dismantle(SS3003),
                new Ward(SSJ011)
        ));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.呂布))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = List.of(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        game.setDeck(new Deck(List.of(
                new BarbarianInvasion(SSK013),
                new Dismantle(SS3003)
        )));
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));
        repository.save(game);

        // Given
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SS3003", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.useDismantleEffect(gameId, "player-a", "player-b", "", 0).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When
        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Dismantle/players_skip_ward_and_dismantle_resolves_for_%s.json";

        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @DisplayName("""
    Given
    有 玩家 ABCD
    A 有過河拆橋
    A 有無懈可擊
    B 有無懈可擊
    C 有無懈可擊

    A 出 過河拆橋
    B 出 無懈可擊
    A 出 無懈可擊

    When
    C 出 skip 無懈可擊

    Then
    A B C D 收到 event >> A 的無懈可擊 抵銷了 B 的無懈可擊 
    A B C D 收到 event >> A 過河拆橋發動 的 event
""")
    @Test
    public void givenPlayerAHasDismantleAndPlayerAAndBAndCHasWard_WhenPlayerAAndBPlayWardAndCSkip_ThenDismantleSucceeds() throws Exception {
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008),
                new Peach(BH3029),
                new Dismantle(SS3003),
                new Ward(SSJ011)
        ));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.呂布))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(Arrays.asList(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);

        // Given
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SS3003", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.useDismantleEffect(gameId, "player-a", "player-b", "", 0).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        mockMvcUtil.playWardCard(gameId, "player-a", "SSJ011", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When - C skip 無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Dismantle/player_c_skip_ward_after_a_ward_countered_b_ward_for_%s.json";

        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @DisplayName("""
            Given
            A有無懈可擊，B有過河拆橋，B的回合
            B對C使用過河拆橋。
            
            When
            B對C使用過河拆橋
            
            Then
            此時系統應詢問Ａ是否出無懈可擊，但A收到等待玩家出無懈可擊的event(AskPlayWardViewModel)
            """)
    @Test
    public void givenPlayerAHasWard_PlayerBHasDismantle_PlayerBTurn_WhenPlayerBPlaysDismantleOnC_ThenSystemAskAForWardEvent() throws Exception {
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008),
                new Peach(BH3029),
                new Ward(SSJ011)
        ));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(Arrays.asList(new Dismantle(SS3003)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerC.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.呂布))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerB);
        Deck deck = new Deck();
        deck.add(Arrays.asList(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);

        // Given
        mockMvcUtil.playCard(gameId, "player-b", "player-c", "SS3003", PlayType.ACTIVE.getPlayType()).andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.useDismantleEffect(gameId, "player-b", "player-c", "", 0).andExpect(status().isOk()).andReturn();


        // Then
        List<String> playerIds = List.of("player-a");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Dismantle/SystemAskAForWardEvent_for_%s.json";

        for (String testPlayerId : playerIds) {
            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
//            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    private void givenPlayerAHaveDismantle() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dismantle(SS3003)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Peach(BH3029), new Ward(SSJ011)
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.呂布,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void popAllPlayerMessage() {
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");
    }
}

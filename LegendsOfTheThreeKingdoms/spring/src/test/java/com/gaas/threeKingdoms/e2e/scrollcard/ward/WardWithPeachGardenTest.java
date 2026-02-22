package com.gaas.threeKingdoms.e2e.scrollcard.ward;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.JsonFileWriterUtil;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.PeachGarden;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WardWithPeachGardenTest extends AbstractBaseIntegrationTest {

    private static final String BASE_PATH = "src/test/resources/TestJsonFile/ScrollTest/Ward/PeachGarden/";

    @DisplayName("""
        Given
        有 玩家 A B C D
        A 有桃園結義
        B 有無懈可擊
        C hp=3 max=4

        When
        A 出桃園結義

        Then
        A B C D 等待發動無懈可擊 的 event
        event 裡有可以發動無懈可擊 event 的 B
    """)
    @Test
    public void givenPlayerAHasPeachGardenAndPlayerBHasWard_WhenPlayerAPlaysPeachGarden_ThenWaitForWardEvent() throws Exception {
        givenGameWithPeachGardenV1();

        // When: A 出桃園結義
        mockMvcUtil.playCard(gameId, "player-a", "", "SHA027", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_a_play_peach_garden_wait_ward_for_%s.json";

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
        有 玩家 A B C D
        A 有桃園結義
        B 有無懈可擊
        C hp=3 max=4

        A 出桃園結義

        When
        B 出無懈可擊

        Then
        A B C D 收到 event >> B 的無懈可擊抵銷了 A 的桃園結義
        C HP 不變 (仍為 3)
    """)
    @Test
    public void givenPlayerAPlaysPeachGarden_WhenPlayerBPlaysWard_ThenPeachGardenCancelled() throws Exception {
        givenGameWithPeachGardenV1();

        // Given: A 出桃園結義
        mockMvcUtil.playCard(gameId, "player-a", "", "SHA027", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_b_play_ward_cancel_peach_garden_for_%s.json";

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
        有 玩家 A B C D
        A 有桃園結義
        B 有無懈可擊
        C 有無懈可擊, hp=3 max=4

        A 出桃園結義
        B 出無懈可擊

        When
        C 出無懈可擊

        Then
        A B C D 收到 C 的無懈可擊抵銷了 B 的無懈可擊
        桃園結義生效，C HP = 4
    """)
    @Test
    public void givenBPlaysWard_WhenCPlaysWard_ThenPeachGardenTakesEffect() throws Exception {
        givenGameWithPeachGardenV2();

        // Given: A 出桃園結義
        mockMvcUtil.playCard(gameId, "player-a", "", "SHA027", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: C 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-c", "SCK078", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_c_play_ward_peach_garden_takes_effect_for_%s.json";

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
        有 玩家 A B C D
        A 有桃園結義
        B 有無懈可擊
        C 有無懈可擊, hp=3 max=4

        A 出桃園結義
        B skip 無懈可擊

        When
        C skip 無懈可擊

        Then
        桃園結義生效，C HP = 4
    """)
    @Test
    public void givenPlayersHaveWard_WhenAllSkip_ThenPeachGardenTakesEffect() throws Exception {
        givenGameWithPeachGardenV2();

        // Given: A 出桃園結義
        mockMvcUtil.playCard(gameId, "player-a", "", "SHA027", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // B skip 無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: C skip 無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "players_skip_ward_peach_garden_takes_effect_for_%s.json";

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
        有 玩家 A B C D
        A 有桃園結義 + 無懈可擊
        B 有無懈可擊
        C 有無懈可擊, hp=3 max=4

        A 出桃園結義
        B 出無懈可擊
        A 出無懈可擊

        When
        C skip 無懈可擊

        Then
        2 張無懈可擊 (偶數) → 桃園結義生效，C HP = 4
    """)
    @Test
    public void givenBPlaysWardAndAPlaysWard_WhenCSkips_ThenPeachGardenTakesEffect() throws Exception {
        givenGameWithPeachGardenV3();

        // Given: A 出桃園結義
        mockMvcUtil.playCard(gameId, "player-a", "", "SHA027", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // A 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-a", "SCQ077", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: C skip 無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_c_skip_ward_after_a_counter_b_peach_garden_takes_effect_for_%s.json";

        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    // V1: A 有桃園結義, B 有無懈可擊, C hp=3
    private void givenGameWithPeachGardenV1() {
        Player playerA = createPlayer(
                "player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new PeachGarden(SHA027)
        );
        Player playerB = createPlayer(
                "player-b", 4, General.張飛, HealthStatus.ALIVE, Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Ward(SSJ011)
        );
        Player playerC = createPlayer(
                "player-c", 4, General.關羽, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
        );
        playerC.damage(1); // hp=3

        Player playerD = createPlayer(
                "player-d", 4, General.呂布, HealthStatus.ALIVE, Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
        );

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    // V2: A 有桃園結義, B 有無懈可擊, C 有無懈可擊 + hp=3
    private void givenGameWithPeachGardenV2() {
        Player playerA = createPlayer(
                "player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new PeachGarden(SHA027)
        );
        Player playerB = createPlayer(
                "player-b", 4, General.張飛, HealthStatus.ALIVE, Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Ward(SSJ011)
        );
        Player playerC = createPlayer(
                "player-c", 4, General.關羽, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Ward(SCK078)
        );
        playerC.damage(1); // hp=3

        Player playerD = createPlayer(
                "player-d", 4, General.呂布, HealthStatus.ALIVE, Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
        );

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    // V3: A 有桃園結義 + 無懈可擊, B 有無懈可擊, C 有無懈可擊 + hp=3
    private void givenGameWithPeachGardenV3() {
        Player playerA = createPlayer(
                "player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new PeachGarden(SHA027), new Ward(SCQ077)
        );
        Player playerB = createPlayer(
                "player-b", 4, General.張飛, HealthStatus.ALIVE, Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Ward(SSJ011)
        );
        Player playerC = createPlayer(
                "player-c", 4, General.關羽, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Ward(SCK078)
        );
        playerC.damage(1); // hp=3

        Player playerD = createPlayer(
                "player-d", 4, General.呂布, HealthStatus.ALIVE, Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
        );

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void popAllPlayerMessage() {
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");
    }
}

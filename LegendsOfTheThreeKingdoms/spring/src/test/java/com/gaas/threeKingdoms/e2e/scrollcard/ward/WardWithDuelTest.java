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
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
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

public class WardWithDuelTest extends AbstractBaseIntegrationTest {

    @Test
    public void givenPlayerAHasDuelAndPlayerAAndBAndCHasWard_WhenPlayerAPlaysDuelAndPlayerAAndBAndCPlaysWard_ThenABCDReceive() throws Exception {
//        Given
//        有 玩家 ABCD
//        A 有決鬥
//        A 有無懈可擊
//        B 有無懈可擊
//        C 有無懈可擊
//
//        A 對 B 出 決鬥
//        C 出 無懈可擊
//        A 出 無懈可擊
//
//        When
//        B 出 無懈可擊
//
//        Then
//        A B C D 收到 B 張飛 的無懈可擊抵銷了 A 劉備 的無懈可擊
//        A B C D 收到 C 關羽 的無懈可擊抵銷了 A 劉備 的決鬥
        givenPlayerAHaveDuelV1();

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SSA001", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_a_play_duel_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        mockMvcUtil.playWardCard(gameId, "player-c", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_c_play_ward_to_player_a_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        mockMvcUtil.playWardCard(gameId, "player-a", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_a_play_ward_to_player_c_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_b_play_ward_to_player_a_for_%s.json";
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
    public void givenPlayerAHasDuelAndPlayerBHasWard_WhenPlayerAPlaysDuelAndPlayerBPlaysWard_ThenABCDReceive() throws Exception {
//        Given
//        有 玩家 A 劉備 B 張飛 C 關羽 D 呂布
//        A 有決鬥
//        B 有無懈可擊
//
//        A 對 B 出決鬥
//        A B C D 收到等待別人發動無懈可擊 的 event
//
//        When
//        B 出無懈可擊
//
//        Then
//        A B C D 收到 event B 張飛 的無懈可擊 抵銷了 A 劉備 中生有
        givenPlayerAHaveDuelV2();

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SSA001", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_a_play_duel_for_%s_v2.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_b_play_ward_to_player_a_for_%s_v2.json";
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
    public void givenPlayerAHasDuelAndPlayerBAndCHasWard_WhenPlayerAPlaysDuelAndPlayerBPlaysWard_ThenABCDReceive() throws Exception {

//        Given
//        有 玩家 A 劉備 B 張飛 C 關羽 D 呂布
//        A 有決鬥
//        B 有無懈可擊
//        C 有無懈可擊
//
//        A 出決鬥
//        B 出無懈可擊
//
//        When
//        C 出無懈可擊
//
//        Then
//        A B C D 收到 C 關羽 的無懈可擊抵銷了 B 張飛 的無懈可擊
//        A B C D 收到 A 劉備 決鬥發動 的 event
        givenPlayerAHaveDuelV3();

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SSA001", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_a_play_duel_for_%s_v3.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_b_play_ward_to_player_a_for_%s_v3.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        mockMvcUtil.playWardCard(gameId, "player-c", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_c_play_ward_for_%s_v3.json";
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
    public void givenPlayerAHasDuelAndPlayerAAndBAndCHasWard_WhenPlayerAPlaysDuelAndPlayerAAndBAndCSkipPlaysWard_ThenABCDReceive() throws Exception {

//        Given
//        有 玩家 A 劉備 B 張飛 C 關羽 D 呂布
//        A 有決鬥
//        A 有無懈可擊
//        B 有無懈可擊
//        C 有無懈可擊
//
//        A 出決鬥
//        B 出 skip 無懈可擊
//        A 出 skip 無懈可擊
//
//        When
//        C 出 skip 無懈可擊
//
//        Then
//        A B C D 收到 A 劉備 決鬥發動 的 event
        givenPlayerAHaveDuelV4();

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SSA001", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        popAllPlayerMessage();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_b_play_skip_ward_to_player_a_for_%s_v4.json";

        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        mockMvcUtil.playWardCard(gameId, "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_a_play_skip_ward_to_player_a_for_%s_v4.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_c_play_skip_ward_to_player_a_for_%s_v4.json";
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
    public void givenPlayerAHasDuelAndPlayerAAndBAndCHasWard_WhenPlayerAPlaysDuelAndPlayerAAndBPlayWardCardAndPlayerCSkipPlaysWard_ThenABCDReceive() throws Exception {

//        Given
//        有 玩家 A 劉備 B 張飛 C 關羽 D 呂布
//        A 有決鬥
//        A 有無懈可擊
//        B 有無懈可擊
//        C 有無懈可擊
//
//        A 出 決鬥
//        B 出 無懈可擊
//        A 出 無懈可擊
//
//        When
//        C 出 skip 無懈可擊
//
//        Then
//        A B C D 收到 event >> A 劉備 的無懈可擊 抵銷了 B 張飛 的無懈可擊
//        A B C D 收到 event >> A 決鬥發動 的 event
        givenPlayerAHaveDuelV5();

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SSA001", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        popAllPlayerMessage();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_b_play_ward_to_player_a_for_%s_v5.json";

        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        mockMvcUtil.playWardCard(gameId, "player-a", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_a_play_ward_to_player_a_for_%s_v5.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_c_play_skip_ward_to_player_a_for_%s_v5.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
//            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }


    @Test
    @DisplayName("Given: A對B出決鬥 / When: B出無懈可擊，而C和其他人放棄響應 / Then: 決鬥被抵銷，當前回合玩家應維持為A")
    public void givenPlayerADuelsB_WhenBPlaysWardAndOthersSkip_ThenActivePlayerShouldRemainA() throws Exception {
        // Given: A有決鬥, B和C有無懈可擊
        givenPlayerAHasDuelAndBCHaveWards();

        // A 對 B 出決鬥
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SSA001", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage(); // 清理 A 出決鬥後的訊息

        // When
        // B 對 A 的決鬥出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage(); // 清理 B 出無懈可擊後的訊息

        // C 收到詢問，選擇 skip
        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_c_play_skip_ward_to_player_a_for_%s_v6.json";
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
    @DisplayName("Given: A對B出決鬥, B有兩張無懈可擊 / When: B出無懈可擊後再skip / Then: 決鬥被抵銷, 當前回合玩家應維持為A")
    public void givenBHasTwoWards_WhenBWardsAndThenSkips_ActivePlayerShouldRemainA() throws Exception {
        // Given: A有決鬥, B有兩張無懈可擊
        givenPlayerAHasDuelAndBHasTwoWards();

        // A 對 B 出決鬥
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SSA001", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // When
        // B 對 A 的決鬥出第一張無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        // Then
        // 驗證當前回合玩家是否仍然是 A
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_b_play_skip_ward_and_check_active_player_to_player_a_for_%s_v5.json";
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
    @DisplayName("Given: A duels B / When: B skips Ward and plays Kill / Then: Game should ask A to play a Kill card")
    public void givenADuelsB_WhenBSkipsWardAndPlaysKill_ThenAskAForKill() throws Exception {
        // Given: A has a Duel, B has a Ward and a Kill.
        givenPlayerAHasDuelAndPlayerBHasWardAndKill();

        // A plays Duel on B
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SSA001", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage(); // Clear messages after initial card play

        // When
        // 1. B is asked to play Ward, but skips.
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_skip_ward_to_player_a_for_%s.json";

        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
//            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // Then
        // 2. B is asked to play Kill for the Duel, and plays it.
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/ask_player_a_for_kill_after_b_responds_for_%s.json";

        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
//            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }


    private void givenPlayerAHaveDuelV1() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dismantle(SS3003), new Ward(SSJ011), new Duel(SSA001)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH2028), new Peach(BH2028), new Ward(SSJ011)
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new Ward(SSJ011)
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
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerAHaveDuelV2() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dismantle(SS3003), new Duel(SSA001)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH2028), new Peach(BH2028), new Ward(SSJ011)
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
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerAHaveDuelV3() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dismantle(SS3003), new Duel(SSA001)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH2028), new Peach(BH2028), new Ward(SSJ011)
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new Ward(SSJ011)
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
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerAHaveDuelV4() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dismantle(SS3003), new Duel(SSA001), new Ward(SSJ011)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH2028), new Peach(BH2028), new Ward(SSJ011)
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new Ward(SSJ011)
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
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerAHaveDuelV5() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dismantle(SS3003), new Duel(SSA001), new Ward(SSJ011)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH2028), new Peach(BH2028), new Ward(SSJ011)
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new Ward(SSJ011)
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
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerAHasDuelAndBCHaveWards() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Duel(SSA001) // A 有決鬥
        );
        Player playerB = createPlayer("player-b",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Ward(SSJ011) // B 有無懈可擊
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Ward(SSJ011) // C 也有無懈可擊
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.呂布,
                HealthStatus.ALIVE,
                Role.TRAITOR
                // D 沒有無懈可擊
        );

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerAHasDuelAndBHasTwoWards() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Duel(SSA001) // A 有決鬥
        );
        Player playerB = createPlayer("player-b",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Ward(SSJ011), // B 有第一張無懈
                new Ward(SSJ011)  // B 有第二張無懈
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.REBEL
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.呂布,
                HealthStatus.ALIVE,
                Role.TRAITOR
        );

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerAHasDuelAndPlayerBHasWardAndKill() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Duel(SSA001), // A has Duel
                new Kill(BHK039)  // A also has a Kill to respond
        );
        Player playerB = createPlayer("player-b",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Ward(SSJ011), // B has a Ward (but won't use it)
                new Kill(BS8008)  // B has a Kill to play
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.REBEL
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.呂布,
                HealthStatus.ALIVE,
                Role.TRAITOR
        );

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    @Test
    @DisplayName("Given: A有決鬥 B有無懈可擊 C有無懈可擊 A有無懈可擊 / When: A對B出決鬥 B出無懈可擊 A C 不出無懈可擊 / Then: A的決鬥被B的無懈可擊取消，A後續操作不應該出現異常")
    public void givenAHasDuelBCHaveWard_WhenADuelsBAndBWardsACSkip_ThenAShouldBeAbleToPlayCardAndEndTurn() throws Exception {
        // Given: A有決鬥和無懈可擊, B和C有無懈可擊
        givenPlayerAHasDuelAndWardAndBCHaveWards();

        // A 對 B 出決鬥
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SSA001", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // B 對 A 的決鬥出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // A 收到詢問，選擇 skip (不對B的無懈出無懈)
        mockMvcUtil.playWardCard(gameId, "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // C 收到詢問，選擇 skip
        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_a_play_duel_to_player_b_and_player_b_and_c_skip_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }


//        popAllPlayerMessage();
//
//        // Then: A應該能夠正常出牌而不會出現NullPointerException
//        // A再出一張牌 (假設A還有其他牌)
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                    .andExpect(status().isOk());
//
//        // A應該能夠正常結束回合而不會出現IllegalStateException
//        mockMvcUtil.finishAction(gameId, "player-a");
    }

    @Test
    @DisplayName("Given: 只有A有無懈可擊與決鬥 / When: A對B使用決鬥 / Then: 決鬥直接使出，不詢問無懈可擊(本人不用詢問)")
    public void givenAOnlyHasDuel_WhenADuelsB_ThenAShouldNotReceiveWardEvent() throws Exception {
        // Given: A有決鬥和無懈可擊, B和C有無懈可擊
        givenPlayerOnlyAHasDuel();

        // A 對 B 出決鬥
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SSA001", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        // Then 決鬥直接使出，不詢問無懈可擊(本人不用詢問)
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Duel/player_a_play_duel_to_player_b_and_only_play_a_have_ward_for_%s.json";
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

    private void givenPlayerOnlyAHasDuel() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Duel(SSA001),  // A 有決鬥
                new Ward(SSJ011)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.MINISTER
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.REBEL
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.呂布,
                HealthStatus.ALIVE,
                Role.TRAITOR
        );

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }


    private void givenPlayerAHasDuelAndWardAndBCHaveWards() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Duel(SSA001),  // A 有決鬥
                new Ward(SSJ011),  // A 有無懈可擊
                new Kill(BS8008)   // A 有殺 (用於測試後續出牌)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Ward(SSJ011) // B 有無懈可擊
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Ward(SSJ011) // C 有無懈可擊
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.呂布,
                HealthStatus.ALIVE,
                Role.TRAITOR
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

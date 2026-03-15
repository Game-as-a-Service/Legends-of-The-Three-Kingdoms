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

public class WardWithBarbarianInvasionTest extends AbstractBaseIntegrationTest {

    private static final String BASE_PATH = "src/test/resources/TestJsonFile/ScrollTest/Ward/BarbarianInvasion/";

    @DisplayName("""
        Given
        玩家 A B C D，A 的回合
        A 有南蠻入侵
        B 有無懈可擊

        When
        A 出南蠻入侵

        Then
        A B C D 收到 PlayCardEvent + WaitForWardEvent
        B 收到 AskPlayWardEvent（B 有無懈可擊）
    """)
    @Test
    public void givenPlayerAHasBarbarianInvasionAndPlayerBHasWard_WhenPlayerAPlaysBarbarianInvasion_ThenWaitForWardEvent() throws Exception {
        givenBarbarianInvasionWithWardV1();

        // When: A 出南蠻入侵
        mockMvcUtil.playCard(gameId, "player-a", "", "SS7007", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_a_play_barbarian_ward_trigger_for_%s.json";

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
        玩家 A B C D，A 的回合
        A 有南蠻入侵
        B 有無懈可擊

        A 出南蠻入侵

        When
        B 出無懈可擊

        Then
        A B C D 收到 WardEvent，南蠻入侵被抵銷
        activePlayer = A
    """)
    @Test
    public void givenPlayerAPlaysBarbarianInvasion_WhenPlayerBPlaysWard_ThenBarbarianInvasionCancelled() throws Exception {
        givenBarbarianInvasionWithWardV1();

        // Given: A 出南蠻入侵
        mockMvcUtil.playCard(gameId, "player-a", "", "SS7007", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_b_play_ward_cancel_barbarian_for_%s.json";

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
        玩家 A B C D，A 的回合
        A 有南蠻入侵
        B 有無懈可擊
        C 有無懈可擊

        A 出南蠻入侵
        B 出無懈可擊

        When
        C 出無懈可擊（偶數 → 南蠻入侵生效）

        Then
        WardEvent + AskKillEvent for B
    """)
    @Test
    public void givenBCHaveWard_WhenEvenWards_ThenBarbarianInvasionProceeds() throws Exception {
        givenBarbarianInvasionWithWardV2();

        // Given: A 出南蠻入侵
        mockMvcUtil.playCard(gameId, "player-a", "", "SS7007", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: C 出無懈可擊（偶數）
        mockMvcUtil.playWardCard(gameId, "player-c", "SCK078", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_c_play_ward_even_barbarian_proceeds_for_%s.json";

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
        玩家 A B C D，A 的回合
        A 有南蠻入侵
        B 有無懈可擊（只有 B 有）

        A 出南蠻入侵

        When
        B skip Phase 1 無懈可擊

        Then
        0 張無懈可擊 → 南蠻入侵生效
        Phase 2: B 有無懈可擊 → WaitForWardEvent
    """)
    @Test
    public void givenPlayerBHasWard_WhenBSkipsPhase1Ward_ThenBarbarianInvasionProceeds() throws Exception {
        givenBarbarianInvasionWithWardV1();

        // Given: A 出南蠻入侵
        mockMvcUtil.playCard(gameId, "player-a", "", "SS7007", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: B skip Phase 1 無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_b_skip_ward_barbarian_proceeds_for_%s.json";

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
        玩家 A B C D，A 的回合
        A 有南蠻入侵
        B 有無懈可擊

        A 出南蠻入侵
        B skip Phase 1 無懈可擊 → Phase 2 for B → WaitForWardEvent

        When
        B 出無懈可擊（Phase 2 保護自己）

        Then
        B 被保護，C 收到 AskKillEvent
    """)
    @Test
    public void givenBSkipsPhase1_WhenBPlaysWardPhase2_ThenBProtectedAndCAskKill() throws Exception {
        givenBarbarianInvasionWithWardV1();

        // Given: A 出南蠻入侵
        mockMvcUtil.playCard(gameId, "player-a", "", "SS7007", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // B skip Phase 1 無懈可擊 → Phase 2 WaitForWardEvent
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: B 出無懈可擊（Phase 2 保護自己）
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_b_ward_phase2_protect_self_for_%s.json";

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
        玩家 A B C D，A 的回合
        A 有南蠻入侵
        B 有無懈可擊, C 有無懈可擊 (V2)

        A 出南蠻入侵
        B 出無懈可擊 (Phase 1)
        C 出無懈可擊 (偶數 → 生效) → AskKillEvent for B

        When
        B 出殺

        Then
        南蠻入侵繼續，C 收到 AskKillEvent
    """)
    @Test
    public void givenEvenWardsProceeds_WhenBPlaysKill_ThenCAskKillEvent() throws Exception {
        givenBarbarianInvasionWithWardV2();

        // Given: A 出南蠻入侵
        mockMvcUtil.playCard(gameId, "player-a", "", "SS7007", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // C 出無懈可擊（偶數 → 生效）→ AskKillEvent for B
        mockMvcUtil.playWardCard(gameId, "player-c", "SCK078", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: B 出殺
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_b_play_kill_then_c_ask_kill_for_%s.json";

        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    // V1: A 有南蠻入侵, B 有無懈可擊
    private void givenBarbarianInvasionWithWardV1() {
        Player playerA = createPlayer(
                "player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new BarbarianInvasion(SS7007), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
        );
        Player playerB = createPlayer(
                "player-b", 4, General.張飛, HealthStatus.ALIVE, Role.MINISTER,
                new Ward(SSJ011), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
        );
        Player playerC = createPlayer(
                "player-c", 4, General.關羽, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerD = createPlayer(
                "player-d", 4, General.呂布, HealthStatus.ALIVE, Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    // V2: A 有南蠻入侵, B 有無懈可擊, C 有無懈可擊
    private void givenBarbarianInvasionWithWardV2() {
        Player playerA = createPlayer(
                "player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new BarbarianInvasion(SS7007), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
        );
        Player playerB = createPlayer(
                "player-b", 4, General.張飛, HealthStatus.ALIVE, Role.MINISTER,
                new Ward(SSJ011), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
        );
        Player playerC = createPlayer(
                "player-c", 4, General.關羽, HealthStatus.ALIVE, Role.REBEL,
                new Ward(SCK078), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
        );
        Player playerD = createPlayer(
                "player-d", 4, General.呂布, HealthStatus.ALIVE, Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
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

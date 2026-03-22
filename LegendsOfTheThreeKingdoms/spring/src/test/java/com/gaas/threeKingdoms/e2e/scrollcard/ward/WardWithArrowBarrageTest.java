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
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
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

public class WardWithArrowBarrageTest extends AbstractBaseIntegrationTest {

    private static final String BASE_PATH = "src/test/resources/TestJsonFile/ScrollTest/Ward/ArrowBarrage/";

    @DisplayName("""
        Given
        玩家 A B C D，A 的回合
        A 有萬箭齊發
        B 有無懈可擊

        When
        A 出萬箭齊發

        Then
        A B C D 收到 PlayCardEvent + WaitForWardEvent
        B 收到 AskPlayWardEvent（B 有無懈可擊）
    """)
    @Test
    public void givenPlayerAHasArrowBarrageAndPlayerBHasWard_WhenPlayerAPlaysArrowBarrage_ThenWaitForWardEvent() throws Exception {
        givenArrowBarrageWithWardV1();

        // When: A 出萬箭齊發
        mockMvcUtil.playCard(gameId, "player-a", "", "SHA040", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_a_play_arrow_ward_trigger_for_%s.json";

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
        A 有萬箭齊發
        B 有無懈可擊

        A 出萬箭齊發

        When
        B 出無懈可擊

        Then
        A B C D 收到 WardEvent，萬箭齊發被抵銷
        activePlayer = A
    """)
    @Test
    public void givenPlayerAPlaysArrowBarrage_WhenPlayerBPlaysWard_ThenArrowBarrageCancelled() throws Exception {
        givenArrowBarrageWithWardV1();

        // Given: A 出萬箭齊發
        mockMvcUtil.playCard(gameId, "player-a", "", "SHA040", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_b_play_ward_cancel_arrow_for_%s.json";

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
        A 有萬箭齊發
        B 有無懈可擊
        C 有無懈可擊

        A 出萬箭齊發
        B 出無懈可擊

        When
        C 出無懈可擊（偶數 → 萬箭齊發生效）

        Then
        WardEvent + AskDodgeEvent for B
    """)
    @Test
    public void givenBCHaveWard_WhenEvenWards_ThenArrowBarrageProceeds() throws Exception {
        givenArrowBarrageWithWardV2();

        // Given: A 出萬箭齊發
        mockMvcUtil.playCard(gameId, "player-a", "", "SHA040", PlayType.ACTIVE.getPlayType())
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
        String filePathTemplate = BASE_PATH + "player_c_play_ward_even_arrow_proceeds_for_%s.json";

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
        A 有萬箭齊發
        B 有無懈可擊（只有 B 有）

        A 出萬箭齊發

        When
        B skip Phase 1 無懈可擊

        Then
        0 張無懈可擊 → 萬箭齊發生效
        Phase 2: B 有無懈可擊 → WaitForWardEvent
    """)
    @Test
    public void givenPlayerBHasWard_WhenBSkipsPhase1Ward_ThenArrowBarrageProceeds() throws Exception {
        givenArrowBarrageWithWardV1();

        // Given: A 出萬箭齊發
        mockMvcUtil.playCard(gameId, "player-a", "", "SHA040", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: B skip Phase 1 無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_b_skip_ward_arrow_proceeds_for_%s.json";

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
        A 有萬箭齊發
        B 有無懈可擊

        A 出萬箭齊發
        B skip Phase 1 無懈可擊 → Phase 2 for B → WaitForWardEvent

        When
        B 出無懈可擊（Phase 2 保護自己）

        Then
        B 被保護，C 收到 AskDodgeEvent
    """)
    @Test
    public void givenBSkipsPhase1_WhenBPlaysWardPhase2_ThenBProtectedAndCAskDodge() throws Exception {
        givenArrowBarrageWithWardV1();

        // Given: A 出萬箭齊發
        mockMvcUtil.playCard(gameId, "player-a", "", "SHA040", PlayType.ACTIVE.getPlayType())
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
        A 有萬箭齊發
        B 有無懈可擊, C 有無懈可擊 (V2)

        A 出萬箭齊發
        B 出無懈可擊 (Phase 1)
        C 出無懈可擊 (偶數 → 生效) → AskDodgeEvent for B

        When
        B 出閃

        Then
        萬箭齊發繼續，C 收到 AskDodgeEvent
    """)
    @Test
    public void givenEvenWardsProceeds_WhenBPlaysDodge_ThenCAskDodgeEvent() throws Exception {
        givenArrowBarrageWithWardV2();

        // Given: A 出萬箭齊發
        mockMvcUtil.playCard(gameId, "player-a", "", "SHA040", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // C 出無懈可擊（偶數 → 生效）→ AskDodgeEvent for B
        mockMvcUtil.playWardCard(gameId, "player-c", "SCK078", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: B 出閃
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BH2028", PlayType.INACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_b_play_dodge_then_c_ask_dodge_for_%s.json";

        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    // V1: A 有萬箭齊發, B 有無懈可擊
    private void givenArrowBarrageWithWardV1() {
        Player playerA = createPlayer(
                "player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new ArrowBarrage(SHA040), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
        );
        Player playerB = createPlayer(
                "player-b", 4, General.張飛, HealthStatus.ALIVE, Role.MINISTER,
                new Ward(SSJ011), new Dodge(BH2028), new Peach(BH3029), new Dodge(BHK039)
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

    // V2: A 有萬箭齊發, B 有無懈可擊, C 有無懈可擊
    private void givenArrowBarrageWithWardV2() {
        Player playerA = createPlayer(
                "player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new ArrowBarrage(SHA040), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
        );
        Player playerB = createPlayer(
                "player-b", 4, General.張飛, HealthStatus.ALIVE, Role.MINISTER,
                new Ward(SSJ011), new Dodge(BH2028), new Peach(BH3029), new Dodge(BHK039)
        );
        Player playerC = createPlayer(
                "player-c", 4, General.關羽, HealthStatus.ALIVE, Role.REBEL,
                new Ward(SCK078), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
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

    @DisplayName("""
        Given
        玩家 A B C D，B 的回合
        A 有 2 張無懈可擊 (SSJ011, SCQ077)
        B 有萬箭齊發 + 1 張無懈可擊 (SCK078)

        B 出萬箭齊發
        A skip Phase 1 Ward → Phase 2 開始
        Phase 2 for C: A skip Ward → C AskDodgeEvent → C skip → C 扣血
        Phase 2 for D: A 出 Ward (SSJ011) 保護 D

        When
        WaitForWardEvent → B 可以 counter（A 被排除）

        Then
        B skip counter → 1 Ward（奇數）→ D 被保護
        遊戲不會卡住，繼續到 A
    """)
    @Test
    public void givenCounterChainStuck_WhenBSkipsCounterWard_ThenDProtectedAndGameContinues() throws Exception {
        givenArrowBarrageCounterChain();

        // B plays ArrowBarrage
        mockMvcUtil.playCard(gameId, "player-b", "", "SHA040", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // Phase 1: A skips
        mockMvcUtil.playWardCard(gameId, "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // Phase 2 for C: A skips Ward → AskDodgeEvent for C
        mockMvcUtil.playWardCard(gameId, "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // C skips Dodge → takes damage
        mockMvcUtil.playCard(gameId, "player-c", "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // Phase 2 for D: A plays Ward (SSJ011) to protect D → WaitForWardEvent for counter
        mockMvcUtil.playWardCard(gameId, "player-a", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Validate: WaitForWardEvent shows B can counter, A excluded
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "counter_chain_a_ward_for_d_for_%s.json";
        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // B skips counter → 1 Ward (odd) → D protected → game continues to A
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Validate: D protected, game not stuck, continues to A
        filePathTemplate = BASE_PATH + "counter_chain_b_skip_d_protected_for_%s.json";
        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    // Counter-chain setup: B's turn, A has 2 Ward, B has ArrowBarrage + 1 Ward
    private void givenArrowBarrageCounterChain() {
        Player playerA = createPlayer(
                "player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Ward(SSJ011), new Ward(SCQ077), new Kill(BS8008), new Peach(BH3029)
        );
        Player playerB = createPlayer(
                "player-b", 4, General.張飛, HealthStatus.ALIVE, Role.MINISTER,
                new ArrowBarrage(SHA040), new Ward(SCK078), new Dodge(BH2028), new Peach(BH3029)
        );
        Player playerC = createPlayer(
                "player-c", 4, General.關羽, HealthStatus.ALIVE, Role.REBEL,
                new Peach(BH3029), new Kill(BS8008), new Kill(BS8008), new Kill(BS8008)
        );
        Player playerD = createPlayer(
                "player-d", 4, General.呂布, HealthStatus.ALIVE, Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerB);
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

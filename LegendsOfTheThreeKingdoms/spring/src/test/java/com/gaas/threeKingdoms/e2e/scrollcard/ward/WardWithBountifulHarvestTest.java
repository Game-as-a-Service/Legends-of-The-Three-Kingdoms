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
import com.gaas.threeKingdoms.handcard.scrollcard.BountifulHarvest;
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

public class WardWithBountifulHarvestTest extends AbstractBaseIntegrationTest {

    private static final String BASE_PATH = "src/test/resources/TestJsonFile/ScrollTest/Ward/BountifulHarvest/";

    // =============================================
    // Phase 1 Tests
    // =============================================

    @DisplayName("""
        Given
        玩家 A B C D，A 的回合
        A 有五穀豐登
        B 有無懈可擊

        When
        A 出五穀豐登

        Then
        A B C D 收到 PlayCardEvent + WaitForWardEvent
    """)
    @Test
    public void givenPlayerAHasBHAndPlayerBHasWard_WhenPlayerAPlaysBH_ThenWaitForWardEvent() throws Exception {
        givenBountifulHarvestWithWardV1();

        // When: A 出五穀豐登
        mockMvcUtil.playCard(gameId, "player-a", "", "SH3042", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_a_play_bh_ward_trigger_for_%s.json";

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
        A 有五穀豐登
        B 有無懈可擊

        A 出五穀豐登

        When
        B 出無懈可擊（1 張，奇數 → 抵銷）

        Then
        A B C D 收到 WardEvent，五穀豐登被抵銷
    """)
    @Test
    public void givenPlayerAPlaysBH_WhenPlayerBPlaysWard_ThenBHCancelled() throws Exception {
        givenBountifulHarvestWithWardV1();

        // Given: A 出五穀豐登
        mockMvcUtil.playCard(gameId, "player-a", "", "SH3042", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: B 出無懈可擊
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_b_play_ward_cancel_bh_for_%s.json";

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
        A 有五穀豐登
        B 有無懈可擊

        A 出五穀豐登

        When
        B skip Phase 1 Ward

        Then
        0 張無懈可擊 → 五穀豐登生效
        Phase 2: B 有無懈可擊 → WaitForWardEvent（target: A）
    """)
    @Test
    public void givenPlayerBHasWard_WhenBSkipsPhase1Ward_ThenBHProceedsPhase2() throws Exception {
        givenBountifulHarvestWithWardV1();

        // Given: A 出五穀豐登
        mockMvcUtil.playCard(gameId, "player-a", "", "SH3042", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: B skip Phase 1
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then: Phase 2 WaitForWardEvent for A's turn
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_b_skip_ward_bh_proceeds_for_%s.json";

        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    // =============================================
    // Phase 2 Tests
    // =============================================

    @DisplayName("""
        Given
        玩家 A B C D，A 的回合
        A 有五穀豐登
        B 有無懈可擊

        A 出五穀豐登
        B skip Phase 1 → Phase 2 for A → WaitForWardEvent
        B skip Phase 2 for A → A picks BS8008

        When
        Phase 2 for B → B plays Ward to skip themselves

        Then
        B skipped, C gets BountifulHarvestEvent
    """)
    @Test
    public void givenBSkipsPhase1_WhenBPlaysWardPhase2_ThenBSkippedAndCPicks() throws Exception {
        givenBountifulHarvestWithWardV1();

        // A plays BH → Phase 1 WaitForWardEvent
        mockMvcUtil.playCard(gameId, "player-a", "", "SH3042", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // Phase 1: B skips → Phase 2 for A → WaitForWardEvent
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // Phase 2 for A: B skips → A gets BountifulHarvestEvent
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // A picks BS8008
        mockMvcUtil.chooseCardFromBountifulHarvest(gameId, "player-a", "BS8008")
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: Phase 2 for B → B plays Ward to skip themselves
        mockMvcUtil.playWardCard(gameId, "player-b", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then: B skipped, C gets BountifulHarvestEvent
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_b_ward_phase2_skip_self_for_%s.json";

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
        A 有五穀豐登
        沒有人有無懈可擊

        When
        A 出五穀豐登

        Then
        直接開始選牌，A 收到 BountifulHarvestEvent
    """)
    @Test
    public void givenNoOneHasWard_WhenAPlaysBH_ThenDirectPicking() throws Exception {
        givenBountifulHarvestNoWard();

        // When: A 出五穀豐登
        mockMvcUtil.playCard(gameId, "player-a", "", "SH3042", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then: A gets BountifulHarvestEvent directly
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_a_play_bh_no_ward_for_%s.json";

        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    // =============================================
    // Phase 2 Bug Fix: C has Ward (not B)
    // =============================================

    @DisplayName("""
        Given
        玩家 A B C D，A 的回合
        A 有五穀豐登
        C 有無懈可擊

        A 出五穀豐登
        Phase 1: C skip → proceeds
        Phase 2 for A: C skip → A picks BS8008

        When
        A picks BS8008 (ChooseCardFromBountifulHarvest)

        Then
        Phase 2 for B: C 有 Ward → WaitForWardEvent
        C 收到 AskPlayWardEvent（不是 WaitForWardEvent），其他人收到 WaitForWardEvent
    """)
    @Test
    public void givenCHasWard_WhenAPicksCard_ThenCReceivesAskPlayWardEvent() throws Exception {
        givenBountifulHarvestWithWardV2();

        // A plays BH → Phase 1 WaitForWardEvent (C has Ward)
        mockMvcUtil.playCard(gameId, "player-a", "", "SH3042", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // Phase 1: C skips → 0 wards → proceeds to Phase 2
        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // Phase 2 for A: C skips → A gets BountifulHarvestEvent
        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When: A picks BS8008 → ChooseCardFromBountifulHarvestPresenter handles events
        // Phase 2 for B triggers WaitForWardEvent (C still has Ward)
        mockMvcUtil.chooseCardFromBountifulHarvest(gameId, "player-a", "BS8008")
                .andExpect(status().isOk()).andReturn();

        // Then: C should receive AskPlayWardEvent, others receive WaitForWardEvent
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = BASE_PATH + "player_a_picks_phase2_ward_for_b_for_%s.json";

        for (String testPlayerId : playerIds) {
//            String testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            String testPlayerJson = websocketUtil.getValue(testPlayerId);
            String fileSafeId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, fileSafeId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    // =============================================
    // Setup methods
    // =============================================

    // V2: A 有五穀豐登, C 有無懈可擊 (Ward 持有者不是相鄰玩家，用來測試 ChooseCardFromBountifulHarvestPresenter 的 per-player Ward 轉換)
    private void givenBountifulHarvestWithWardV2() {
        Player playerA = createPlayer(
                "player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new BountifulHarvest(SH3042), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
        );
        Player playerB = createPlayer(
                "player-b", 4, General.張飛, HealthStatus.ALIVE, Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerC = createPlayer(
                "player-c", 4, General.關羽, HealthStatus.ALIVE, Role.REBEL,
                new Ward(SSJ011), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
        );
        Player playerD = createPlayer(
                "player-d", 4, General.呂布, HealthStatus.ALIVE, Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)));
        game.setDeck(deck);
        repository.save(game);
    }

    // V1: A 有五穀豐登, B 有無懈可擊
    private void givenBountifulHarvestWithWardV1() {
        Player playerA = createPlayer(
                "player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new BountifulHarvest(SH3042), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
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
        // BH draws 4 cards (1 per player): BS8008, BH3029, BH2028, BHK039
        deck.add(List.of(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)));
        game.setDeck(deck);
        repository.save(game);
    }

    // No Ward: A 有五穀豐登, 沒有人有無懈可擊
    private void givenBountifulHarvestNoWard() {
        Player playerA = createPlayer(
                "player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new BountifulHarvest(SH3042), new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)
        );
        Player playerB = createPlayer(
                "player-b", 4, General.張飛, HealthStatus.ALIVE, Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
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
        deck.add(List.of(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)));
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

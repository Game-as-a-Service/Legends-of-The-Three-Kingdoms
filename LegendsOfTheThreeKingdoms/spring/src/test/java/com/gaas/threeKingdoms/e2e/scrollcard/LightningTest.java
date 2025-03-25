package com.gaas.threeKingdoms.e2e.scrollcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.JsonFileWriterUtil;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LightningTest extends AbstractBaseIntegrationTest {

    public LightningTest() {
        this.gameId = "my-id";
    }

    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasLightning_WhenPlayerAPlaysLightning_ThenPlayerAHasLightningInJudgmentArea() throws Exception {

        // Given
        // 玩家 A B C D
        // A 的回合
        // A 有閃電 x 1

        givenPlayerAHaveLightning();

        // When
        mockMvcUtil.playCard(gameId, "player-a", "", "SSA014", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/LightningBehavior/player_a_play_lightning_for_%s.json";

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
    public void givenPlayerABCD_PlayerATurn_PlayerAHasOneLightningInJudgmentArea_WhenPlayerAPlaysLightning_ThenPlayerAHasTwoLightningsInJudgmentArea() throws Exception {
        // Given
//        玩家ABCD
//        A的回合
//        A的判定牌閃電 x 1

        givenPlayerAHasOneLightningInJudgmentArea();

        // When Then
        mockMvcUtil.playCard(gameId, "player-a", "", "SSA014", PlayType.ACTIVE.getPlayType())
                .andExpect(status().is4xxClientError()); // 預期拋出例外（不能放第二張閃電）

    }

    @Test
    public void givenPlayerABCD_PlayerAPlaysLightning_WhenTurnEndsAndBStarts_ThenBStarts_AStillHasLightning_BHasNoJudgmentCard() throws Exception {
        // Given
        // 玩家ABCD，A 的回合，A 有閃電 x1
        givenPlayerAHaveLightning();

        // When
        // A 出閃電
        mockMvcUtil.playCard(gameId, "player-a", "", "SSA014", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        popAllPlayerMessage();

        // A 結束回合
        mockMvcUtil.finishAction(gameId, "player-a")
                .andExpect(status().is2xxSuccessful());

        // Then
        // 進入 B 的回合，A 仍有閃電，B 沒有判定牌
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/LightningBehavior/player_a_finish_turn_then_b_start_turn_for_%s.json";

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
    public void givenPlayerABCD_PlayerAPlaysLightning_ABCEndTurns_WhenPlayerDEndsTurn_ThenLightningTransfersToB_AReceivesDrawEvent_AHasTurn_ABCDReceiveEvents() throws Exception {
        // Given
        // 玩家ABCD，A 的回合，A 的手牌有閃電 x1，牌堆保證判定為紅心（不觸發閃電傷害）
        givenPlayerAPlaysLightningAndDeckIsAllRed();

        // When
        // A 出閃電
        mockMvcUtil.playCard(gameId, "player-a", "", "SSA014", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();
        // A、B、C 結束回合
        mockMvcUtil.finishAction(gameId, "player-a");
        popAllPlayerMessage();
        mockMvcUtil.finishAction(gameId, "player-b");
        popAllPlayerMessage();
        mockMvcUtil.finishAction(gameId, "player-c");
        popAllPlayerMessage();
        // D 結束回合（進入 A 的回合，觸發判定 → 閃電移轉）
        mockMvcUtil.finishAction(gameId, "player-d");

        // Then
        // 閃電轉移到 B，A 抽牌、是當前回合，所有玩家收到 LightningTransferredEvent 與 JudgementEvent
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/LightningBehavior/player_a_judgment_lightning_red_transfers_to_b_for_%s.json";

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
    public void givenPlayerABCD_AHas3Hp_PlaysLightning_NoPeach_ABCEndTurn_WhenDEndsTurnAndJudgmentSpade3_ThenAHas0Hp_AskAPlayPeach() throws Exception {
        // Given
        // 玩家ABCD，A 的回合，A HP = 3，手牌有閃電但沒有桃，牌堆為黑桃（觸發閃電）
        givenPlayerAHas3HpNoPeachAndLightningWithSpadeDeck();

        // When
        // A 出閃電
        mockMvcUtil.playCard(gameId, "player-a", "", "SSA014", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // A、B、C 結束回合
        mockMvcUtil.finishAction(gameId, "player-a");
        popAllPlayerMessage();
        mockMvcUtil.finishAction(gameId, "player-b");
        popAllPlayerMessage();
        mockMvcUtil.finishAction(gameId, "player-c");
        popAllPlayerMessage();

        // D 結束回合 → A 進入回合並觸發判定（扣 3 HP → 剩 0）
        mockMvcUtil.finishAction(gameId, "player-d");

        // Then
        // A HP = 0，系統發出 AskPeachEvent 要求 A 出桃
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/LightningBehavior/player_a_judgment_lightning_3hp_becomes_0_ask_peach_for_%s.json";

        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
//        testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @Test
    public void givenPlayerABCD_AHas3HpAndOnePeach_PlaysLightning_ABCDEndTurn_WhenJudgmentSpade3_HPBecomes0_APlaysPeach_ThenHP1_ReceiveEvents_DrawCard() throws Exception {
        // Given
        // 玩家A B C D，A 的回合，HP = 3，手牌有閃電與 1 張桃，牌堆為黑桃（閃電觸發），會變成 0 血但能補救
        givenPlayerAHas3HpAndOnePeachWithSpadeDeck();

        // When
        // A 出閃電
        mockMvcUtil.playCard(gameId, "player-a", "", "SSA014", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();
        // ABC 結束回合
        mockMvcUtil.finishAction(gameId, "player-a");
        popAllPlayerMessage();
        mockMvcUtil.finishAction(gameId, "player-b");
        popAllPlayerMessage();
        mockMvcUtil.finishAction(gameId, "player-c");
        popAllPlayerMessage();
        // D 結束回合 → A 判定 → HP 變 0 → 系統要求出桃
        mockMvcUtil.finishAction(gameId, "player-d");
        popAllPlayerMessage();
        // A 出桃（救回）
        mockMvcUtil.playCard(gameId, "player-a", "player-a", "BH3029", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        // Then
        // A HP 變 1，收到判定結束與抽牌事件，所有人同步狀態
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/LightningBehavior/player_a_judgment_hp0_play_peach_revive_for_%s.json";

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
    public void givenPlayerAHas2HpAndTwoPeach_PlaysLightning_BHasOnePeach_ABCDEndTurn_JudgmentSpade3_WhenMinus1Hp_APlaysPeach_Then0Hp_AskAgain() throws Exception {
        // Given
        // 玩家ABCD，A HP = 2，手上有閃電與兩張桃，B 有一張桃，牌堆為黑桃，會導致 A 扣 3 HP
        givenPlayerAHas2HpTwoPeach_BHasOnePeach_SpadeDeck();

        // When
        // A 出閃電
        mockMvcUtil.playCard(gameId, "player-a", "", "SSA014", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // ABC 結束回合，逐一清除訊息
        mockMvcUtil.finishAction(gameId, "player-a");
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-b");
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-c");
        popAllPlayerMessage();

        // D 結束回合，觸發 A 判定
        mockMvcUtil.finishAction(gameId, "player-d");
        popAllPlayerMessage();

        // A HP = -1，系統要求出桃 → A 出第一張桃
        mockMvcUtil.playCard(gameId, "player-a", "player-a", "BH3029", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        // Then
        // A HP = 0，系統再次要求出桃，確認訊息正確（下一步交由後續測試驗證）
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/LightningBehavior/player_a_judgment_hp_minus1_play_peach_then_0hp_ask_again_for_%s.json";

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
    public void givenPlayerAHas2HpTwoPeach_BHasOnePeach_JudgmentSpade3_WhenMinus1Hp_APlaysPeach_AskAgain_APlaysPeach_ThenHP1_ReceiveJudgmentAndDraw() throws Exception {
        // Given
        // 玩家ABCD，A HP = 2，手上有閃電與兩張桃，B 有一張桃，牌堆為黑桃，將造成 A -3 HP
        givenPlayerAHas2HpTwoPeach_BHasOnePeach_SpadeDeck();

        // When
        // A 出閃電
        mockMvcUtil.playCard(gameId, "player-a", "", "SSA014", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // ABC 結束回合
        mockMvcUtil.finishAction(gameId, "player-a");
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-b");
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-c");
        popAllPlayerMessage();

        // D 結束回合，觸發 A 判定（HP → -1）
        mockMvcUtil.finishAction(gameId, "player-d");
        popAllPlayerMessage();

        // A 出第一張桃（HP → 0）
        mockMvcUtil.playCard(gameId, "player-a", "player-a", "BH3029", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // A 出第二張桃（HP → 1）
        mockMvcUtil.playCard(gameId, "player-a", "player-a", "BH3029", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        // Then
        // A HP = 1，收到 JudgementEvent 與 DrawCardEvent，其他玩家同步狀態
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/LightningBehavior/player_a_judgment_hp_minus1_then_1_after_two_peaches_for_%s.json";

        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
//        testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @Test
    public void givenPlayerAHas2Peach_BHasOnePeach_JudgmentSpade3_WhenAPlayPeachThenSkip_ThenBPlaysPeach_ThenARevivesWithEvents() throws Exception {
        // Given
        // A HP = 2，有兩張桃，B 有一張桃，黑桃判定會使 A → -1 → 0，A skip，B 補桃
        givenPlayerAHas2Peach_BHasOnePeach_AndSpadeDeckForSkip();

        // When
        mockMvcUtil.playCard(gameId, "player-a", "", "SSA014", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-a");
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-b");
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-c");
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-d");
        popAllPlayerMessage();

        // A HP -3 → -1，出一張桃（→ 0）
        mockMvcUtil.playCard(gameId, "player-a", "player-a", "BH3029", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // 再次要求出桃，A 選擇 skip
        mockMvcUtil.playCard(gameId, "player-a", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // 系統要求 B 補桃
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BH3029", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        // Then
        // A HP = 1，收到抽牌與判定結束事件，所有玩家同步
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/LightningBehavior/player_a_skip_then_b_rescue_with_peach_for_%s.json";

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
    public void givenAllPlayersNoPeach_JudgmentSpade3_APlaysSkip_AllSkip_ThenADeath_BStartsTurn() throws Exception {
        // Given
        // 玩家ABCD 都沒有桃，A 出閃電，黑桃判定 → -3 HP → A 全場無人可救
        givenAllPlayersNoPeachWithSpadeDeck();

        // When
        mockMvcUtil.playCard(gameId, "player-a", "", "SSA014", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-a");
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-b");
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-c");
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-d");
        popAllPlayerMessage();

        // A 判定 → HP -3 → -1，系統要求出桃 → A skip
        mockMvcUtil.playCard(gameId, "player-a", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // B skip
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // C skip
        mockMvcUtil.playCard(gameId, "player-c", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // D skip
        mockMvcUtil.playCard(gameId, "player-d", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());

        // Then
        // A 死亡，所有人收到 SettlementEvent，進入 B 的回合
        List<String> playerIds = List.of("player-b", "player-c", "player-d"); // A 已死亡
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/LightningBehavior/player_a_all_skip_then_a_dies_b_starts_for_%s.json";

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
    public void givenAHas1Hp_BHas3Peach_JudgmentSpade3_WhenMinus2Hp_APlaySkip_BPlayPeachTwice_ThenARevivedWith1HpAndReceivesEvents() throws Exception {
        // Given
        // A HP = 1，手牌無桃，B 有 3 張桃，黑桃判定 → A 扣 3 HP → -2，B 出兩次桃救回
        givenPlayerA1HpNoPeach_BHas3Peach_WithSpadeDeck();

        // When
        mockMvcUtil.playCard(gameId, "player-a", "", "SSA014", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-a");
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-b");
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-c");
        popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-d");
        popAllPlayerMessage();

        // A 判定 → -2 HP，選擇 skip
        mockMvcUtil.playCard(gameId, "player-a", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // B 補第一張桃（→ -1 HP）
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BH3029", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // B 補第二張桃（→ -1 → 0 HP）
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BH3029", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAllPlayerMessage();

        // B 補第二張桃（→ 0 → 1 HP）
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BH3029", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        // Then
        // A 復活，HP = 1，收到 JudgementEnd 與 DrawCard，其他人同步
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/LightningBehavior/player_a_skip_b_peach_twice_then_revived_for_%s.json";

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

    private void givenPlayerA1HpNoPeach_BHas3Peach_WithSpadeDeck() {
        Player playerA = createPlayer(
                "player-a",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Lightning(SSA014) // 無桃
        );

        Player playerB = createPlayer(
                "player-b",
                7,
                General.關羽,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Peach(BH3029),
                new Peach(BH3029),
                new Peach(BH3029),
                new Peach(BH3029)
        );

        Player playerC = createPlayer("player-c", 4, General.張飛, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.趙雲, HealthStatus.ALIVE, Role.REBEL);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);

        Stack<HandCard> cards = new Stack<>();
        for (int i = 0; i < 36; i++) {
            cards.add(new Dismantle(SS3003));
        }

        Deck deck = new Deck();
        deck.add(cards);
        game.setDeck(deck);

        repository.save(game);
    }

    private void givenAllPlayersNoPeachWithSpadeDeck() {
        Player playerA = createPlayer(
                "player-a",
                2,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Lightning(SSA014)
        ); // 無桃

        Player playerB = createPlayer("player-b", 4, General.關羽, HealthStatus.ALIVE, Role.MONARCH); // 無桃
        Player playerC = createPlayer("player-c", 4, General.張飛, HealthStatus.ALIVE, Role.REBEL);   // 無桃
        Player playerD = createPlayer("player-d", 4, General.趙雲, HealthStatus.ALIVE, Role.REBEL);   // 無桃

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);

        // 8 次行為 × 4 = 32 張卡
        Stack<HandCard> cards = new Stack<>();
        for (int i = 0; i < 32; i++) {
            cards.add(new Dismantle(SS3003));
        }

        Deck deck = new Deck();
        deck.add(cards);
        game.setDeck(deck);

        repository.save(game);
    }

    private void givenPlayerAHas2Peach_BHasOnePeach_AndSpadeDeckForSkip() {
        Player playerA = createPlayer(
                "player-a",
                2,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Lightning(SSA014),
                new Peach(BH3029),
                new Peach(BH3029)
        );

        Player playerB = createPlayer(
                "player-b",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Peach(BH3029)
        );

        Player playerC = createPlayer("player-c", 4, General.張飛, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.趙雲, HealthStatus.ALIVE, Role.REBEL);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);

        // 7 次行為（出牌 + finishAction + 桃）→ 需要至少 7 * 4 = 28 張
        Stack<HandCard> cards = new Stack<>();
        for (int i = 0; i < 28; i++) {
            cards.add(new Dismantle(SS3003));
        }

        Deck deck = new Deck();
        deck.add(cards);
        game.setDeck(deck);

        repository.save(game);
    }

    private void givenPlayerAHas2HpTwoPeach_BHasOnePeach_SpadeDeck() {
        Player playerA = createPlayer(
                "player-a",
                2,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Lightning(SSA014),
                new Peach(BH3029),
                new Peach(BH3029)
        );

        Player playerB = createPlayer(
                "player-b",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Peach(BH3029)
        );

        Player playerC = createPlayer("player-c", 4, General.張飛, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.趙雲, HealthStatus.ALIVE, Role.REBEL);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);

        Deck deck = new Deck();
        deck.add(List.of(
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003)
        ));
        game.setDeck(deck);

        repository.save(game);
    }

    private void givenPlayerAHas3HpAndOnePeachWithSpadeDeck() {
        Player playerA = createPlayer(
                "player-a",
                3,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Lightning(SSA014),
                new Peach(BH3029)
        );

        Player playerB = createPlayer("player-b", 4, General.關羽, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.張飛, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.趙雲, HealthStatus.ALIVE, Role.REBEL);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);

        Deck deck = new Deck();
        deck.add(List.of(
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003)
        )); // 黑桃，閃電觸發
        game.setDeck(deck);

        repository.save(game);
    }

    private void givenPlayerAHas3HpNoPeachAndLightningWithSpadeDeck() {
        Player playerA = createPlayer(
                "player-a",
                3,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Lightning(SSA014)
        ); // 無桃

        Player playerB = createPlayer("player-b", 4, General.關羽, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.張飛, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.趙雲, HealthStatus.ALIVE, Role.REBEL);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);

        // 設定牌堆為黑桃 → 閃電會扣 3 HP
        Deck deck = new Deck();
        deck.add(List.of(
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003),
                new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003)
        ));
        game.setDeck(deck);

        repository.save(game);
    }

    private void givenPlayerAPlaysLightningAndDeckIsAllRed() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Lightning(SSA014)
        );
        Player playerB = createPlayer("player-b", 4, General.關羽, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.張飛, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.趙雲, HealthStatus.ALIVE, Role.REBEL);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);

        // 設定牌堆為紅心，保證判定不觸發閃電
        Deck deck = new Deck();
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)
        ));
        game.setDeck(deck);

        repository.save(game);
    }

    private void givenPlayerAHasOneLightningInJudgmentArea() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Lightning(SSA014)
        );
        playerA.getDelayScrollCards().add(new Lightning(SSA014)); // A 已有一張閃電

        Player playerB = createPlayer("player-b", 4, General.關羽, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.張飛, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.趙雲, HealthStatus.ALIVE, Role.REBEL);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }

    private void givenPlayerAHaveLightning() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Lightning(SSA014)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.關羽,
                HealthStatus.ALIVE,
                Role.MINISTER
        );
        Player playerC = createPlayer("player-c",
                4,
                General.張飛,
                HealthStatus.ALIVE,
                Role.REBEL
        );
        Player playerD = createPlayer("player-d",
                4,
                General.趙雲,
                HealthStatus.ALIVE,
                Role.REBEL
        );
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)
        ));
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

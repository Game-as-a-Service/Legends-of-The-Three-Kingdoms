package com.gaas.threeKingdoms.e2e.scrollcard.ward;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.JsonFileWriterUtil;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
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

public class WardWithContentmentTest extends AbstractBaseIntegrationTest {

    @Test
    public void givenPlayerBHasContentmentAndCHasWard_WhenAFinishesAction_ThenWaitForWardEvent() throws Exception {
//        Given
//        玩家 ABCD，A 的回合
//        B 判定區有樂不思蜀
//        C 有無懈可擊
//
//        When
//        A 結束回合 → B 的回合開始
//
//        Then
//        ABCD 收到 WaitForWardEvent (C 可以出無懈可擊)
        givenPlayerBHasContentmentAndCHasWard();

        mockMvcUtil.finishAction(gameId, "player-a")
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Contentment/ward_trigger_on_contentment_for_%s.json";
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
    public void givenPlayerBHasContentmentAndCHasWard_WhenCPlaysWard_ThenContentmentCancelled() throws Exception {
//        Given
//        玩家 ABCD，A 的回合
//        B 判定區有樂不思蜀
//        C 有無懈可擊
//
//        A 結束回合 → WaitForWardEvent
//
//        When
//        C 出無懈可擊 (count=1, odd → 取消)
//
//        Then
//        ABCD 收到 Ward 取消樂不思蜀，B 正常抽牌進入 Action phase
        givenPlayerBHasContentmentAndCHasWard();

        mockMvcUtil.finishAction(gameId, "player-a")
                .andExpect(status().isOk()).andReturn();

        popAllPlayerMessage();

        mockMvcUtil.playWardCard(gameId, "player-c", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Contentment/ward_cancels_contentment_for_%s.json";
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
    public void givenPlayerBHasContentmentAndCHasWard_WhenCSkipsWard_ThenContentmentProceeds() throws Exception {
//        Given
//        玩家 ABCD，A 的回合
//        B 判定區有樂不思蜀，B HP=1 手牌4張
//        C 有無懈可擊
//        Deck 頂是黑桃（樂不思蜀生效）
//
//        A 結束回合 → WaitForWardEvent
//
//        When
//        C skip 無懈可擊
//
//        Then
//        ABCD 收到 ContentmentEvent (success)，B 進入 Discard phase
        givenPlayerBHasContentmentAndCHasWardForSkip();

        mockMvcUtil.finishAction(gameId, "player-a")
                .andExpect(status().isOk()).andReturn();

        popAllPlayerMessage();

        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Contentment/skip_ward_contentment_proceeds_for_%s.json";
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
    public void givenPlayerBHasContentmentAndNoOneHasWard_WhenAFinishesAction_ThenContentmentJudgementImmediately() throws Exception {
//        Given
//        玩家 ABCD，A 的回合
//        B 判定區有樂不思蜀，B HP=1 手牌4張
//        沒人有無懈可擊
//        Deck 頂是黑桃（樂不思蜀生效）
//
//        When
//        A 結束回合
//
//        Then
//        ABCD 直接收到 ContentmentEvent (success)，沒有 WaitForWardEvent
//        B 進入 Discard phase
        givenPlayerBHasContentmentAndNoOneHasWard();

        mockMvcUtil.finishAction(gameId, "player-a")
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Contentment/no_ward_contentment_immediate_for_%s.json";
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

    private void givenPlayerBHasContentmentAndCHasWard() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Kill(BS8008)
        );
        Player playerB = createPlayer(
                "player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)
        );
        playerB.addDelayScrollCard(new Contentment(SC6071));

        Player playerC = createPlayer(
                "player-c",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Ward(SSJ011)
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
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029)
        ));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerBHasContentmentAndCHasWardForSkip() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Kill(BS8008)
        );
        Player playerB = createPlayer(
                "player-b",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)
        );
        playerB.addDelayScrollCard(new Contentment(SC6071));

        Player playerC = createPlayer(
                "player-c",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Ward(SSJ011)
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
        // LIFO: last = first drawn. Judgement card (Spade) on top, then drawing phase cards
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029),
                new BarbarianInvasion(SSK013)   // judgement card: Spade -> Contentment succeeds
        ));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerBHasContentmentAndNoOneHasWard() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Kill(BS8008)
        );
        Player playerB = createPlayer(
                "player-b",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)
        );
        playerB.addDelayScrollCard(new Contentment(SC6071));

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
        // LIFO: last = first drawn. Judgement card (Spade) on top, then drawing phase cards
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029),
                new BarbarianInvasion(SSK013)   // judgement card: Spade -> Contentment succeeds
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

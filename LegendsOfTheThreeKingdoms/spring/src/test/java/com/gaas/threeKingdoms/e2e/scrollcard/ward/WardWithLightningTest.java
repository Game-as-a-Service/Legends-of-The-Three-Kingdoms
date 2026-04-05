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
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
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

public class WardWithLightningTest extends AbstractBaseIntegrationTest {

    @Test
    public void givenPlayerAHasLightningAndCHasWard_WhenDFinishesAction_ThenWaitForWardEvent() throws Exception {
//        Given
//        玩家 ABCD，D 的回合
//        A 判定區有閃電
//        C 有無懈可擊
//
//        When
//        D 結束回合 → A 的回合開始
//
//        Then
//        ABCD 收到 WaitForWardEvent (C 可以出無懈可擊)
        givenPlayerAHasLightningAndCHasWard();

        mockMvcUtil.finishAction(gameId, "player-d")
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Lightning/ward_trigger_on_lightning_for_%s.json";
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
    public void givenPlayerAHasLightningAndCHasWard_WhenCPlaysWard_ThenLightningCancelledAndTransfersToB() throws Exception {
//        Given
//        玩家 ABCD，D 的回合
//        A 判定區有閃電
//        C 有無懈可擊
//
//        D 結束回合 → WaitForWardEvent
//
//        When
//        C 出無懈可擊 (count=1, odd → 取消)
//
//        Then
//        ABCD 收到 Ward 取消閃電，閃電傳 B，A 正常抽牌進入 Action phase
        givenPlayerAHasLightningAndCHasWard();

        mockMvcUtil.finishAction(gameId, "player-d")
                .andExpect(status().isOk()).andReturn();

        popAllPlayerMessage();

        mockMvcUtil.playWardCard(gameId, "player-c", "SSJ011", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Lightning/ward_cancels_lightning_transfers_to_b_for_%s.json";
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
    public void givenPlayerAHasLightningAndCHasWard_WhenCSkipsWard_ThenLightningJudgementProceeds() throws Exception {
//        Given
//        玩家 ABCD，D 的回合
//        A 判定區有閃電
//        C 有無懈可擊
//        Deck 頂是紅心（閃電判定失敗，安全，傳下家）
//
//        D 結束回合 → WaitForWardEvent
//
//        When
//        C skip 無懈可擊
//
//        Then
//        ABCD 收到 LightningEvent (fail)，閃電傳 B，A 正常抽牌
        givenPlayerAHasLightningAndCHasWardForSkip();

        mockMvcUtil.finishAction(gameId, "player-d")
                .andExpect(status().isOk()).andReturn();

        popAllPlayerMessage();

        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Ward/Lightning/skip_ward_lightning_proceeds_for_%s.json";
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

    private void givenPlayerAHasLightningAndCHasWard() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Kill(BS8008)
        );
        playerA.addDelayScrollCard(new Lightning(SSA014));

        Player playerB = createPlayer(
                "player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)
        );
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
        game.setCurrentRound(new Round(playerD));
        game.enterPhase(new Normal(game));
        Deck deck = new Deck();
        // Cards for A's drawing phase + spare
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029)
        ));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerAHasLightningAndCHasWardForSkip() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Kill(BS8008)
        );
        playerA.addDelayScrollCard(new Lightning(SSA014));

        Player playerB = createPlayer(
                "player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)
        );
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
        game.setCurrentRound(new Round(playerD));
        game.enterPhase(new Normal(game));
        Deck deck = new Deck();
        // LIFO: Lightning judgement card (Heart -> fail, safe) + A's drawing phase
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029)                        // Lightning judgement: Heart -> fail (safe)
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

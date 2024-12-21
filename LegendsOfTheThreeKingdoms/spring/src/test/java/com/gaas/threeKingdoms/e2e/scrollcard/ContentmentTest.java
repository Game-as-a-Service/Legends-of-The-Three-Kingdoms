package com.gaas.threeKingdoms.e2e.scrollcard;

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

public class ContentmentTest extends AbstractBaseIntegrationTest {


    @Test
    public void givenPlayerABCD_PlayerAHasContentment_WhenPlayerAPlaysContentmentToPlayerB_ThenPlayerBHaveContentment1() throws Exception {

//       Given
//       玩家ABCD
//       A的回合
//       A有樂不思蜀x 1
//       A 出樂不思蜀，指定 B
//
//       When
//       A 結束回合
//       B 的回合，系統進行樂不思蜀 判定，抽出一張紅桃卡
//
//       Then
//       B 不能出牌，進入到棄牌階段
//       DiscardPhase

        givenPlayerAHaveContentment();

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SC6071", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ContentmentBehavior/player_a_play_contentment_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        mockMvcUtil.finishAction(gameId, "player-a")
                .andExpect(status().isOk()).andReturn();

        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ContentmentBehavior/player_b_contentment_effect_success_for_%s.json";
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
    public void givenPlayerABCD_PlayerATurn_PlayerAHasContentment_WhenPlayerAPlaysContentmentAndAssignsB_AndPlayerATurnEnds_SystemDrawsCardForBContentmentJudgment_ThenPlayerBProceedsToNormalPhase() throws Exception {

//      Given
//      玩家 ABCD
//      A 的回合
//      A 有樂不思蜀 x 1
//      A 出樂不思蜀，指定 B玩家
//
//      When
//      A 結束回合
//      B 的回合，系統進行樂不思蜀判定，抽出一張黑桃 7
//
//      Then
//      B 正常出牌
//      NormalPhase

        givenPlayerAHaveContentmentAndDeckIsSpadeSeven();

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SC6071", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        websocketUtil.popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-a");

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ContentmentBehavior/player_b_contentment_effect_fail_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            //testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    private void givenPlayerAHaveContentment() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Contentment(SC6071)
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
        deck.add(List.of(new Peach(BH7033), new Peach(BH7033), new Peach(BH7033), new Peach(BH7033), new Peach(BH7033), new Peach(BH7033)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerAHaveContentmentAndDeckIsSpadeSeven() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Contentment(SC6071)
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
        deck.add(List.of(new BarbarianInvasion(SS7007), new BarbarianInvasion(SS7007), new BarbarianInvasion(SS7007), new BarbarianInvasion(SS7007), new BarbarianInvasion(SS7007)));
        game.setDeck(deck);

        repository.save(game);
    }

}

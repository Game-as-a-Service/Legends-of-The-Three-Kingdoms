package com.gaas.threeKingdoms.e2e.scrollcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.JsonFileWriterUtil;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
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

public class DuelTest extends AbstractBaseIntegrationTest {

    public DuelTest() {
        this.gameId = "my-id";
    }

    private String duelCardId = "SDA079";

    @Test
    public void givenPlayerABCD_PlayerAPlayDuel_WhenPlayerBHaveNoKill_ThenPlayerBHpMinusOne() throws Exception {
//        Given
//        玩家 A B C D
//        A 的回合
//        A 有決鬥 x 1 沒有殺
//
//        B 沒有殺，B 4 hp
//
        givenPlayerAHaveDuel();

//        When
//        A 出決鬥，指定 B
        mockMvcUtil.playCard(gameId, "player-a", "player-b", duelCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

//        Then
//        B 扣血，B 3 hp
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Duel/player_a_player_duel_for_%s.json";
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
                .andExpect(status().is2xxSuccessful()).andReturn();
    }

    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasDuelAndKillWith4HP_BPlayerHasTwoKillsAnd4HP_WhenPlayerAPlaysDuelAndAssignsB_AndPlayersAlternateKillsUntilBPlaysLastKill_ThenPlayerALoses1HPAndIsAt3HPWhilePlayerBRemainsAt4HP() throws Exception {
        //            Given
        //            玩家ABCD
        //            A的回合
        //            A有決鬥 x 1,殺 x 1, A 4hp
        //
        //            B 殺 x 2，B 4hp
        givenPlayerAHaveDuelVer2();

        //            When
        //            A 出決鬥，指定 B
        //            B 出殺
        //            A 出殺
        //            B 出殺
        mockMvcUtil.playCard(gameId, "player-a", "player-b", duelCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        //            Then
        //            A 扣血, A 3hp
        //            B 沒扣血, B 4hp
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Duel/player_a_player_duel_and_player_b_play_kill_for_%s.json";
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
    public void givenPlayerABCD_PlayerATurn_PlayerAHasDuelAndNoKillWith4HP_BPlayerHasKillAnd4HP_WhenPlayerAPlaysDuelAndAssignsB_AndPlayerBDoesNotPlayKill_ThenPlayerADoesNotLoseHPAndRemainsAt4HPWhilePlayerBLoses1HPAndIsAt3HP() throws Exception {
        //             Given
        //            玩家 A B C D
        //            A的回合
        //            A有決鬥 x 1,殺 x 1, A 4hp
        //            B 殺 x 2，B 4hp
        givenPlayerAHaveDuelVer2();

        //            When
        //            A 出決鬥，指定 B
        //            B 不出殺
        mockMvcUtil.playCard(gameId, "player-a", "player-b", duelCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        //            Then
        //            A 不扣血, A 4hp
        //            B 扣血, B 3hp
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Duel/player_a_player_duel_and_player_b_skip_for_%s.json";
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
    public void givenPlayerABCD_PlayerATurn_PlayerAHasDuelAndKillWith1HP_BPlayerHasTwoKillsAnd1HP_WhenPlayerAPlaysDuelAndAssignsB_AndPlayersAlternateKillsUntilBDoesNotPlayKill_ThenPlayerADoesNotLoseHPAndRemainsAt4HPWhilePlayerBLoses1HPAndDead() throws Exception {
//        Given
//        玩家 A 主公 B 反賊 C 忠臣 D 內奸
//        A的回合
//        A有決鬥 x 1,殺 x 1, A 4hp
//        B 殺 x 2，B 1hp
        givenPlayerAHaveDuelAndPlayerAWin();
//
//        When
//        A 出決鬥，指定 B
//        B 出殺
//        A 出殺
//        B 不出殺
//        A 沒扣血, A 4hp
//        B 扣血, B 0hp
//        B C D A 被詢問要不要出桃 skip

        // A plays Duel and assigns B
        mockMvcUtil.playCard(gameId, "player-a", "player-b", duelCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        // B C D A are asked if they want to play Peach

        mockMvcUtil.playCard(gameId, "player-b", "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        mockMvcUtil.playCard(gameId, "player-c", "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        mockMvcUtil.playCard(gameId, "player-d", "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
//        Then
//        反賊 B 死亡，A 抽三張牌
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Duel/player_a_player_duel_and_player_a_win_for_%s.json";
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

    private void popAllPlayerMessage() {
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");
    }

    private void givenPlayerAHaveDuelAndPlayerAWin() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Peach(BH3029), new Kill(BS8008), new Dodge(BH2028), new Dodge(BHK039), new Duel(SDA079)
        );
        Player playerB = createPlayer("player-b",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Kill(BS8008), new Dodge(BHK039), new Duel(SSA001)
        );
        Player playerC = createPlayer(
                "player-c",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
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
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerAHaveDuelVer2() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Duel(SDA079)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Kill(BS8008), new Peach(BH4030), new Dodge(BH2028)
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
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }


    private void givenPlayerAHaveDuel() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Duel(SDA079)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Peach(BH4030), new Dodge(BH2028)
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
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }


}

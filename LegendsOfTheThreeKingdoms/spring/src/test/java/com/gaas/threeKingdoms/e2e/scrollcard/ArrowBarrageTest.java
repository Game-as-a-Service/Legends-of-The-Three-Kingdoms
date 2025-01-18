package com.gaas.threeKingdoms.e2e.scrollcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
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

public class ArrowBarrageTest extends AbstractBaseIntegrationTest {

    @Test
    public void testPlayerBPlayArrowBarrage() throws Exception {
//        Given A玩家有萬箭齊發
//        玩家ABCD
//        B的回合
//        C 玩家有一張閃
//        C 玩家 hp = 3
//        D 玩家有一張閃
//        D 玩家 hp = 4
//        A 玩家有一張閃
//        A 玩家 hp = 4
        givenPlayerBHaveArrowBarrage(3);

        //When
        //B玩家出萬箭齊發
        playerPlayBPlayArrowBarrage();

        // C玩家收到要求出閃的event
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ArrowBarrage/player_b_use_ArrowBarrage_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        //C玩家出閃
        mockMvcUtil.playCard(gameId, "player-c", "player-b", "BDJ089", PlayType.INACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        //C玩家 hp = 3
        //D玩家收到要求出閃的event
        //D玩家出閃
        mockMvcUtil.playCard(gameId, "player-d", "player-b", "BDJ089", PlayType.INACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        //A 玩家收到要求出閃的event
        //A玩家出閃
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BDJ089", PlayType.INACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ArrowBarrage/player_b_use_ArrowBarrage_and_player_c_play_dodge_for_%s.json";
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
    public void testPlayerBPlayArrowBarrageAndEveryOneSkip() throws Exception {
        // Given A玩家有萬箭齊發
        // 玩家ABCD
        // B的回合
        // C 玩家沒有閃，有桃
        // C 玩家 hp = 3
        // B 玩家出萬箭齊發
        // C 玩家出skip
        // D 玩家出skip

        givenPlayerBHaveArrowBarrage(3);

        // B 出萬箭齊發
        playerPlayBPlayArrowBarrage();
        popAllPlayerMessage();

        // C 玩家出skip
        mockMvcUtil.playCard(gameId, "player-c", "", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // D玩家收到要求出閃的event
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ArrowBarrage/player_b_use_ArrowBarrage_player_c_skip_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // D 玩家出skip
        mockMvcUtil.playCard(gameId, "player-d", "", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // A 玩家出skip
        mockMvcUtil.playCard(gameId, "player-a", "", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // B 玩家出桃
        mockMvcUtil.playCard(gameId, "player-b", "player-b", BH3029.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
    }


    @Test
    public void testPlayerBPlayArrowBarrageAndPlayerCDying() throws Exception {
        // Given A玩家有萬箭齊發
        // 玩家ABCD
        // B的回合
        // C 玩家沒有閃，有桃
        // C 玩家 hp = 1
        // B玩家出萬箭齊發
        // C 玩家出skip
        // C 玩家 hp = 0 ，進入瀕臨死亡
        // C 玩家沒有桃
        // D 玩家有一張桃
        givenPlayerBHaveArrowBarrage(1);

        // B 出萬箭齊發
        playerPlayBPlayArrowBarrage();
        popAllPlayerMessage();

        // C 玩家出skip
        mockMvcUtil.playCard(gameId, "player-c", "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // C 玩家 hp = 0 瀕臨死亡
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ArrowBarrage/player_b_use_ArrowBarrage_player_c_play_skip_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // C 玩家出skip
        mockMvcUtil.playCard(gameId, "player-c", "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // D 玩家出桃
        mockMvcUtil.playCard(gameId, "player-d", "player-c", "BH3029", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        //Then
        // C玩家 hp = 1
        // D玩家收到要求出閃的event
        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ArrowBarrage/player_c_add_hp_and_player_d_accept_ask_dodge_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // D 玩家出閃
        mockMvcUtil.playCard(gameId, "player-d", "player-b", "BDJ089", PlayType.INACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // A 玩家出閃，萬箭齊發結束
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BDJ089", PlayType.INACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ArrowBarrage/player_b_play_ArrowBarrage_and_finish_all_action_for_%s.json";
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
    public void testPlayerBPlayArrowBarrageAndPlayerCDyingPlayerAPlaySkip() throws Exception {
        // Given A玩家有萬箭齊發
        // 玩家ABCD
        // B的回合
        // C 玩家沒有閃，有桃
        // C 玩家 hp = 1
        // B玩家出萬箭齊發
        // C 玩家出skip (for 萬箭齊發)
        // C 玩家 hp = 0 ，進入瀕臨死亡
        // C 玩家出skip (for 瀕臨死亡出桃)
        // D 玩家出skip (for 瀕臨死亡出桃)
        // A 玩家出skip (for 瀕臨死亡出桃)

        givenPlayerBHaveArrowBarrage(1);

        // B 出萬箭齊發
        playerPlayBPlayArrowBarrage();
        popAllPlayerMessage();

        // C 玩家出skip
        mockMvcUtil.playCard(gameId, "player-c", "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // C 玩家 hp = 0 瀕臨死亡
        popAllPlayerMessage();

        // C 玩家出skip
        mockMvcUtil.playCard(gameId, "player-c", "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // D 玩家出skip
        mockMvcUtil.playCard(gameId, "player-d", "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // When
        // A 玩家出Skip
        mockMvcUtil.playCard(gameId, "player-a", "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        mockMvcUtil.playCard(gameId, "player-b", "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        // Then
        // 不會報錯
        // C玩家死亡
    }


    private void popAllPlayerMessage() {
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");
    }


    private void playerPlayBPlayArrowBarrage() throws Exception {
        mockMvcUtil.playCard(gameId, "player-b", "", "SHK040", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
    }

    private void givenPlayerBHaveArrowBarrage(int playerCHp) {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Dodge(BDJ089), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new ArrowBarrage(SHK040)
        );
        Player playerC = createPlayer(
                "player-c",
                playerCHp,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerD = createPlayer(
                "player-d",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerB);
        repository.save(game);
    }
}

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
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
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

public class BarbarianInvasionTest extends AbstractBaseIntegrationTest {

    @Test
    public void testPlayerBPlayBarbarian() throws Exception {
//        Given A玩家有南蠻入侵
//        玩家ABCD
//        B的回合
//        C 玩家有一張殺
//        C 玩家 hp = 3
//        D 玩家有一張殺
//        D 玩家 hp = 4
//        A 玩家有一張殺
//        A 玩家 hp = 4
        givenPlayerBHaveBarbarianInvasion(3);

        //When
        //B玩家出南蠻入侵
        playerPlayBPlayBarbarian();

        // C玩家收到要求出殺的event
        String actualJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_use_barbarian_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, actualJsonForA);

        String actualJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_use_barbarian_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, actualJsonForB);

        String actualJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_use_barbarian_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, actualJsonForC);

        String actualJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_use_barbarian_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, actualJsonForD);

        //C玩家出殺
        mockMvcUtil.playCard(gameId, "player-c", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        //C玩家 hp = 3
        //D玩家收到要求出殺的event
        //D玩家出殺
        mockMvcUtil.playCard(gameId, "player-d", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        //A 玩家收到要求出殺的event
        //A玩家出殺
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_use_barbarian_and_player_c_play_kill_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            path = Paths.get(String.format(filePathTemplate, testPlayerId));
            expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }
    }

    @Test
    public void testPlayerBPlayBarbarianAndEveryOneSkip() throws Exception {
        // Given A玩家有南蠻入侵
        // 玩家ABCD
        // B的回合
        // C 玩家沒有殺，有桃
        // C 玩家 hp = 3
        // B 玩家出南蠻入侵
        // C 玩家出skip殺
        // D 玩家出skip殺

        givenPlayerBHaveBarbarianInvasion(3);

        // B 出南蠻入侵
        playerPlayBPlayBarbarian();
        popAllPlayerMessage();

        // C 玩家出skip
        mockMvcUtil.playCard(gameId, "player-c", "", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // D玩家收到要求出殺的event
        String actualJsonForA = websocketUtil.getValue("player-d");
        Path path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_use_barbarian_player_c_skip_for_player_d.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, actualJsonForA);

        popAllPlayerMessage();

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
    public void testPlayerBPlayBarbarianAndPlayerCDying() throws Exception {
        // Given A玩家有南蠻入侵
        // 玩家ABCD
        // B的回合
        // C 玩家沒有殺，有桃
        // C 玩家 hp = 1
        // B玩家出南蠻入侵
        // C 玩家出skip殺
        // C 玩家 hp = 0 ，進入瀕臨死亡
        // C 玩家沒有桃
        // D 玩家有一張桃
        givenPlayerBHaveBarbarianInvasion(1);

        // B 出南蠻入侵
        playerPlayBPlayBarbarian();
        popAllPlayerMessage();

        // C 玩家出skip
        mockMvcUtil.playCard(gameId, "player-c", "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // C 玩家 hp = 0 瀕臨死亡
        String playerCPlayKillJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_use_barbarian_player_c_play_skip_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerCPlayKillJsonForA);

        String playerCPlayKillJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_use_barbarian_player_c_play_skip_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerCPlayKillJsonForB);

        String playerCPlayKillJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_use_barbarian_player_c_play_skip_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerCPlayKillJsonForC);

        String playerCPlayKillJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_use_barbarian_player_c_play_skip_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerCPlayKillJsonForD);

        // C 玩家出skip
        mockMvcUtil.playCard(gameId, "player-c", "player-c", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // D 玩家出桃
        mockMvcUtil.playCard(gameId, "player-d", "player-c", "BH3029", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        //Then
        // C玩家 hp = 1
        // D玩家收到要求出殺的event

        String playerDPlayPeachJsonForA = websocketUtil.getValue("player-a");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_c_add_hp_and_player_d_accept_ask_kill_for_player_a.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerDPlayPeachJsonForA);

        String playerDPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_c_add_hp_and_player_d_accept_ask_kill_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerDPlayPeachJsonForB);

        String playerDPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_c_add_hp_and_player_d_accept_ask_kill_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerDPlayPeachJsonForC);

        String playerDPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_c_add_hp_and_player_d_accept_ask_kill_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerDPlayPeachJsonForD);

        // D 玩家出殺
        mockMvcUtil.playCard(gameId, "player-d", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // A 玩家出殺，南蠻入侵結束
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        playerDPlayPeachJsonForA = websocketUtil.getValue("player-a");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_play_barbarian_and_finish_all_action_for_player_a.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerDPlayPeachJsonForA);
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");
    }

    @Test
    public void testPlayerBPlayBarbarianAndPlayerCDyingPlayerAPlaySkip() throws Exception {
        // Given A玩家有南蠻入侵
        // 玩家ABCD
        // B的回合
        // C 玩家沒有殺，有桃
        // C 玩家 hp = 1
        // B玩家出南蠻入侵
        // C 玩家出skip殺 (for 南蠻入侵)
        // C 玩家 hp = 0 ，進入瀕臨死亡
        // C 玩家出skip (for 瀕臨死亡出桃)
        // D 玩家出skip (for 瀕臨死亡出桃)
        // A 玩家出skip (for 瀕臨死亡出桃)

        givenPlayerBHaveBarbarianInvasion(1);

        // B 出南蠻入侵
        playerPlayBPlayBarbarian();
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


    @Test
    public void givenPlayerABCD_PlayerAPlaysBarbarianInvasion_WhenNoOneCanDodge_ThenBAndDDieAndACWin() throws Exception {
        //        Given
        //        玩家A B C D
        //        A 為主公 B 為反賊 C 為忠臣 D 為內奸
        //        B hp = 1 C hp = 3 D hp = 1
        //        A 手牌有一張南蠻入侵
        //        B D 手牌沒有閃
        //        A B C D 都沒有桃
        givenPlayerAHaveBarbarianInvasion();

        //        When
        //        A 出 南蠻入侵
        mockMvcUtil.playCard(gameId, "player-a", "", "SS7007", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        //        B skip 南蠻入侵
        //        B C D A 被詢問要不要出桃 skip
        //        B 死亡 A 抽三張卡到手牌
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
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
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_dead_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        //        C skip 南蠻入侵
        mockMvcUtil.playCard(gameId, "player-c", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        //        D skip 南蠻入侵
        mockMvcUtil.playCard(gameId, "player-d", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        //        D A C 被詢問要不要出桃 skip
        mockMvcUtil.playCard(gameId, "player-d", "player-d", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, "player-a", "player-d", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, "player-c", "player-d", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        //        Then
        //        D 死亡
        //        A C 獲勝 (主公與忠臣獲勝)
        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_d_dead_for_%s.json";
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


    private void popAllPlayerMessage() {
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");
    }


    private void playerPlayBPlayBarbarian() throws Exception {
        mockMvcUtil.playCard(gameId, "player-b", "", "SS7007", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
    }

    private void givenPlayerBHaveBarbarianInvasion(int playerCHp) {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)
        );
        Player playerB = createPlayer("player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new BarbarianInvasion(SS7007)
        );
        Player playerC = createPlayer(
                "player-c",
                playerCHp,
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
        Game game = initGame(gameId, players, playerB);
        repository.save(game);
    }


    private void givenPlayerAHaveBarbarianInvasion() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new BarbarianInvasion(SS7007)
        );
        Player playerB = createPlayer("player-b",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)
        );
        Player playerC = createPlayer(
                "player-c",
                3,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        Player playerD = createPlayer(
                "player-d",
                1,
                General.劉備,
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

}

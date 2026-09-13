package com.gaas.threeKingdoms.e2e.scrollcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.JsonFileWriterUtil;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
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
        playerPlayArrowBarrage("player-b", "SHA040");

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
        playerPlayArrowBarrage("player-b", "SHA040");
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
        playerPlayArrowBarrage("player-b", "SHA040");
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
        playerPlayArrowBarrage("player-b", "SHA040");
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
        // Then
        // 不會報錯
        // C玩家死亡，B抽三張牌，D要求出🍑
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ArrowBarrage/player_b_use_ArrowBarrage_and_player_c_dead_for_%s.json";
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
    public void testPlayerAPlayArrowBarrageAndPlayerDIsDyingButPlayerAIsNotAskDodge() throws Exception {
        //Given
        //玩家Ａ內奸ＢＣＤ ，Ｂ反賊已死亡，C主公剩三滴血，Ｄ忠臣剩一滴血
        //A出萬箭齊發，C不出閃扣血，D不出閃扣血瀕死沒人救，此時會觸發要求A出閃
        //期望: A自己出萬箭齊發，不應被要求出閃
        givenPlayerAPlayArrowBarrageWhenPlayerBIsDeadAndPlayerCAndPlayerDNoDodge();

        // When
        //A 出萬箭齊發
        playerPlayArrowBarrage("player-a", "SHA040");
        popAllPlayerMessage();

        // C 出skip
        mockMvcUtil.playCard(gameId, "player-c", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // D 出skip
        mockMvcUtil.playCard(gameId, "player-d", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        // D dying
        mockMvcUtil.playCard(gameId, "player-d", "player-d", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        mockMvcUtil.playCard(gameId, "player-a", "player-d", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();
        popAllPlayerMessage();

        mockMvcUtil.playCard(gameId, "player-c", "player-d", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then
        // 玩家A沒有收到要求出閃的 event
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ArrowBarrage/player_a_play_ArrowBarrage_and_player_d_dead_for_%s.json";

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
    public void testPlayerBPlayArrowBarrageAndPlayerCSkipEquipment() throws Exception {
//        Given A玩家有萬箭齊發
//        玩家ABCD
//        B的回合
//        C 玩家有八卦陣

        givenPlayerBHaveArrowBarrageAndPlayerCHaveEquipment();

        // When
        // C 玩家使用萬箭齊發
        playerPlayArrowBarrage("player-b", "SHA040");

        // C 玩家詢問要不要出發動裝備效果
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ArrowBarrage/player_b_use_ArrowBarrage_and_player_c_play_equipment_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // When C 不發動裝備卡
        mockMvcUtil.useEquipment(gameId, "player-c", "player-b", "ES2015", EquipmentPlayType.SKIP)
                .andExpect(status().isOk()).andReturn();

        // C玩家收到要求出閃的event
        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ArrowBarrage/player_skip_play_equipment_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // When C 出閃
        mockMvcUtil.playCard(gameId, "player-c", "player-a", "BDJ089", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // 要求 D 玩家出閃
        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ArrowBarrage/player_c_play_dodge_after_skip_play_equipment_for_%s.json";
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
    public void testPlayerBPlayArrowBarrageAndPlayerCUseEquipmentAndFailure() throws Exception {
//        Given A玩家有萬箭齊發
//        玩家ABCD
//        B的回合
//        C 玩家有八卦陣

        givenPlayerBHaveArrowBarrageAndPlayerCHaveEquipment();

        // When
        // C 玩家使用萬箭齊發
        playerPlayArrowBarrage("player-b", "SHA040");
        // C 玩家詢問要不要出發動裝備效果
        popAllPlayerMessage();

        // When C 發動裝備卡
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        mockMvcUtil.useEquipment(gameId, "player-c", "player-b", "ES2015", EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk()).andReturn();

        // 發動失敗，要求 c 玩家出閃
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ArrowBarrage/player_use_play_equipment_for_%s.json";
        for (String testPlayerId : playerIds) {
            String testPlayerJson = "";
//            testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // When C 出閃
        mockMvcUtil.playCard(gameId, "player-c", "player-b", "BDJ089", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // 要求 D 玩家出閃
        filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ArrowBarrage/player_c_play_dodge_after_use_play_equipment_for_%s.json";
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
    public void testPlayerBPlayArrowBarrageAndPlayerCUseEquipmentAndSuccess() throws Exception {
//        Given A玩家有萬箭齊發
//        玩家ABCD
//        B的回合
//        C 玩家有八卦陣

        givenPlayerBHaveArrowBarrageAndPlayerCHaveEquipmentV2();

        // When
        // C 玩家使用萬箭齊發
        playerPlayArrowBarrage("player-b", "SHA040");
        // C 玩家詢問要不要出發動裝備效果
        popAllPlayerMessage();

        // When C 發動裝備卡
        List<String> playerIds = List.of("player-a", "player-b", "player-c", "player-d");
        mockMvcUtil.useEquipment(gameId, "player-c", "player-b", "ES2015", EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk()).andReturn();

        // 發動成功，要求 d 玩家出閃
        String filePathTemplate = "src/test/resources/TestJsonFile/ScrollTest/ArrowBarrage/player_use_play_equipment_and_success_for_%s.json";
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

    private void playerPlayArrowBarrage(String playerId, String cardId) throws Exception {
        mockMvcUtil.playCard(gameId, playerId, "", cardId, PlayType.ACTIVE.getPlayType())
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
                new Dodge(BDJ089), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new ArrowBarrage(SHA040)
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
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerAPlayArrowBarrageWhenPlayerBIsDeadAndPlayerCAndPlayerDNoDodge() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Dodge(BDJ089), new Peach(BH3029), new ArrowBarrage(SHA040)
        );
        Player playerB = createPlayer("player-b",
                0,
                General.劉備,
                HealthStatus.DEATH,
                Role.REBEL,
                new Dodge(BDJ089), new Peach(BH3029)
        );
        Player playerC = createPlayer(
                "player-c",
                3,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH
        );
        Player playerD = createPlayer(
                "player-d",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER
        );
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        game.removeDyingPlayer(playerB);
        repository.save(game);
    }

    private void givenPlayerBHaveArrowBarrageAndPlayerCHaveEquipment() {
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
                new Dodge(BDJ089), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new ArrowBarrage(SHA040)
        );
        Player playerC = createPlayer(
                "player-c",
                3,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        playerC.getEquipment().setArmor(new EightDiagramTactic(ES2015));
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
        Deck deck = new Deck();
        deck.add(List.of(new Dismantle(SS3003), new Dismantle(SS3003), new Dismantle(SS3003)));
        game.setDeck(deck);
        repository.save(game);
    }

    private void givenPlayerBHaveArrowBarrageAndPlayerCHaveEquipmentV2() {
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
                new Dodge(BDJ089), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new ArrowBarrage(SHA040)
        );
        Player playerC = createPlayer(
                "player-c",
                3,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Dodge(BDJ089), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        playerC.getEquipment().setArmor(new EightDiagramTactic(ES2015));
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
        Deck deck = new Deck();
        deck.add(List.of(new RedRabbitHorse(BH3029), new RedRabbitHorse(BH3029), new RedRabbitHorse(BH3029)));
        game.setDeck(deck);
        repository.save(game);
    }
}
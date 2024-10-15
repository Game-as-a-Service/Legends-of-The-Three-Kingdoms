package com.gaas.threeKingdoms.e2e.equipment;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.JsonFileValidateHelper;
import com.gaas.threeKingdoms.e2e.MockMvcUtil;
import com.gaas.threeKingdoms.e2e.WebsocketUtil;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.outport.GameRepository;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
public class EightDiagramTacticTest {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private MockMvc mockMvc;

    private MockMvcUtil mockMvcUtil;

    private JsonFileValidateHelper helper;

    private WebsocketUtil websocketUtil;

    @Value(value = "${local.server.port}")
    private Integer port;
    private final String gameId = "my-id";

    @BeforeEach
    public void setup() throws Exception {
        mockMvcUtil = new MockMvcUtil(mockMvc);
        websocketUtil = new WebsocketUtil(port, gameId);
        helper = new JsonFileValidateHelper(websocketUtil);
        Thread.sleep(1000);
    }

    @AfterEach
    public void deleteMockGame() {
        gameRepository.deleteById(gameId);
    }

    @Test
    public void testPlayerAPlayEightDiagramTactic() throws Exception {
        // Given A玩家有一張八卦陣
        givenPlayerAHasEightDiagramTactic();

        // When A 玩家出八卦陣
        String currentPlayer = "player-a";
        String targetPlayerId = "player-a";
        String playedCardId = "ES2015";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, "active")
                .andExpect(status().isOk()).andReturn();

        //Then A裝備有八卦陣
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_playredeightDiagramTactic_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_playredeightDiagramTactic_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_playredeightDiagramTactic_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_playredeightDiagramTactic_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);
    }

    @Test
    public void testPlayerAUseEightDiagramTacticAndEffectSuccess() throws Exception {
        // Given A玩家已裝備一張八卦陣
        givenPlayerAEquipedEightDiagramTacticAndWillSuccess();

        //B 玩家攻擊 A 玩家，A收到要不要發動裝備卡的event
        whenBKillAThenAShouldHaveEquipmentEvent();

        //全部人收到 八卦陣效果抽到赤兔馬 (♥5) 的 Event ，並且效果成功
        whenPlayerAUseEightDiagramTacticAndSuccess();

        //B 玩家在裝備諸葛連弩
        whenPlayerBEquipRepeatingCrossbow();
    }

    private void whenPlayerBEquipRepeatingCrossbow() throws Exception {
        // When B 玩家裝備諸葛連弩
        String currentPlayer = "player-b";
        String targetPlayerId = "player-b";
        String playedCardId = "ECA066";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, "active")
                .andExpect(status().isOk()).andReturn();

        //Then B裝備有諸葛連弩
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_b_use_repeating_crossbow_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_b_use_repeating_crossbow_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_b_use_repeating_crossbow_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_b_use_repeating_crossbow_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);
    }

    @Test
    public void testPlayerAUseEightDiagramTacticAndEffectFailed() throws Exception {
        // Given A玩家已裝備一張八卦陣
        givenPlayerAEquipedEightDiagramTacticAndWillfail();

        //B 玩家攻擊 A 玩家，A收到要不要發動裝備卡的event
        whenBKillAThenAShouldHaveEquipmentEvent();

        //全部人收到 八卦陣效果抽到 (黑桃7) 的 Event ，效果失敗
        whenPlayerAUseEightDiagramTacticAndFailed();

        //玩家A 出閃，血量不變
        whenPlayerAPlayDodgeAndHpKeep();

        //玩家B 出諸葛連弩
        whenPlayerBPlayRepeatingCrossbow();

    }

    @Test
    public void testPlayerANotUseEightDiagramTactic() throws Exception {
        // Given A玩家已裝備一張八卦陣
        givenPlayerAEquipedEightDiagramTacticAndWillfail();

        //B 玩家攻擊 A 玩家，A收到要不要發動裝備卡的event
        whenBKillAThenAShouldHaveEquipmentEvent();

        //A不發動效果
        whenPlayerANotUseEightDiagram();

        //玩家A 出閃，血量不變
        whenPlayerAPlayDodgeAndHpKeep();

        //玩家B 出諸葛連弩
        whenPlayerBPlayRepeatingCrossbow();

    }

    private void whenPlayerANotUseEightDiagram() throws Exception {
        // When A不發動裝備卡
        String currentPlayer = "player-a";
        String targetPlayerId = "player-a";
        String playedCardId = "ES2015";

        mockMvcUtil.useEquipment(gameId, currentPlayer, targetPlayerId, playedCardId, EquipmentPlayType.SKIP)
                .andExpect(status().isOk()).andReturn();

        //Then A不發動八卦陣的效果
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_skip_equipment_effect_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_skip_equipment_effect_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_skip_equipment_effect_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_skip_equipment_effect_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);

    }

    private void whenPlayerBPlayRepeatingCrossbow() throws Exception {
        // When A發動裝備卡
        String currentPlayer = "player-b";
        String targetPlayerId = "player-b";
        String playedCardId = "ECA066";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
    }

    private void whenBKillAThenAShouldHaveEquipmentEvent() throws Exception {
        // When B 玩家攻擊 A 玩家
        String currentPlayer = "player-b";
        String targetPlayerId = "player-a";
        String playedCardId = "BS8008";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        //Then A收到要不要發動裝備卡的event
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_receive_EightDiagramTactic_Event_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_receive_EightDiagramTactic_Event_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_receive_EightDiagramTactic_Event_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_receive_EightDiagramTactic_Event_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);
    }

    private void whenPlayerAUseEightDiagramTacticAndSuccess() throws Exception {
        // When A發動裝備卡
        String currentPlayer = "player-a";
        String targetPlayerId = "player-a";
        String playedCardId = "ES2015";

        mockMvcUtil.useEquipment(gameId, currentPlayer, targetPlayerId, playedCardId, EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk()).andReturn();

        //Then 全部人收到 八卦陣效果抽到赤兔馬 (♥5) 的 Event
        //Event 內是效果成功, A 不用出閃
        //A玩家HP=4
        //還是 B 的回合
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_use_EightDiagramTactic_Event_Success_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_use_EightDiagramTactic_Event_Success_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_use_EightDiagramTactic_Event_Success_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_use_EightDiagramTactic_Event_Success_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);
    }

    private void whenPlayerAPlayDodgeAndHpKeep() throws Exception {
        String currentPlayer = "player-a";
        String targetPlayerId = "player-b";
        String playedCardId = "BH2028";

        // When A出閃
        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, PlayType.INACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();

        //Then A血量仍然為Hp=4
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_play_dodge_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_play_dodge_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_play_dodge_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_play_dodge_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);

    }

    private void whenPlayerASkipAndHpDecrease() throws Exception {
        String currentPlayer = "player-a";
        String targetPlayerId = "";

        // When A出閃
        mockMvcUtil.playCardWithoutCardId(gameId, currentPlayer, targetPlayerId, PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        //Then A血量為Hp=3
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_skip_when_eightdiagram_fail_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_skip_when_eightdiagram_fail_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_skip_when_eightdiagram_fail_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_skip_when_eightdiagram_fail_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);

    }

    private void whenPlayerAUseEightDiagramTacticAndFailed() throws Exception {
        // When A發動裝備卡
        String currentPlayer = "player-a";
        String targetPlayerId = "player-a";
        String playedCardId = "ES2015";
        mockMvcUtil.useEquipment(gameId, currentPlayer, targetPlayerId, playedCardId, EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk()).andReturn();

        //Then 全部人收到 八卦陣效果抽到黑桃2 的 Event
        //Event 內是效果失敗, A 要出閃
        //A玩家HP=4
        //Active Player-a
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_use_EightDiagramTactic_Event_Failed_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_use_EightDiagramTactic_Event_Failed_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_use_EightDiagramTactic_Event_Failed_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/EquipmentTest/PlayEightDiagramTactic/player_a_use_EightDiagramTactic_Event_Failed_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);
    }

    private void givenPlayerAEquipedEightDiagramTacticAndWillSuccess() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        playerA.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        Player playerB = createPlayer("player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new RepeatingCrossbowCard(ECA066)
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
        Game game = initGame(gameId, players, playerB);
        Deck deck = new Deck(
                List.of(
                        new RedRabbitHorse(BH3029)
                )
        );
        game.setDeck(deck);
        gameRepository.save(game);
    }

    private void givenPlayerAEquipedEightDiagramTacticAndWillfail() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );
        playerA.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        Player playerB = createPlayer("player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new RepeatingCrossbowCard(ECA066)
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
        Game game = initGame(gameId, players, playerB);
        Deck deck = new Deck(
                List.of(
                        new Kill(BS7020)
                )
        );
        game.setDeck(deck);
        gameRepository.save(game);
    }

    private void givenPlayerAHasEightDiagramTactic() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new EightDiagramTactic(ES2015)
        );

        Player playerB = createPlayer("player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)
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
        gameRepository.save(game);
    }
}

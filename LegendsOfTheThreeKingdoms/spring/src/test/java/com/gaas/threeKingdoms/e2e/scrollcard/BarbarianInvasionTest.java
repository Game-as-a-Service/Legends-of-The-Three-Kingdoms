package com.gaas.threeKingdoms.e2e.scrollcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.JsonFileValidateHelper;
import com.gaas.threeKingdoms.e2e.MockMvcUtil;
import com.gaas.threeKingdoms.e2e.WebsocketUtil;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.outport.GameRepository;
import com.gaas.threeKingdoms.player.Equipment;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static com.gaas.threeKingdoms.handcard.PlayCard.BHK039;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
public class BarbarianInvasionTest {

    @MockBean
    private GameRepository repository;

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

    @Test
    public void testPlayerBPlayBarbarian() throws Exception {
        // Given A玩家有南蠻入侵
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
        actualJsonForA = websocketUtil.getValue("player-a");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_use_barbarian_and_player_c_play_kill_for_player_a.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, actualJsonForA);

        actualJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_use_barbarian_and_player_c_play_kill_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, actualJsonForB);

        actualJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_use_barbarian_and_player_c_play_kill_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, actualJsonForC);

        actualJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/ScrollTest/Barbarianinvasion/player_b_use_barbarian_and_player_c_play_kill_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, actualJsonForD);

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
        Mockito.when(repository.findById(gameId)).thenReturn(game);
    }


}

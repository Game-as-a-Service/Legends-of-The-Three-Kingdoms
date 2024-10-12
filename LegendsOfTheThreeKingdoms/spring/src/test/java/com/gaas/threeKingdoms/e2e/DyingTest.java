package com.gaas.threeKingdoms.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.exception.NotFoundException;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.outport.GameRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
public class DyingTest {

    @MockBean
    private GameRepository repository;

    private WebsocketUtil websocketUtil;

    @Autowired
    private MockMvc mockMvc;

    private MockMvcUtil mockMvcUtil;

    @Value(value = "${local.server.port}")
    private Integer port;

    @Autowired
    private ObjectMapper objectMapper;

    private final String gameId = "dyingTestGame";


    @BeforeEach
    public void setup() throws Exception {
        websocketUtil = new WebsocketUtil(port, gameId);
        mockMvcUtil = new MockMvcUtil(mockMvc);
        Thread.sleep(1000);
    }


    @Test
    public void testPlayerAIsEnterDyingStatus() throws Exception {
        givenPlayerAIsEnterDyingStatus();
        //Given A玩家瀕臨死亡
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        //When A玩家出桃
        String currentPlayer = "player-a";
        String targetPlayerId = "player-a";
        String playedCardId = "BH3029";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, "inactive")
                .andExpect(status().isOk()).andReturn();

        //Then A玩家還有一滴血
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndPlayerPeach/player_a_playpeach_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndPlayerPeach/player_a_playpeach_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndPlayerPeach/player_a_playpeach_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndPlayerPeach/player_a_playpeach_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);

    }

    @Test
    public void testPlayerAIsEnterDyingStatusAndNoPlayPeach() throws Exception {
        givenPlayerAIsEnterDyingStatus();
        //Given A玩家瀕臨死亡
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        //When A玩家出skip
        String currentPlayer = "player-a";
        String targetPlayerId = "player-a";
        String playedCardId = "";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        String playerASkipJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndSkipPeach/player_a_skip_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerASkipJsonForA);
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");

        //When B玩家出skip
        currentPlayer = "player-b";
        targetPlayerId = "player-a";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        playerASkipJsonForA = websocketUtil.getValue("player-a");
        path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndSkipPeach/player_b_skip_for_player_a.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerASkipJsonForA);
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");


        //When C玩家出skip
        currentPlayer = "player-c";
        targetPlayerId = "player-a";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        playerASkipJsonForA = websocketUtil.getValue("player-a");
        path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndSkipPeach/player_c_skip_for_player_a.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerASkipJsonForA);
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");

        currentPlayer = "player-d";
        targetPlayerId = "player-a";
        //When d玩家出skip
        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        playerASkipJsonForA = websocketUtil.getValue("player-a");
        path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndSkipPeach/player_d_skip_for_player_a.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerASkipJsonForA);
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");
    }

    @Test
    public void testPlayerBIsEnterDyingStatusAndNoPlayPeach() throws Exception {
        givenPlayerBIsEnterDyingStatus();
        //Given B玩家瀕臨死亡
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found"));

        //When B玩家出skip
        String currentPlayer = "player-b";
        String targetPlayerId = "player-b";
        String playedCardId = "";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");

        //When C玩家出skip
        currentPlayer = "player-c";
        targetPlayerId = "player-b";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");


        //When D玩家出skip
        currentPlayer = "player-d";
        targetPlayerId = "player-b";

        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");

        currentPlayer = "player-a";
        targetPlayerId = "player-b";
        //When A玩家出skip
        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, PlayType.SKIP.getPlayType())
                .andExpect(status().isOk()).andReturn();

        // Then B 玩家死亡，active player 是 C
        List<Player> players = game.getPlayers();
        String filePathTemplate = "src/test/resources/TestJsonFile/DyingTest/PlayerBDyingAndSkipPeach/player_a_skip_for_%s.json";
        for (Player player : players) {
            String testPlayerId = player.getId();
            String testPlayerJson = "";
            //testPlayerJson = JsonFileWriterUtil.writeJsonToFile(websocketUtil, testPlayerId, filePathTemplate);
            testPlayerJson = websocketUtil.getValue(testPlayerId);
            testPlayerId = testPlayerId.replace("-", "_");
            Path path = Paths.get(String.format(filePathTemplate, testPlayerId));
            String expectedJson = Files.readString(path);
            assertEquals(expectedJson, testPlayerJson);
        }

        // Player C 裝備諸葛連弩
        currentPlayer = "player-c";
        targetPlayerId = "player-c";
        playedCardId = "ECA066";
        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        String testPlayerJson = websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");

        // Player C finishAction
        currentPlayer = "player-c";
        mockMvcUtil.finishAction(gameId, currentPlayer);
        String testPlayerJson2 = websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");

        // Player D finishAction
        currentPlayer = "player-d";
        mockMvcUtil.finishAction(gameId, currentPlayer);
        String testPlayerJson3 = websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");

        // Player A finishAction
        currentPlayer = "player-a";
        mockMvcUtil.finishAction(gameId, currentPlayer);
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");

        // Player B finishAction
        currentPlayer = "player-b";
        mockMvcUtil.finishAction(gameId, currentPlayer);
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");

        // Player C finishAction
        currentPlayer = "player-c";
        mockMvcUtil.finishAction(gameId, currentPlayer);
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");
    }

    private void givenPlayerAIsEnterDyingStatus() {
        Player playerA = createPlayer(
                "player-a",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
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
        Game game = initGame(gameId, players, playerB);

        // B對A出殺
        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");

        // A玩家出skip
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");

        repository.save(game);
    }

    private void givenPlayerBIsEnterDyingStatus() {
        Player playerA = createPlayer(
                "player-a",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Player playerB = createPlayer("player-b",
                1,
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
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new RepeatingCrossbowCard(ECA066)
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
        Game game = initGame(gameId, players, playerC);

        // C對B出殺
        game.playerPlayCard(playerC.getId(), "BS8008", playerB.getId(), "active");
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");

        // B玩家出skip
        game.playerPlayCard(playerB.getId(), "", playerC.getId(), "skip");
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");

        repository.save(game);
    }

}

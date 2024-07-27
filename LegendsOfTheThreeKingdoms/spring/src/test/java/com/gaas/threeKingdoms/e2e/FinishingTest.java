package com.gaas.threeKingdoms.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
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
import static com.gaas.threeKingdoms.handcard.PlayCard.BHK039;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
public class FinishingTest {


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
        Game game = repository.findById(gameId);

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

        // A玩家出skip
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");
        Mockito.when(repository.findById(gameId)).thenReturn(game);
    }

}

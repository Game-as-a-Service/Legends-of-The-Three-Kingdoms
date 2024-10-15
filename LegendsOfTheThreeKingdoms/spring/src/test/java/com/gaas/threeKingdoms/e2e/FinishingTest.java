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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
public class FinishingTest {


    @Autowired
    private GameRepository gameRepository;

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

    @AfterEach
    public void deleteMockGame() {
        gameRepository.deleteById(gameId);
    }

    @Test
    public void testPlayerAIsCurrentRoundPlayerAndFinishActionBeforePlayerBSkip() throws Exception {
        //Given A 是當前回合玩家
        givenPlayerAIsCurrentRoundPlayer();

        // A 對 B 出殺
        String currentPlayer = "player-a";
        String targetPlayerId = "player-b";
        String playedCardId = "BS8008";
        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, "active");

        // A玩家結束回合，不等 B skip
        mockMvcUtil.finishAction(gameId, currentPlayer)
                .andExpect(status().is4xxClientError()).andReturn();
    }

    private void givenPlayerAIsCurrentRoundPlayer() {
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
        Game game = initGame(gameId, players, playerA);

        gameRepository.save(game);
    }

}

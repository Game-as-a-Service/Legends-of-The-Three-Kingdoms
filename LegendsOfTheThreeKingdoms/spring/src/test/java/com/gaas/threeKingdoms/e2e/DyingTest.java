package com.gaas.threeKingdoms.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.Round;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.BloodCard;
import com.gaas.threeKingdoms.player.Hand;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.repository.InMemoryGameRepository;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class DyingTest {

//    @Mock
//    private final InMemoryGameRepository repository = Mockito.mock(InMemoryGameRepository.class);

    @Autowired
    private InMemoryGameRepository repository;

    private WebsocketUtil websocketUtil;

    @Autowired
    private MockMvc mockMvc;

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    public void setup() throws Exception {
        websocketUtil = new WebsocketUtil(port);
        String gameId = "my-id";
        givenPlayerAIsEnterDyingStatus(gameId);
    }

    @Test
    public void testMockGame() throws Exception {
        Game game = repository.findById("my-id");
        assertNotNull(game);
    }

    @Test
    public void testPlayerAIsEnterDyingStatus() throws Exception {
        //Given A玩家瀕臨死亡
        Game game = repository.findById("my-id");

        //When A玩家出桃
        String currentPlayer = "player-a";
        String targetPlayerId = "player-a";
        String playedCardId = "BH3029";

        playCard(currentPlayer, targetPlayerId, playedCardId, "active")
                .andExpect(status().isOk()).andReturn();

        //Then A玩家還有一滴血
        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/PlayCard/round_playcard_player_b_skip_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/PlayCard/round_playcard_player_b_skip_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/PlayCard/round_playcard_player_b_skip_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/PlayCard/round_playcard_player_b_skip_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);
    }


    private void givenPlayerAIsEnterDyingStatus(String gameId) {
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();


        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = new Game(gameId, players);
        game.setCurrentRound(new Round(playerB));
        game.enterPhase(new Normal(game));

        // B對A出殺
        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");

        // A玩家出skip
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");
        repository.save(game);
//        Mockito.when(repository.findById(gameId)).thenReturn(game);
    }

    private ResultActions playCard(String currentPlayerId, String targetPlayerId, String cardId, String playType) throws Exception {
        return this.mockMvc.perform(post("/api/games/my-id/player:playCard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        { "playerId": "%s",
                          "targetPlayerId": "%s",
                          "cardId": "%s",
                          "playType": "%s"
                        }""", currentPlayerId, targetPlayerId, cardId, playType)));
    }

}

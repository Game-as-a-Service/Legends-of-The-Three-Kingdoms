package com.gaas.threeKingdoms.e2e;

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

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext
@AutoConfigureMockMvc
public class PlayPeachCardTest {

    @MockBean
    private GameRepository repository;

    @Autowired
    private MockMvc mockMvc;

    private MockMvcUtil mockMvcUtil;

    private JsonFileValidateHelper helper;

    private WebsocketUtil websocketUtil;

    @Value(value = "${local.server.port}")
    private int port = 1001;
    private final String gameId = "my-id";

    @BeforeEach
    public void setup() throws Exception {
        mockMvcUtil = new MockMvcUtil(mockMvc);
        websocketUtil = new WebsocketUtil(port, gameId);
        helper = new JsonFileValidateHelper(websocketUtil);
    }

    @Test
    public void testPlayerAPlayPeachCard() throws Exception {
        setup();
        //Given A玩家hp為3
        givenPlayerAPlayCardStatus();

        //When A玩家出桃
        mockMvcUtil.playCard(gameId, "player-a", "player-a", "BH3029", "active")
                .andExpect(status().isOk());

        //Then A玩家hp為4
        helper.assertThat("player-a").getJsonIsEqualToFile("PlayPeachCardTest/PlayerPlayPeachCard/player_a_playpeach_for_player_a.json");
        helper.assertThat("player-b").getJsonIsEqualToFile("PlayPeachCardTest/PlayerPlayPeachCard/player_a_playpeach_for_player_b.json");
        helper.assertThat("player-c").getJsonIsEqualToFile("PlayPeachCardTest/PlayerPlayPeachCard/player_a_playpeach_for_player_c.json");
        helper.assertThat("player-d").getJsonIsEqualToFile("PlayPeachCardTest/PlayerPlayPeachCard/player_a_playpeach_for_player_d.json");
    }


    private void givenPlayerAPlayCardStatus() {
        Player playerA = createPlayer(
                "player-a",
                4,
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

        playerA.damage(1);
        Mockito.when(repository.findById(gameId)).thenReturn(initGame(gameId, playerA, playerB, playerC, playerD));
    }


}

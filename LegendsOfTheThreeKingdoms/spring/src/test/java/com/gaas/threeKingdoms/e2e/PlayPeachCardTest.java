package com.gaas.threeKingdoms.e2e;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PlayPeachCardTest extends AbstractBaseIntegrationTest {

    @Test
    public void testPlayerAPlayPeachCard() throws Exception {
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
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        repository.save(game);
    }


}

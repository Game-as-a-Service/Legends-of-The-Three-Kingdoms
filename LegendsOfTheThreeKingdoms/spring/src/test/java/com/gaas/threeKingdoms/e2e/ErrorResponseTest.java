package com.gaas.threeKingdoms.e2e;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.Snatch;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 錯誤回應格式（issue #200）— body 必須是精簡 JSON {error, message}，不得洩漏 stack trace。
 */
public class ErrorResponseTest extends AbstractBaseIntegrationTest {

    private void givenFourPlayerGame() {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Snatch(SS3016), new Kill(BS8008));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER,
                new Kill(BS9009));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS0010));
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        repository.save(game);
    }

    @Test
    public void testOutOfRangeSnatch_Returns400WithConciseJsonBody() throws Exception {
        // 復刻 issue #200 HAR 情境：對距離 2 的玩家出順手牽羊
        givenFourPlayerGame();

        MvcResult result = mockMvcUtil.playCard(gameId, "player-a", "player-c", SS3016.getCardId(),
                        PlayType.ACTIVE.getPlayType())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("DistanceErrorException"))
                .andExpect(jsonPath("$.message").value("Players are not within range."))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertFalse(body.contains("at com.gaas"), "不得洩漏 stack trace");
        assertTrue(body.length() < 200, "body 應精簡（實際 " + body.length() + " 字元）");
    }

    @Test
    public void testWrongPlayerResponds_Returns400WithNonEmptyMessage() throws Exception {
        // A 殺 B，但 C（非 reactionPlayers）搶著回應
        givenFourPlayerGame();

        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        MvcResult result = mockMvcUtil.playCard(gameId, "player-c", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("IllegalStateException"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertTrue(body.contains("player-c"), "message 應指出是哪個玩家的問題");
        assertFalse(body.contains("at com.gaas"), "不得洩漏 stack trace");
    }
}

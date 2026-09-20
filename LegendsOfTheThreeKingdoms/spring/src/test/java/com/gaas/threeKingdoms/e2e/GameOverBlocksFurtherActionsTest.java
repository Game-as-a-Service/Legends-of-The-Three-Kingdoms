package com.gaas.threeKingdoms.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.SomethingForNothing;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 使用者回報：遊戲結束後還可以繼續出牌。
 *
 * <p>主公死亡、GameOverEvent 廣播之後，這裡逐一打回真正的 HTTP endpoint：出牌 / 結束回合 /
 * 棄牌 / 武將技都必須被拒絕（4xx，訊息寫「遊戲已結束」），而且 MongoDB 裡的狀態不能被改動。
 * GameOver phase 有存進 MongoDB，所以每個 request reload 後都還看得到遊戲已結束。
 */
public class GameOverBlocksFurtherActionsTest extends AbstractBaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * a=反賊(回合玩家，還有無中生有)、b=主公(1 HP)、c=忠臣、d=內奸；a 殺 b、全員不出桃 → 反賊獲勝。
     *
     * @param dropEarlierPushes true 時把結束前每一步的推播清掉，讓最後一批就是遊戲結束那則
     */
    private void givenGameOverByMonarchDeath(boolean dropEarlierPushes) throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS8008), new SomethingForNothing(SH7046));
        // 主公用黃蓋：苦肉是主動技，不會插進殺／瀕死流程
        Player playerB = createPlayer("player-b", 1, General.黃蓋, HealthStatus.ALIVE, Role.MONARCH);
        Player playerC = createPlayer("player-c", 4, General.趙雲, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        game.setDeck(new Deck());
        game.getDeck().add(List.of(new Kill(BS9009)));
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        dropPush(dropEarlierPushes);
        mockMvcUtil.playCard(gameId, "player-b", "player-b", "", "skip").andExpect(status().isOk()); // 不出閃
        dropPush(dropEarlierPushes);
        // 依序詢問桃，全部 skip；最後一位（a）回應完才結束遊戲，所以只清到倒數第二步為止
        List<String> peachAsked = List.of("player-b", "player-c", "player-d", "player-a");
        for (String playerId : peachAsked) {
            mockMvcUtil.playCard(gameId, playerId, "player-b", "", "skip").andExpect(status().isOk());
            if (!playerId.equals("player-a")) {
                dropPush(dropEarlierPushes);
            }
        }

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals("GameOver", saved.getGamePhase().getPhaseName(), "fixture 應該已經結束遊戲");
    }

    /** 清掉這一步的推播（每人一則），讓最後留在佇列裡的是遊戲結束那一批。 */
    private void dropPush(boolean enabled) {
        if (enabled) {
            websocketUtil.popAllPlayerMessage();
        }
    }

    /** 遊戲結束那一批推播裡，每位玩家都該看到 GameOverEvent。 */
    private void assertGameOverWasBroadcast() throws Exception {
        for (String playerId : List.of("player-a", "player-b", "player-c", "player-d")) {
            JsonNode push = objectMapper.readTree(websocketUtil.getValue(playerId));
            JsonNode gameOver = null;
            for (JsonNode event : push.get("events")) {
                if ("GameOverEvent".equals(event.get("event").asText())) {
                    gameOver = event;
                }
            }
            assertNotNull(gameOver, playerId + " 應收到 GameOverEvent");
            assertEquals("player-a",
                    gameOver.get("data").get("winners").get(0).asText(), "反賊獲勝");
        }
    }

    private void assertRejectedBecauseGameIsOver(MvcResult result) throws Exception {
        assertTrue(result.getResponse().getStatus() >= 400 && result.getResponse().getStatus() < 500,
                "遊戲結束後的操作應該被拒絕，實際 status：" + result.getResponse().getStatus());
        // MockMvc 預設用 ISO-8859-1 解 body，中文訊息要指定 UTF-8 才讀得回來
        String body = result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(body.contains("遊戲已結束"), "錯誤訊息要說明原因，實際 body：" + body);
    }

    @DisplayName("遊戲結束後：出牌 / 結束回合 / 棄牌 / 武將技全被拒絕，狀態不變")
    @Test
    public void afterGameOver_everyPlayerActionEndpointIsRejected() throws Exception {
        givenGameOverByMonarchDeath(false);

        Game before = repository.findById(gameId).orElseThrow();
        int handSizeBefore = before.getPlayer("player-a").getHandSize();
        String roundPlayerBefore = before.getCurrentRoundPlayer().getId();

        // 出牌（無中生有：不受出殺次數限制，遊戲沒結束的話這張是打得出去的）
        assertRejectedBecauseGameIsOver(mockMvcUtil
                .playCard(gameId, "player-a", "player-a", "SH7046", PlayType.ACTIVE.getPlayType())
                .andReturn());
        // 結束回合（原本會直接開始下一位玩家的回合）
        assertRejectedBecauseGameIsOver(mockMvcUtil.finishAction(gameId, "player-a").andReturn());
        // 棄牌
        assertRejectedBecauseGameIsOver(mockMvc.perform(
                post("/api/games/" + gameId + "/player:discardCards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"SH7046\"]")).andReturn());
        // 武將技（孫權制衡）
        assertRejectedBecauseGameIsOver(mockMvcUtil
                .useSkillEffect(gameId, "player-d", "制衡", "ACCEPT", List.of("SH7046"), null)
                .andReturn());

        Game after = repository.findById(gameId).orElseThrow();
        assertEquals("GameOver", after.getGamePhase().getPhaseName());
        assertEquals(handSizeBefore, after.getPlayer("player-a").getHandSize(), "手牌不能被動到");
        assertEquals(roundPlayerBefore, after.getCurrentRoundPlayer().getId(), "回合不能往下走");
        assertTrue(after.getPlayer("player-a").getEquipment().getWeapon() == null);
    }

    @DisplayName("遊戲結束的那一刻：GameOverEvent 有廣播給全員（前端該據此收掉操作介面）")
    @Test
    public void gameOverIsBroadcastToEveryone() throws Exception {
        givenGameOverByMonarchDeath(true);

        assertGameOverWasBroadcast();
    }
}

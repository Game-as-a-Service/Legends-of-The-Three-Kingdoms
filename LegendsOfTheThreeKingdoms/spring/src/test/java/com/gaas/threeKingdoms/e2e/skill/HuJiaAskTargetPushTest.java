package com.gaas.threeKingdoms.e2e.skill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 使用者提問：「有沒有路徑，曹操的護駕會問自己要不要出閃？」
 *
 * <p>domain 層由 {@code HuJiaNeverAsksCaoCaoTest} 驗；這裡驗**前端真正收到的推播**：
 * 整條代閃輪詢跨 request 走完（每步都經 MongoDB 存回讀出），每一則
 * {@code AskHuJiaEffectEvent} 的 {@code data.playerId} 都不是曹操。
 *
 * <p>同時把曹操**確實**會收到的兩種詢問釘住，因為這兩者容易被誤認成「問曹操要不要代自己出閃」：
 * <ol>
 *   <li>{@code AskSkillEffectEvent}(skillName=護駕、data.playerId=曹操) —— 問曹操要不要發動護駕（issue #217）</li>
 *   <li>全部魏將拒絕後的 {@code AskDodgeEvent}(曹操) —— 回到曹操自己出閃</li>
 * </ol>
 *
 * <p>5 人座位：a(甘寧,反賊) → b(曹操,主公) → c(夏侯惇,魏) → d(張遼,魏) → e(許褚,魏)
 */
public class HuJiaAskTargetPushTest extends AbstractBaseIntegrationTest {

    private static final String CAO_CAO = "player-b";
    private static final List<String> ALL_PLAYERS =
            List.of("player-a", "player-b", "player-c", "player-d", "player-e");

    private final ObjectMapper objectMapper = new ObjectMapper();

    private void givenFivePlayerGameWithThreeWeiHelpers() {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS8008));
        Player playerB = createPlayer(CAO_CAO, 4, General.曹操, HealthStatus.ALIVE, Role.MONARCH);
        Player playerC = createPlayer("player-c", 4, General.夏侯惇, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.張遼, HealthStatus.ALIVE, Role.MINISTER);
        Player playerE = createPlayer("player-e", 4, General.許褚, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD, playerE), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH3029)));
        game.setDeck(deck);
        repository.save(game);
    }

    /** 每位玩家各取一則推播（getValue 是消費性的，一次 action 只能取一輪）。 */
    private List<JsonNode> popOnePushPerPlayer() throws Exception {
        List<JsonNode> pushes = new ArrayList<>();
        for (String playerId : ALL_PLAYERS) {
            String message = websocketUtil.getValue(playerId);
            assertNotNull(message, playerId + " 沒有收到推播");
            pushes.add(objectMapper.readTree(message));
        }
        return pushes;
    }

    private List<JsonNode> dataOf(List<JsonNode> pushes, String eventName) {
        List<JsonNode> result = new ArrayList<>();
        for (JsonNode push : pushes) {
            for (JsonNode event : push.get("events")) {
                if (eventName.equals(event.get("event").asText())) {
                    result.add(event.get("data"));
                }
            }
        }
        return result;
    }

    /** 這批推播裡被問「要不要代主公出閃」的人（每位玩家都收到同一則，去重後回傳）。 */
    private List<String> huJiaAskedPlayerIds(List<JsonNode> pushes) {
        List<String> asked = new ArrayList<>();
        for (JsonNode data : dataOf(pushes, "AskHuJiaEffectEvent")) {
            String playerId = data.get("playerId").asText();
            assertNotEquals(CAO_CAO, playerId,
                    "曹操不可以被問「要不要代主公出閃」（caoCaoPlayerId="
                            + data.get("caoCaoPlayerId").asText() + "）");
            assertEquals(CAO_CAO, data.get("caoCaoPlayerId").asText(), "被代替的一定是曹操");
            if (!asked.contains(playerId)) {
                asked.add(playerId);
            }
        }
        return asked;
    }

    @DisplayName("整條代閃輪詢的推播：問的是 c → d → e，從頭到尾沒有問曹操自己")
    @Test
    public void huJiaPollingPushes_neverAskCaoCaoToDodgeForHimself() throws Exception {
        givenFivePlayerGameWithThreeWeiHelpers();

        // A 出殺打曹操 → 只有「是否發動護駕」的詢問，被問的是曹操（issue #217），還沒有代閃詢問
        mockMvcUtil.playCard(gameId, "player-a", CAO_CAO, "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        List<JsonNode> killPushes = popOnePushPerPlayer();
        List<JsonNode> activationAsks = dataOf(killPushes, "AskSkillEffectEvent");
        assertEquals(ALL_PLAYERS.size(), activationAsks.size(), "發動詢問廣播給全員");
        for (JsonNode data : activationAsks) {
            assertEquals("護駕", data.get("skillName").asText());
            assertEquals(CAO_CAO, data.get("playerId").asText(), "「是否發動護駕」問的本來就是曹操");
        }
        assertTrue(dataOf(killPushes, "AskHuJiaEffectEvent").isEmpty(), "曹操還沒 ACCEPT，不該有代閃詢問");

        // 曹操 ACCEPT → 代閃詢問從下家算起的第一位魏將 c 開始
        mockMvcUtil.useSkillEffect(gameId, CAO_CAO, "護駕", "ACCEPT", null, null)
                .andExpect(status().isOk());
        List<String> asked = new ArrayList<>(huJiaAskedPlayerIds(popOnePushPerPlayer()));
        assertEquals(List.of("player-c"), asked);

        // c、d 依序拒絕 → 依座位順序往下問，永遠不會回頭問曹操
        for (String decliner : List.of("player-c", "player-d")) {
            mockMvcUtil.useHuJiaEffect(gameId, decliner, "DECLINE", null).andExpect(status().isOk());
            asked.addAll(huJiaAskedPlayerIds(popOnePushPerPlayer()));
        }
        assertEquals(List.of("player-c", "player-d", "player-e"), asked, "代閃只問這三位、各一次");

        // e 是最後一位：拒絕後 fallback 回曹操自己出閃（AskDodgeEvent），不是再問一輪代閃
        mockMvcUtil.useHuJiaEffect(gameId, "player-e", "DECLINE", null).andExpect(status().isOk());
        List<JsonNode> lastPushes = popOnePushPerPlayer();
        assertTrue(dataOf(lastPushes, "AskHuJiaEffectEvent").isEmpty(), "最後一位拒絕後不會再有代閃詢問");
        List<JsonNode> dodgeAsks = dataOf(lastPushes, "AskDodgeEvent");
        assertEquals(ALL_PLAYERS.size(), dodgeAsks.size(), "改成問曹操自己出閃，廣播給全員");
        for (JsonNode data : dodgeAsks) {
            assertEquals(CAO_CAO, data.get("playerId").asText());
        }

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(CAO_CAO, saved.getCurrentRound().getActivePlayer().getId(), "輪到曹操自己回應");
        assertEquals(4, saved.getPlayer(CAO_CAO).getHP(), "還沒結算傷害");
    }

    @DisplayName("代閃名單存進 MongoDB 再讀出來也不會多出曹操")
    @Test
    public void weiOrderSurvivesReloadWithoutAddingCaoCao() throws Exception {
        givenFivePlayerGameWithThreeWeiHelpers();

        mockMvcUtil.playCard(gameId, "player-a", CAO_CAO, "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.useSkillEffect(gameId, CAO_CAO, "護駕", "ACCEPT", null, null)
                .andExpect(status().isOk());

        Game reloaded = repository.findById(gameId).orElseThrow();
        var waiting = (com.gaas.threeKingdoms.behavior.behavior.WaitingHuJiaResponseBehavior)
                reloaded.peekTopBehavior();
        assertEquals(List.of("player-c", "player-d", "player-e"), waiting.getWeiOrder(),
                "reload 後的代閃名單仍是其他三位魏將");
        assertFalse(waiting.getWeiOrder().contains(CAO_CAO));
        assertEquals("player-c", waiting.getCurrentWei());
        assertEquals("player-c", reloaded.getCurrentRound().getActivePlayer().getId());
    }
}

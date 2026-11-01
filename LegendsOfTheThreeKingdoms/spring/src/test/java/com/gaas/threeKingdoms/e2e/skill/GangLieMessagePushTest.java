package com.gaas.threeKingdoms.e2e.skill;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * issue #235（前端回報）：SkillEffectEvent 的 message 固定為「武將技發動結果」，
 * 具體結算文字（如「剛烈判定紅桃，未生效」）只在外層 push message，前端顯示不到。
 * 驗證 websocket 推播中事件層 message 帶 domain event 的具體結算訊息。
 */
public class GangLieMessagePushTest extends AbstractBaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("剛烈判定紅桃 → 推播的 SkillEffectEvent.message 寫出判定牌與未生效")
    @Test
    public void gangLieHeartJudgement_pushCarriesSpecificMessage() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        Player playerB = createPlayer("player-b", 4, General.夏侯惇, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH3029))); // 剛烈判定牌：紅心 → 不生效
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        websocketUtil.popAllPlayerMessage(); // popAllPlayerMessage 每人只 pop 一則，逐 request 清
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk());
        websocketUtil.popAllPlayerMessage();

        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-b",
                                  "skillName": "剛烈",
                                  "choice": "ACCEPT"
                                }"""))
                .andExpect(status().isOk());

        for (String playerId : List.of("player-a", "player-b", "player-c", "player-d")) {
            JsonNode push = objectMapper.readTree(websocketUtil.getValue(playerId));
            String skillEffectMessage = null;
            for (JsonNode e : push.get("events")) {
                if ("SkillEffectEvent".equals(e.get("event").asText())
                        && "剛烈".equals(e.get("data").get("skillName").asText())) {
                    skillEffectMessage = e.get("message").asText();
                    assertFalse(e.get("data").get("accepted").asBoolean(), "判定紅桃 → accepted=false");
                }
            }
            assertEquals("剛烈判定：紅心3 桃 → 紅桃，未生效", skillEffectMessage,
                    playerId + " 的事件層 message 應為具體結算文字，且寫出是哪張判定牌（使用者回報）");
        }
    }

    /**
     * 使用者回報：「夏侯惇技能判定通過，系統問司馬懿要不要觸發剛烈（隨後卡局）」。
     * <p>
     * 第二段問的是傷害來源要棄兩張手牌還是受 1 點傷害，但推播裡的詢問事件跟一般
     * 「是否發動武將技」完全一樣（沒有 options、message 又被 presenter 寫死），
     * 前端只能畫出發動／放棄 → 送回來的 choice 一律被拒，遊戲停在這裡。
     * 這是推播層才看得出來的問題，domain 測試擋不住。
     */
    @DisplayName("使用者回報：問來源的推播要帶 DISCARD/DAMAGE options 與真正的問題，且放棄也能收束")
    @Test
    public void gangLieSourceAsk_pushCarriesOptionsAndQuestion() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Peach(BH4030), new Peach(BH7033));
        Player playerB = createPlayer("player-b", 4, General.夏侯惇, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS9009))); // 剛烈判定牌：黑桃 → 生效
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        websocketUtil.popAllPlayerMessage();
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk());
        websocketUtil.popAllPlayerMessage();

        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-b",
                                  "skillName": "剛烈",
                                  "choice": "ACCEPT"
                                }"""))
                .andExpect(status().isOk());

        for (String playerId : List.of("player-a", "player-b", "player-c", "player-d")) {
            JsonNode push = objectMapper.readTree(websocketUtil.getValue(playerId));
            JsonNode ask = null;
            String judgementMessage = null;
            for (JsonNode e : push.get("events")) {
                if ("AskSkillEffectEvent".equals(e.get("event").asText())
                        && "剛烈".equals(e.get("data").get("skillName").asText())) {
                    ask = e;
                } else if ("SkillEffectEvent".equals(e.get("event").asText())
                        && "剛烈".equals(e.get("data").get("skillName").asText())) {
                    judgementMessage = e.get("message").asText();
                }
            }
            assertEquals("剛烈判定：黑桃9 殺 → 非紅桃，生效", judgementMessage, playerId + " 看不到判定牌");
            assertNotNull(ask, playerId + " 應收到問來源的詢問事件");
            assertEquals("player-a", ask.get("data").get("playerId").asText(), "被問的是傷害來源");
            List<String> options = objectMapper.convertValue(
                    ask.get("data").get("options"), new TypeReference<>() {});
            assertEquals(List.of("DISCARD", "DAMAGE"), options,
                    "前端要靠 options 才畫得出正確按鈕；只有發動／放棄的話玩家回不了合法答案");
            assertEquals("剛烈判定生效：請 player-a 選擇棄兩張手牌或受 1 點傷害",
                    ask.get("message").asText(), "不可再顯示成「詢問是否發動武將技」");
        }
        websocketUtil.popAllPlayerMessage();

        // 前端目前只送得出 SKIP：不能再丟例外把遊戲鎖住，要當成「不棄牌 → 受 1 點傷害」
        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-a",
                                  "skillName": "剛烈",
                                  "choice": "SKIP"
                                }"""))
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(3, saved.getPlayer("player-a").getHP(), "沒棄牌 → 來源受剛烈 1 點傷害");
        assertTrue(saved.getTopBehavior().isEmpty(), "剛烈結束，回合要走得下去");
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
    }
}

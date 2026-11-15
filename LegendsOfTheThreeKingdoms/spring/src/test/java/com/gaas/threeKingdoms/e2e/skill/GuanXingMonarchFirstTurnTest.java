package com.gaas.threeKingdoms.e2e.skill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.round.RoundPhase;
import com.gaas.threeKingdoms.utils.ShuffleWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 主公選諸葛亮：最後一位選完將即進入主公第一回合，回合開始就要詢問觀星。
 * 開局推播（RoundStartPresenter）不可因為尚未摸牌（沒有 DrawCardEvent）而失敗，
 * 且諸葛亮必須收到 AskSkillEffectEvent(觀星)。
 */
public class GuanXingMonarchFirstTurnTest extends AbstractBaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("主公諸葛亮開局：選將最後一步成功，推播含觀星詢問；SKIP 後照常摸牌進出牌")
    @Test
    public void monarchZhugeLiangFirstTurnAsksGuanXing() throws Exception {
        try (MockedStatic<ShuffleWrapper> mocked = Mockito.mockStatic(ShuffleWrapper.class)) {
            mocked.when(() -> ShuffleWrapper.shuffle(Mockito.anyList())).thenAnswer(inv -> null);
            mockMvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "gameId": "%s", "players": ["player-a","player-b","player-c","player-d"] }
                                    """.formatted(gameId)))
                    .andExpect(status().isOk());
        }
        websocketUtil.popAllPlayerMessage();

        mockMvc.perform(put("/api/debug/games/" + gameId + "/deck").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "cardIds": ["BS8008","BH3029","BH4030","BH2028",
                                              "BH6032","BH7033","BH8034","BH9035",
                                              "BC2054","BC3055","BC4056","BC5057",
                                              "BS7020","BS8021","BS9022","BS0023",
                                              "BH0036","BHJ037","BS9009","BHQ038"] }
                                """))
                .andExpect(status().isOk());

        chooseGeneral("player:monarchChooseGeneral", "player-a", "SHU004"); // 諸葛亮
        websocketUtil.popAllPlayerMessage();
        websocketUtil.popAllPlayerMessage();
        websocketUtil.popAllPlayerMessage();
        chooseGeneral("player:otherChooseGeneral", "player-b", "WU001");
        websocketUtil.popAllPlayerMessage();
        chooseGeneral("player:otherChooseGeneral", "player-c", "WU001");
        websocketUtil.popAllPlayerMessage();
        chooseGeneral("player:otherChooseGeneral", "player-d", "WU001"); // 最後一位 → A 回合開始

        Game paused = repository.findById(gameId).orElseThrow();
        assertEquals("player-a", paused.getCurrentRoundPlayer().getId());
        assertEquals(RoundPhase.Judgement, paused.getCurrentRoundPhase());
        assertTrue(paused.peekTopBehavior() instanceof WaitingSkillEffectBehavior w && "觀星".equals(w.getSkillName()));

        JsonNode ask = pollUntilInnerEvent("player-a", "AskSkillEffectEvent");
        assertEquals("觀星", ask.get("data").get("skillName").asText());
        assertEquals(List.of("ARRANGE", "SKIP"),
                objectMapper.convertValue(ask.get("data").get("options"), List.class));
        assertEquals(4, ask.get("data").get("dataCardIds").size());

        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-a", "skillName": "觀星", "choice": "SKIP", "cardIds": [] }
                                """))
                .andExpect(status().isOk());
        Game resumed = repository.findById(gameId).orElseThrow();
        assertEquals(RoundPhase.Action, resumed.getCurrentRoundPhase());
        assertEquals(6, resumed.getPlayer("player-a").getHandSize());
    }

    /** 逐一 poll 該玩家 queue，直到某則訊息的 events[] 或頂層 event 含指定 event 名稱。 */
    private JsonNode pollUntilInnerEvent(String playerId, String eventName) throws Exception {
        for (int attempt = 0; attempt < 10; attempt++) {
            String message = websocketUtil.getValue(playerId);
            if (message == null) {
                break;
            }
            JsonNode node = objectMapper.readTree(message);
            if (node.has("event") && eventName.equals(node.get("event").asText())) {
                return node;
            }
            if (node.has("events")) {
                for (JsonNode inner : node.get("events")) {
                    if (inner.has("event") && eventName.equals(inner.get("event").asText())) {
                        return inner;
                    }
                }
            }
        }
        fail(playerId + " 未收到 " + eventName);
        return null;
    }

    private void chooseGeneral(String action, String playerId, String generalId) throws Exception {
        mockMvc.perform(post("/api/games/" + gameId + "/" + action).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "%s", "generalId": "%s" }
                                """.formatted(playerId, generalId)))
                .andExpect(status().isOk());
    }
}

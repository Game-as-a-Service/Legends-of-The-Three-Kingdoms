package com.gaas.threeKingdoms.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.utils.ShuffleWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 選將進度（issue #237）：中途選將的推播內容 + 重整/重連（findGame）補進度。
 * 推播序列的逐步驗證在 GameTest.shouldReceiveSelectionStatus；本測試聚焦
 * 事件內容細節與 findGameEvent 的 selectionStatus 欄位。
 */
public class GeneralSelectionStatusE2ETest extends AbstractBaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("主公+B 選完後：進度推播內容正確；player-c 重整 findGame 補 selectionStatus")
    @Test
    public void selectionStatusPushedAndAvailableOnFindGame() throws Exception {
        // 建局（shuffle 關掉 → player-a 主公）
        try (MockedStatic<ShuffleWrapper> mocked = Mockito.mockStatic(ShuffleWrapper.class)) {
            mocked.when(() -> ShuffleWrapper.shuffle(Mockito.anyList())).thenAnswer(inv -> null);
            mockMvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "gameId": "%s", "players": ["player-a","player-b","player-c","player-d"] }
                                    """.formatted(gameId)))
                    .andExpect(status().isOk());
        }
        websocketUtil.popAllPlayerMessage();

        // 主公選將 → 消耗 MonarchGeneralChosenEvent / getGeneralCardEventByOthers，取進度推播
        mockMvc.perform(post("/api/games/" + gameId + "/player:monarchChooseGeneral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-a", "generalId": "SHU001" }
                                """))
                .andExpect(status().isOk());
        JsonNode monarchStatus = pollUntilEvent("player-d", "GeneralSelectionStatusEvent");
        assertEquals(List.of("player-a"),
                objectMapper.convertValue(monarchStatus.get("data").get("selectedPlayerIds"), List.class));
        assertEquals("player-a 已選擇武將（1/4）", monarchStatus.get("message").asText());
        // 消耗 player-c queue 中主公那則 1/4 進度，讓下一步 poll 到的是 2/4
        pollUntilEvent("player-c", "GeneralSelectionStatusEvent");

        // B 選將 → 全員收到 2/4；事件不帶武將 id（暗選）
        mockMvc.perform(post("/api/games/" + gameId + "/player:otherChooseGeneral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-b", "generalId": "WEI002" }
                                """))
                .andExpect(status().isOk());
        JsonNode bStatus = pollUntilEvent("player-c", "GeneralSelectionStatusEvent");
        assertEquals(List.of("player-a", "player-b"),
                objectMapper.convertValue(bStatus.get("data").get("selectedPlayerIds"), List.class));
        assertEquals(List.of("player-c", "player-d"),
                objectMapper.convertValue(bStatus.get("data").get("pendingPlayerIds"), List.class));
        assertFalse(bStatus.get("data").get("allSelected").asBoolean());
        assertFalse(bStatus.get("data").toString().contains("WEI002"), "進度事件不得洩漏暗選武將 id");

        // player-c 重整 → findGameEvent 帶 selectionStatus（Initial 階段）
        mockMvc.perform(get("/api/games/" + gameId).param("playerId", "player-c"))
                .andExpect(status().isOk());
        JsonNode findGame = pollUntilEvent("player-c", "findGameEvent");
        JsonNode selectionStatus = findGame.get("data").get("selectionStatus");
        assertNotNull(selectionStatus, "Initial 階段 findGame 應帶 selectionStatus");
        assertEquals(2, selectionStatus.get("selectedCount").asInt());
        assertEquals(4, selectionStatus.get("totalCount").asInt());
        assertEquals(List.of("player-c", "player-d"),
                objectMapper.convertValue(selectionStatus.get("pendingPlayerIds"), List.class));
        assertFalse(selectionStatus.get("allSelected").asBoolean());
    }

    /** 逐一 poll 該玩家 queue 直到收到指定 event（STOMP 順序不保證；未命中訊息丟棄）。 */
    private JsonNode pollUntilEvent(String playerId, String eventName) throws Exception {
        for (int attempt = 0; attempt < 6; attempt++) {
            String message = websocketUtil.getValue(playerId);
            if (message == null) {
                break;
            }
            JsonNode node = objectMapper.readTree(message);
            if (node.has("event") && eventName.equals(node.get("event").asText())) {
                return node;
            }
        }
        fail(playerId + " 未收到 " + eventName);
        return null;
    }
}

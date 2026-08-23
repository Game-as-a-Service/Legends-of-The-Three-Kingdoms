package com.gaas.threeKingdoms.e2e.skill;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 使用者回報（堅持可重現）：司馬懿被殺、不出閃、扣血後沒有反饋詢問。
 * 不走 initGame 捷徑 — 完整 API 流程：建局 → 主公選將 → 其他選將（B=司馬懿）→ 發牌 → A 出殺 → B 不出閃。
 */
public class FanKuiFullFlowTest extends AbstractBaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("完整流程：A(劉備) 殺 B(司馬懿)，B skip 扣血 → 推播含 AskSkillEffectEvent(反饋)")
    @Test
    public void fullFlow_killSimaYi_skip_asksFanKui() throws Exception {
        // 建局（shuffle 關掉 → 身分：player-a 主公）
        try (MockedStatic<ShuffleWrapper> mocked = Mockito.mockStatic(ShuffleWrapper.class)) {
            mocked.when(() -> ShuffleWrapper.shuffle(Mockito.anyList())).thenAnswer(inv -> null);
            mockMvc.perform(post("/api/games").contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    { "gameId": "%s", "players": ["player-a","player-b","player-c","player-d"] }
                                    """.formatted(gameId)))
                    .andExpect(status().isOk());
        }
        websocketUtil.popAllPlayerMessage();

        // 牌堆：發牌 a4/b4/c4/d4（A 第一張 = 殺）→ A 回合摸 2
        mockMvc.perform(put("/api/debug/games/" + gameId + "/deck").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "cardIds": ["BS8008","BH3029","BH4030","BH2028",
                                              "BH6032","BH7033","BH8034","BH9035",
                                              "BC2054","BC3055","BC4056","BC5057",
                                              "BS7020","BS8021","BS9022","BS0023",
                                              "BH0036","BHJ037"] }
                                """))
                .andExpect(status().isOk());

        // 選將
        chooseGeneral("player:monarchChooseGeneral", "player-a", "SHU001"); // 劉備
        websocketUtil.popAllPlayerMessage();
        chooseGeneral("player:otherChooseGeneral", "player-b", "WEI002");   // 司馬懿
        chooseGeneral("player:otherChooseGeneral", "player-c", "WU001");
        chooseGeneral("player:otherChooseGeneral", "player-d", "WU002");    // 最後一位 → 發牌、A 回合開始
        websocketUtil.popAllPlayerMessage();
        websocketUtil.popAllPlayerMessage();
        websocketUtil.popAllPlayerMessage();

        // A 出殺打 B
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", "active").andExpect(status().isOk());
        websocketUtil.popAllPlayerMessage();

        // B 不出閃
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip").andExpect(status().isOk());

        JsonNode pushB = objectMapper.readTree(websocketUtil.getValue("player-b"));
        System.out.println("=== PUSH TO B (full flow) ===\n" + pushB.toPrettyString());

        boolean asked = false;
        for (JsonNode e : pushB.get("events")) {
            if ("AskSkillEffectEvent".equals(e.get("event").asText())
                    && "反饋".equals(e.get("data").get("skillName").asText())) {
                asked = true;
            }
        }
        assertTrue(asked, "B（司馬懿）skip 扣血後應收到 AskSkillEffectEvent(反饋)");
        assertEquals("player-b", pushB.get("data").get("round").get("activePlayer").asText());
    }

    private void chooseGeneral(String action, String playerId, String generalId) throws Exception {
        mockMvc.perform(post("/api/games/" + gameId + "/" + action).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "%s", "generalId": "%s" }
                                """.formatted(playerId, generalId)))
                .andExpect(status().isOk());
    }
}

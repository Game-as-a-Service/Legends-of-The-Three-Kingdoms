package com.gaas.threeKingdoms.e2e;

import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


/**
 * MockMvcUtil doc
 */
public class MockMvcUtil {
    private final MockMvc mockMvc;

    public MockMvcUtil(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions playCard(String gameId, String currentPlayerId, String targetPlayerId, String cardId, String playType) throws Exception {
        return this.mockMvc.perform(post("/api/games/" + gameId + "/player:playCard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        { "playerId": "%s",
                          "targetPlayerId": "%s",
                          "cardId": "%s",
                          "playType": "%s"
                        }""", currentPlayerId, targetPlayerId, cardId, playType)));
    }

    public ResultActions playCardWithoutCardId(String gameId, String currentPlayerId, String targetPlayerId, String playType) throws Exception {
        return this.mockMvc.perform(post("/api/games/" + gameId + "/player:playCard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        { "playerId": "%s",
                          "targetPlayerId": "%s",
                          "playType": "%s"
                        }""", currentPlayerId, targetPlayerId, playType)));
    }

    public ResultActions useEquipment(String gameId, String currentPlayerId, String targetPlayerId, String cardId, EquipmentPlayType playType) throws Exception {
        return this.mockMvc.perform(post("/api/games/" + gameId + "/player:useEquipmentEffect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        { "playerId": "%s",
                          "targetPlayerId": "%s",
                          "cardId": "%s",
                          "playType": "%s"
                        }""", currentPlayerId, targetPlayerId, cardId, playType.getPlayType())));
    }
}

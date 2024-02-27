package com.gaas.threeKingdoms.e2e;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


public class MockMvcUtil {
    private final MockMvc mockMvc;

    public MockMvcUtil(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions playCard(String currentPlayerId, String targetPlayerId, String cardId, String playType) throws Exception {
        return this.mockMvc.perform(post("/api/games/my-id/player:playCard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        { "playerId": "%s",
                          "targetPlayerId": "%s",
                          "cardId": "%s",
                          "playType": "%s"
                        }""", currentPlayerId, targetPlayerId, cardId, playType)));
    }
}

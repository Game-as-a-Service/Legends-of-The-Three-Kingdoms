package com.gaas.threeKingdoms.e2e;

import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


/**
 * MockMvcUtil doc
 */
public class MockMvcUtil {
    private final MockMvc mockMvc;

    public MockMvcUtil(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public ResultActions useDismantleEffect(String gameId, String currentPlayerId, String targetPlayerId, String cardId, Integer targetCardIndex) throws Exception {
        String formattedCardId = cardId == null ? null : "\"" + cardId + "\"";

        String content = String.format("""
            {
              "currentPlayerId": "%s",
              "targetPlayerId": "%s",
              "cardId": %s,
              "targetCardIndex": %s
            }
            """, currentPlayerId, targetPlayerId, formattedCardId, targetCardIndex);

        return this.mockMvc.perform(post("/api/games/" + gameId + "/player:useDismantleEffect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
    }


    public ResultActions useBorrowedSwordEffect(String gameId, String currentPlayerId, String borrowedPlayerId, String attackTargetPlayerId) throws Exception {
        return this.mockMvc.perform(post("/api/games/" + gameId + "/player:useBorrowedSwordEffect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        { "currentPlayerId": "%s",
                          "borrowedPlayerId": "%s",
                          "attackTargetPlayerId": "%s"
                        }""", currentPlayerId, borrowedPlayerId, attackTargetPlayerId)));
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

    public ResultActions finishAction(String gameId, String playerId) throws Exception {
        return this.mockMvc.perform(post("/api/games/" + gameId + "/player:finishAction")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        { "playerId": "%s"
                        }""", playerId)));
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

    public ResultActions useEquipment(String gameId, String currentPlayerId, String cardId, EquipmentPlayType playType) throws Exception {
        return this.mockMvc.perform(post("/api/games/" + gameId + "/player:useEquipmentEffect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        { "playerId": "%s",
                          "cardId": "%s",
                          "playType": "%s"
                        }""", currentPlayerId, cardId, playType.getPlayType())));
    }

    public ResultActions chooseHorse(String gameId, String currentPlayerId, String cardId) throws Exception {
        return this.mockMvc.perform(post("/api/games/" + gameId + "/player:chooseHorseCard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        { "playerId": "%s",
                          "cardId": "%s"
                        }""", currentPlayerId, cardId)));
    }
}

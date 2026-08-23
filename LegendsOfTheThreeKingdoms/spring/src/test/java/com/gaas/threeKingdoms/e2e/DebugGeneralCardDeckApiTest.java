package com.gaas.threeKingdoms.e2e;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.generalcard.GeneralCardDeck;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Arrays;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 武將牌堆 debug API（對應 DebugDeckApiTest 的 deck 版本）。 */
public class DebugGeneralCardDeckApiTest extends AbstractBaseIntegrationTest {

    @Test
    public void testGetGeneralCardDeck_ReturnsDrawOrder() throws Exception {
        givenGameWithKnownGeneralCardDeck();

        mockMvc.perform(get("/api/debug/games/" + gameId + "/generalCardDeck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.deckSize").value(3))
                // Stack push 順序：關羽(底) → 張飛 → 趙雲(頂)，pop 先取頂
                .andExpect(jsonPath("$.generalIds[0]").value("SHU005"))
                .andExpect(jsonPath("$.generalIds[1]").value("SHU003"))
                .andExpect(jsonPath("$.generalIds[2]").value("SHU002"));
    }

    @Test
    public void testSetGeneralCardDeck_ReplacesDeckAndReturnsNewOrder() throws Exception {
        givenGameWithKnownGeneralCardDeck();

        mockMvc.perform(put("/api/debug/games/" + gameId + "/generalCardDeck")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "generalIds": ["WEI002", "SHU001"] }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deckSize").value(2))
                .andExpect(jsonPath("$.generalIds[0]").value("WEI002"))
                .andExpect(jsonPath("$.generalIds[1]").value("SHU001"));

        // 驗證 GET 也反映新順序（經 MongoDB 存取）
        mockMvc.perform(get("/api/debug/games/" + gameId + "/generalCardDeck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deckSize").value(2))
                .andExpect(jsonPath("$.generalIds[0]").value("WEI002"))
                .andExpect(jsonPath("$.generalIds[1]").value("SHU001"));
    }

    @Test
    public void testSetGeneralCardDeck_InvalidGeneralId_Returns400() throws Exception {
        givenGameWithKnownGeneralCardDeck();

        mockMvc.perform(put("/api/debug/games/" + gameId + "/generalCardDeck")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "generalIds": ["SHU001", "NOPE"] }
                                """))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("Invalid general ID: NOPE"));
    }

    @Test
    public void testSetGeneralCardDeck_EmptyList_ClearsDeck() throws Exception {
        givenGameWithKnownGeneralCardDeck();

        mockMvc.perform(put("/api/debug/games/" + gameId + "/generalCardDeck")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "generalIds": [] }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deckSize").value(0))
                .andExpect(jsonPath("$.generalIds", hasSize(0)));
    }

    @Test
    public void testGetGeneralCardDeck_GameNotFound_Returns4xx() throws Exception {
        mockMvc.perform(get("/api/debug/games/nonexistent/generalCardDeck"))
                .andExpect(status().is4xxClientError());
    }

    private void givenGameWithKnownGeneralCardDeck() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH);
        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        GeneralCardDeck deck = new GeneralCardDeck();
        deck.getGeneralStack().push(new GeneralCard(General.關羽));
        deck.getGeneralStack().push(new GeneralCard(General.張飛));
        deck.getGeneralStack().push(new GeneralCard(General.趙雲));
        game.setGeneralCardDeck(deck);
        repository.save(game);
    }
}

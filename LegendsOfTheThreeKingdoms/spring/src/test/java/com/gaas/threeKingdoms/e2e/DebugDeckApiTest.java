package com.gaas.threeKingdoms.e2e;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class DebugDeckApiTest extends AbstractBaseIntegrationTest {

    @Test
    public void testGetDeck_ReturnsDeckInDrawOrder() throws Exception {
        givenGameWithKnownDeck();

        mockMvc.perform(get("/api/debug/games/" + gameId + "/deck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.deckSize").value(3))
                // Stack addAll 順序：Kill(底) → Peach → Dodge(頂)，pop 先取頂
                .andExpect(jsonPath("$.cardIds[0]").value("BH2028"))
                .andExpect(jsonPath("$.cardIds[1]").value("BH3029"))
                .andExpect(jsonPath("$.cardIds[2]").value("BS8008"));
    }

    @Test
    public void testSetDeck_ReplacesDeckAndReturnsNewOrder() throws Exception {
        givenGameWithKnownDeck();

        mockMvc.perform(put("/api/debug/games/" + gameId + "/deck")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "cardIds": ["BH4030", "BS9009"] }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deckSize").value(2))
                .andExpect(jsonPath("$.cardIds[0]").value("BH4030"))
                .andExpect(jsonPath("$.cardIds[1]").value("BS9009"));

        // 驗證 GET 也反映新順序
        mockMvc.perform(get("/api/debug/games/" + gameId + "/deck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deckSize").value(2))
                .andExpect(jsonPath("$.cardIds[0]").value("BH4030"))
                .andExpect(jsonPath("$.cardIds[1]").value("BS9009"));
    }

    @Test
    public void testSetDeck_InvalidCardId_Returns400() throws Exception {
        givenGameWithKnownDeck();

        mockMvc.perform(put("/api/debug/games/" + gameId + "/deck")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "cardIds": ["BS8008", "INVALID_ID"] }
                                """))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("Invalid card ID: INVALID_ID"));
    }

    @Test
    public void testSetDeck_EmptyList_ClearsDeck() throws Exception {
        givenGameWithKnownDeck();

        mockMvc.perform(put("/api/debug/games/" + gameId + "/deck")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "cardIds": [] }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deckSize").value(0))
                .andExpect(jsonPath("$.cardIds", hasSize(0)));
    }

    @Test
    public void testSetDeck_AllowsDuplicateCardIds() throws Exception {
        givenGameWithKnownDeck();

        // debug 工具允許重複 cardId（測試可能需要非正常牌堆）
        mockMvc.perform(put("/api/debug/games/" + gameId + "/deck")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "cardIds": ["BS8008", "BS8008", "BS8008"] }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deckSize").value(3))
                .andExpect(jsonPath("$.cardIds[0]").value("BS8008"))
                .andExpect(jsonPath("$.cardIds[1]").value("BS8008"))
                .andExpect(jsonPath("$.cardIds[2]").value("BS8008"));
    }

    @Test
    public void testGetDeck_GameNotFound_Returns4xx() throws Exception {
        mockMvc.perform(get("/api/debug/games/nonexistent/deck"))
                .andExpect(status().is4xxClientError());
    }

    private void givenGameWithKnownDeck() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH);
        Player playerB = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.TRAITOR);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        com.gaas.threeKingdoms.handcard.Deck deck = new com.gaas.threeKingdoms.handcard.Deck();
        deck.add(List.of(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);
    }
}

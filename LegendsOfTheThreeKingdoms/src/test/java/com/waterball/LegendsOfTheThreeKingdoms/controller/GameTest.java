package com.waterball.LegendsOfTheThreeKingdoms.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GeneralCardDto;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.repository.InMemoryGameRepository;
import com.waterball.LegendsOfTheThreeKingdoms.utils.ShuffleWrapper;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class GameTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryGameRepository inMemoryGameRepository;

    private ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void shouldReturnDefaultMessage() throws Exception {
        this.mockMvc.perform(get("/api/hello")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello, world!")));
    }

    // Monarch
    // Minister
    // Rebel
    // Traitors
    public void shouldStartGame() throws Exception {
        // create game

        String requestBody = objectMapper.writeValueAsString(
                TestGameBuilder.newGame()
                        .withGameId("my-id")
                        .players(4)
                        .withPlayerId("player-a", "player-b", "player-c", "player-d")
                        .build());

        String responseBody = objectMapper.writeValueAsString(TestGameBuilder.newGame()
                .withGameId("my-id")
                .players(4)
                .withPlayerId("player-a", "player-b", "player-c", "player-d")
                .withPlayerRoles("Monarch", "Minister", "Rebel", "Traitor")
                .build());

        try (MockedStatic<ShuffleWrapper> mockedStatic = Mockito.mockStatic(ShuffleWrapper.class)) {
            mockedStatic.when(() -> ShuffleWrapper.shuffle(Mockito.anyList()))
                    .thenAnswer(invocation -> null);

            this.mockMvc.perform(post("/api/games")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(content().string(responseBody));

            // find the game
            this.mockMvc.perform(get("/api/games/my-id")).andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string(responseBody));
        }
    }


    // 主公拿到可以選的五張武將牌 //get api

    // 選一張 // post api
    // 牌堆減少剛剛抽出的牌
    // 主公身上有武將牌

    // Happy Path
    // 玩家總共4人

    @Test
    public void happyPath() throws Exception {
        shouldStartGame();
        shouldChooseGeneralsByMonarch();
        shouldChooseGeneralsByOthers();
    }

    private void shouldChooseGeneralsByMonarch() throws Exception {
        // When 玩家A選劉備

        // 拿到可以選的武將牌
        MvcResult result = this.mockMvc.perform(get("/api/games/my-id/player-a/generals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<GeneralCardDto> generalCards = objectMapper.readValue(json, new TypeReference<List<GeneralCardDto>>() {
        });
        assertEquals("孫權", generalCards.get(0).getGeneralName());
        assertEquals("曹操", generalCards.get(1).getGeneralName());
        assertEquals("劉備", generalCards.get(2).getGeneralName());
        assertEquals(5, generalCards.size());
        // 主公選一張
        this.mockMvc.perform(post("/api/games/my-id/player-a/general/general0")).andDo(print())
                .andExpect(status().isOk());

        // Then 玩家A武將為劉備 ((主公general是 general0 is true)
        Game game = inMemoryGameRepository.findGameById("my-id");
        assertEquals("general0", game.getPlayer("player-a").getGeneralCard().getGeneralID());
        // 牌堆不能有 general0
        assertEquals(0, game.getGeneralCardDeck().getGeneralStack()
                .stream().filter(x -> x.getGeneralID().equals("general0"))
                .count());
    }

    private void shouldChooseGeneralsByOthers() throws Exception {

//        Given
//        玩家A為主公 ，選擇了武將「劉備」
//        B 為忠臣
//        C 為反賊
//        D 為內奸
//        剩下武將牌「曹操」「孫權」「呂布」「董卓」
//
//        When
//        玩家 B 選武將曹操
//        玩家 C 選武將孫權
//        玩家 D 選武將呂布
//
//        Then
//        玩家 B武將為 曹操
//        玩家 C武將為 孫權
//        玩家 D武將為 呂布

        //  劉備 曹操 孫權 關羽 張飛 馬超 趙雲 黃月英 諸葛亮 黃忠 魏延

        // 拿到可以選的武將牌
        MvcResult resultOfPlayerB = this.mockMvc.perform(get("/api/games/my-id/player-b/generals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String json = resultOfPlayerB.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<GeneralCardDto> generalCards = objectMapper.readValue(json, new TypeReference<List<GeneralCardDto>>() {
        });
        assertEquals("馬超", generalCards.get(0).getGeneralName());
        assertEquals("趙雲", generalCards.get(1).getGeneralName());
        assertEquals("黃月英", generalCards.get(2).getGeneralName());
        assertEquals(3, generalCards.size());
        // 玩家B選曹操
        this.mockMvc.perform(post("/api/games/my-id/player-b/general/general1")).andDo(print())
                .andExpect(status().isOk());

        // Then 玩家B武將為曹操 ((玩家B general是 general1 is true)
        Game game = inMemoryGameRepository.findGameById("my-id");
        assertEquals("general1", game.getPlayer("player-b").getGeneralCard().getGeneralID());
        // 牌堆不能有 general1
        assertEquals(0, game.getGeneralCardDeck().getGeneralStack()
                .stream().filter(x -> x.getGeneralID().equals("general1"))
                .count());

        //********************************* Round 2 ***************************************//

        MvcResult result2 = this.mockMvc.perform(get("/api/games/my-id/player-c/generals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String json2 = result2.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<GeneralCardDto> generalCards2 = objectMapper.readValue(json, new TypeReference<List<GeneralCardDto>>() {
        });
        assertEquals("張飛", generalCards.get(0).getGeneralName());
        assertEquals("馬超", generalCards.get(1).getGeneralName());
        assertEquals("趙雲", generalCards.get(2).getGeneralName());
        assertEquals(3, generalCards.size());

        // 玩家B選曹操
        this.mockMvc.perform(post("/api/games/my-id/player-c/general/general2")).andDo(print())
                .andExpect(status().isOk());

        // Then 玩家B武將為曹操 ((玩家B general是 general1 is true)
        assertEquals("general0", game.getPlayer("player-b").getGeneralCard().getGeneralID());
        // 牌堆不能有 general1
        assertEquals(0, game.getGeneralCardDeck().getGeneralStack()
                .stream().filter(x -> x.getGeneralID().equals("general1"))
                .count());

        //********************************* Round 3 ***************************************//

        MvcResult result3 = this.mockMvc.perform(get("/api/games/my-id/player-d/generals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String json3 = result3.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<GeneralCardDto> generalCards3 = objectMapper.readValue(json, new TypeReference<List<GeneralCardDto>>() {
        });
        assertEquals("黃月英", generalCards.get(0).getGeneralName());
        assertEquals("諸葛亮", generalCards.get(1).getGeneralName());
        assertEquals("黃忠", generalCards.get(2).getGeneralName());
        assertEquals(3, generalCards.size());
        // 玩家B選曹操
        this.mockMvc.perform(post("/api/games/my-id/player-b/general/general1")).andDo(print())
                .andExpect(status().isOk());

        // Then 玩家B武將為曹操 ((玩家B general是 general1 is true)
        assertEquals("general0", game.getPlayer("player-b").getGeneralCard().getGeneralID());
        // 牌堆不能有 general1
        assertEquals(0, game.getGeneralCardDeck().getGeneralStack()
                .stream().filter(x -> x.getGeneralID().equals("general1"))
                .count());
    }
}

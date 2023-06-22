package com.waterball.LegendsOfTheThreeKingdoms.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GameDto;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GeneralCardDto;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.RoleCard;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class HelloWorldTest {

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
    @Test
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
                .withPlayerRoles("MONARCH", "MINISTER", "REBEL", "TRAITOR")
                .build());

        try (MockedStatic<ShuffleWrapper> mockedStatic = Mockito.mockStatic(ShuffleWrapper.class)) {
            mockedStatic.when(() -> ShuffleWrapper.shuffle(Mockito.anyList()))
                    .thenAnswer( invocation -> null);

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


    @Test
    public void testObjectMapper() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        GameDto gameDto = new GameDto();
        gameDto.setGameId("my-first-game-id");

        System.out.println(objectMapper.writeValueAsString(gameDto));

        Path path = Paths.get("create_game.json");
        String s = Files.readString(path);

        GameDto gameDto1 = objectMapper.readValue(s, GameDto.class);
        System.out.println(objectMapper.writeValueAsString(gameDto1));

    }

    // 主公拿到可以選的五張武將牌 //get api

    // 選一張 // post api
    // 牌堆減少剛剛抽出的牌
    // 主公身上有武將牌

    // Happy Path
    // 玩家總共4人

    @Test
    public void shouldChooseGeneralByMonarch() throws Exception {

        // Given
        // 玩家A為主公BCD為其他身份
        // B,C,D 為其他身份
        // A從武將牌堆抽兩張卡 + 三張固定武將卡，選擇武將
        //「劉備」「曹操」「孫權」「x」「x」
        String gameRequestBody = objectMapper.writeValueAsString(
                TestGameBuilder.newGame()
                        .withGameId("my-id")
                        .players(4)
                        .withPlayerId("player-a", "player-b", "player-c", "player-d")
                        .build());

        //產生遊戲
        this.mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameRequestBody));

        MvcResult result = this.mockMvc.perform(get("/api/games/my-id/player-a/generals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<GeneralCardDto> generalCards = objectMapper.readValue(json, new TypeReference<List<GeneralCardDto>>(){});
        assertEquals("孫權", generalCards.get(0).getGeneralName());
        assertEquals("曹操", generalCards.get(1).getGeneralName());
        assertEquals("劉備", generalCards.get(2).getGeneralName());
        assertEquals(5, generalCards.size());

        // When 玩家A選劉備

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
}

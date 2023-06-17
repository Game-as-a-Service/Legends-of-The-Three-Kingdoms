package com.waterball.LegendsOfTheThreeKingdoms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GameDto;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GeneralCardDto;
import com.waterball.LegendsOfTheThreeKingdoms.domain.RoleCard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AllPermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.hamcrest.Matchers.containsString;
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
                        .players(4)
                        .build());

        String responseBody = objectMapper.writeValueAsString(TestGameBuilder.newGame()
                .players(4)
                .withPlayerRoles("Monarch", "Minister", "Rebel", "Traitor")
                .build());


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

        String gameRequestBody = objectMapper.writeValueAsString(
                TestGameBuilder.newGame()
                        .players(4)
                        .build());

        ArrayList<GeneralCardDto> generalCardDtoList = new ArrayList<>();
        generalCardDtoList.add(new GeneralCardDto("e","e"));
        generalCardDtoList.add(new GeneralCardDto("d","d"));
        generalCardDtoList.add(new GeneralCardDto("c","c"));
        generalCardDtoList.add(new GeneralCardDto("b","b"));
        generalCardDtoList.add(new GeneralCardDto("a","a"));

        String getGeneralResponseBody = objectMapper.writeValueAsString(generalCardDtoList);

        String chooseGeneralResponseBody = objectMapper.writeValueAsString(
                TestGameBuilder.newGame()
                        .players(4)
                        .withPlayerRoles("Monarch", "Minister", "Rebel", "Traitor")
                        .build());

        String responseBody = objectMapper.writeValueAsString(
                TestGameBuilder.newGame()
                        .players(4)
                        .withPlayerRoles("Monarch", "Minister", "Rebel", "Traitor")
                        .withPlayerGeneral("a")
                        .build());

        //產生遊戲
        this.mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(gameRequestBody));

        // 主公拿到可以選的五張武將牌 //get api
        this.mockMvc.perform(get("/api/games/my-id/player-a/generals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(getGeneralResponseBody));

        // 主公選一張 // post api ///api/games/{gameId}/{playerId}/general/{generalId}
        this.mockMvc.perform(post("/api/games/my-id/player-a/general/a")).andDo(print())
                .andExpect(status().isOk());

        // 確認game是否正確的主公選了武將
        this.mockMvc.perform(get("/api/games/my-id")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(responseBody));

    }

//    @Test
//    public void shouldChooseGeneralByOtherPlayer() throws Exception {
        // playerId
        // generalIndex
        // generalId
        // gameId
//        String requestBody = "{\"gameId\":\"my-id\",\"playerId\":\"2\"}"; // 固定曹操、劉備、孫權 + 2張隨機
//        String getGeneralResponseBody = "{\"gameId\":\"my-id\",\"playerId\":\"2\",\"generals\":[\"b\",\"c\",\"d\",\"e\",\"f\"]}";
//        String chooseGeneralResponseBody = "{\"gameId\":\"my-id\",\"players\":[{\"id\":\"player-a\",\"role\":\"Monarch\",\"general\":\"a\"},{\"id\":\"player-b\",\"role\":\"Minister\",\"general\":\"\"},{\"id\":\"player-c\",\"role\":\"Rebel\",\"general\":\"\"},{\"id\":\"player-d\",\"role\":\"Traitor\",\"general\":\"\"}]}";

//        // find the game
//        this.mockMvc.perform(get("/api/games/my-id")).andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().string(responseBody));

        // 其他玩家拿到可以選的五張武將牌 //get api
//        this.mockMvc.perform(get("/api/games/my-id/generals")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(requestBody))
//                .andExpect(status().isOk())
//                .andExpect(content().string(getGeneralResponseBody));
//    }

//    @Test
//    public void shouldGenerateSampleCase() throws Exception{
//        PlayerDto playerRequestDto = new PlayerDto();
//        PlayerDto playerResponseDto = new PlayerDto();
//
//        this.mockMvc.perform(get("/api/games/my-id/generals")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(String.valueOf(playerRequestDto)))
//                .andExpect(status().isOk())
//                .andExpect(content().string(String.valueOf(playerResponseDto)));
//    }

}

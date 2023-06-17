package com.waterball.LegendsOfTheThreeKingdoms.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterball.LegendsOfTheThreeKingdoms.controller.dto.GameDto;
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
import java.util.ArrayList;


@SpringBootTest
@AutoConfigureMockMvc
public class HelloWorldTest {

    @Autowired
    private MockMvc mockMvc;

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

        String requestBody = "{\"gameId\":\"my-id\",\"players\":[{\"id\":\"player-a\"},{\"id\":\"player-b\"},{\"id\":\"player-c\"},{\"id\":\"player-d\"}]}";
        String responseBody = "{\"gameId\":\"my-id\",\"players\":[{\"id\":\"player-a\",\"role\":\"Monarch\"},{\"id\":\"player-b\",\"role\":\"Minister\"},{\"id\":\"player-c\",\"role\":\"Rebel\"},{\"id\":\"player-d\",\"role\":\"Traitor\"}]}";

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

}
package com.waterball.LegendsOfTheThreeKingdoms.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterball.LegendsOfTheThreeKingdoms.controller.unittest.Utils;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Dodge;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Kill;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Peach;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GeneralCardDto;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.repository.InMemoryGameRepository;
import com.waterball.LegendsOfTheThreeKingdoms.utils.ShuffleWrapper;
import org.junit.jupiter.api.Assertions;
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
import java.util.Arrays;
import java.util.List;

import static com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayCard.*;
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

    @Test
    public void happyPath() throws Exception {
        shouldStartGame();
        shouldChooseGeneralsByMonarch();
        shouldChooseGeneralsByOthers();
        shouldInitialHP();
        shouldDealCardToPlayers();
        shouldDrawCardToPlayer();
        shouldPlayedCard();
    }

    private void shouldPlayedCard() throws Exception {
       /*
        Given
        輪到 A 玩家出牌
        A 玩家手牌有殺x2, 閃x2, 桃x2
        B 玩家在 A 玩家的攻擊距離

        When
        A 玩家對 B 玩家出殺

        Then
        A 玩家出殺成功
        A 玩家手牌有殺x1, 閃x2, 桃x2
         */
        this.mockMvc.perform(post("/api/games/my-id/player:playCard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-a",
                                  "targetPlayerId": "player-b",
                                  "cardId": "BS8008"
                                }"""))
                .andExpect(status().isOk())
                .andReturn();

        Game game = inMemoryGameRepository.findGameById("my-id");
        assertEquals(5, game.getPlayer("player-a").getHandSize());
        Assertions.assertTrue(Utils.compareArrayLists(Arrays.asList(new Kill(BS8008),new Peach(BH4030), new Peach(BH4030), new Dodge(BHK039), new Dodge(BHK039)), game.getPlayer("player-a").getHand().getCards()));

    }

    private void shouldDrawCardToPlayer() {
        Game game = inMemoryGameRepository.findGameById("my-id");
        game.drawCardToPlayer("player-a");
        assertEquals(6, game.getPlayer("player-a").getHandSize());
    }

    private void shouldDealCardToPlayers() {
        Game game = inMemoryGameRepository.findGameById("my-id");
        game.assignHandCardToPlayers();

        assertEquals(4, game.getPlayer("player-a").getHandSize());
        assertEquals(4, game.getPlayer("player-b").getHandSize());
        assertEquals(4, game.getPlayer("player-c").getHandSize());
        assertEquals(4, game.getPlayer("player-d").getHandSize());
    }

    public void shouldInitialHP(){
        Game game = inMemoryGameRepository.findGameById("my-id");
        game.assignHpToPlayers();

        assertEquals(5, game.getPlayer("player-a").getHP());
        assertEquals(4, game.getPlayer("player-b").getHP());
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertEquals(3, game.getPlayer("player-d").getHP());
    }

    public void shouldStartGame() throws Exception {
        // create game

        // Monarch
        // Minister
        // Rebel
        // Traitors

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

    private void shouldChooseGeneralsByMonarch() throws Exception {

        /*
        主公拿到可以選的五張武將牌 //get api

          選一張 // post api
          牌堆減少剛剛抽出的牌
          主公身上有武將牌

          Happy Path
          玩家總共4人

          When 玩家A選劉備

         拿到可以選的武將牌
         */
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
        this.mockMvc.perform(post("/api/games/my-id/player-a/general/SHU001")).andDo(print())
                .andExpect(status().isOk());

        // Then 玩家A武將為劉備 ((主公general是 SHU001 is true)
        Game game = inMemoryGameRepository.findGameById("my-id");
        assertEquals("SHU001", game.getPlayer("player-a").getGeneralCard().getGeneralID());
        // 牌堆不能有 SHU001
        assertEquals(0, game.getGeneralCardDeck().getGeneralStack()
                .stream().filter(x -> x.getGeneralID().equals("SHU001"))
                .count());
    }

    private void shouldChooseGeneralsByOthers() throws Exception {

        /*
        Given
        玩家A為主公 ，選擇了武將「劉備」
        B 為忠臣 可選武將牌「馬超」「趙雲」「黃月英」
        C 為反賊 可選武將牌「諸葛亮」 「黃忠」「魏延」
        D 為內奸 可選武將牌「司馬懿」「夏侯敦」「許褚」

          When
        玩家 B 選武將 馬超
        玩家 C 選武將 諸葛亮
        玩家 D 選武將 司馬懿
          Then
        玩家 B武將為 馬超
        玩家 C武將為 諸葛亮
        玩家 D武將為 司馬懿

          玩家B拿到可以選的武將牌
          */
        MvcResult resultOfPlayerB = this.mockMvc.perform(get("/api/games/my-id/player-b/generals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResultOfPlayerB = resultOfPlayerB.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<GeneralCardDto> generalCards = objectMapper.readValue(jsonResultOfPlayerB, new TypeReference<List<GeneralCardDto>>() {
        });
        assertEquals("馬超", generalCards.get(0).getGeneralName());
        assertEquals("趙雲", generalCards.get(1).getGeneralName());
        assertEquals("黃月英", generalCards.get(2).getGeneralName());
        assertEquals(3, generalCards.size());

        // 玩家B選馬超
        this.mockMvc.perform(post("/api/games/my-id/player-b/general/SHU006")).andDo(print())
                .andExpect(status().isOk());

        // Then 玩家B武將為馬超 ((玩家B general是 general1 is true)
        Game game = inMemoryGameRepository.findGameById("my-id");
        assertEquals("SHU006", game.getPlayer("player-b").getGeneralCard().getGeneralID());
        // 牌堆不能有 general1
        assertEquals(0, game.getGeneralCardDeck().getGeneralStack()
                .stream().filter(x -> x.getGeneralID().equals("SHU006"))
                .count());

        //********************************* Round 2 ***************************************//

        MvcResult resultOfPlayerC = this.mockMvc.perform(get("/api/games/my-id/player-c/generals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResultOfPlayerC = resultOfPlayerC.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<GeneralCardDto> generalCards2 = objectMapper.readValue(jsonResultOfPlayerC, new TypeReference<List<GeneralCardDto>>() {
        });
        assertEquals("諸葛亮", generalCards2.get(0).getGeneralName());
        assertEquals("黃忠", generalCards2.get(1).getGeneralName());
        assertEquals("魏延", generalCards2.get(2).getGeneralName());
        assertEquals(3, generalCards2.size());

        // 玩家C選諸葛亮
        this.mockMvc.perform(post("/api/games/my-id/player-c/general/SHU004")).andDo(print())
                .andExpect(status().isOk());

        // Then 玩家C武將為諸葛亮 ((玩家C genera1是 general18 is true)
        assertEquals("SHU004", game.getPlayer("player-c").getGeneralCard().getGeneralID());
        // 牌堆不能有 general1
        assertEquals(0, game.getGeneralCardDeck().getGeneralStack()
                .stream().filter(x -> x.getGeneralID().equals("SHU004"))
                .count());

        //********************************* Round 3 ***************************************//

        MvcResult resultOfPlayerD = this.mockMvc.perform(get("/api/games/my-id/player-d/generals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResultOfPlayerD = resultOfPlayerD.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<GeneralCardDto> generalCards3 = objectMapper.readValue(jsonResultOfPlayerD, new TypeReference<List<GeneralCardDto>>() {
        });
        assertEquals("司馬懿", generalCards3.get(0).getGeneralName());
        assertEquals("夏侯敦", generalCards3.get(1).getGeneralName());
        assertEquals("許褚", generalCards3.get(2).getGeneralName());
        assertEquals(3, generalCards3.size());

        // 玩家D選司馬懿
        this.mockMvc.perform(post("/api/games/my-id/player-d/general/WEI002")).andDo(print())
                .andExpect(status().isOk());

        // Then 玩家D武將為司馬懿 ((玩家D general是 general1 is true)
        assertEquals("WEI002", game.getPlayer("player-d").getGeneralCard().getGeneralID());
        // 牌堆不能有 general1
        assertEquals(0, game.getGeneralCardDeck().getGeneralStack()
                .stream().filter(x -> x.getGeneralID().equals("WEI002"))
                .count());
    }
}

package com.waterball.LegendsOfTheThreeKingdoms.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterball.LegendsOfTheThreeKingdoms.controller.unittest.Utils;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Phase;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.Deck;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Dodge;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Kill;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.repository.InMemoryGameRepository;
import com.waterball.LegendsOfTheThreeKingdoms.service.dto.GeneralCardDto;
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
import java.util.Stack;

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
        // initial game
        shouldCreateGame();
        shouldChooseGeneralsByMonarch();
        shouldChooseGeneralsByOthers();
        shouldInitialHP();
        shouldDealCardToPlayers();

        // Round 1 hp-0 本人
        shouldJudgementPhase();
        shouldDrawCardToPlayer();
        shouldPlayerAPlayedCard("player-b");
        shouldPlayerFinishAction();
        shouldPlayerADiscardCard();

        //Round 2 hp-1
        shouldJudgementPhase();
        shouldDrawCardToPlayer();
        shouldPlayerBPlayedCard("player-a");
        shouldPlayerFinishAction();
        shouldPlayerBDiscardCard();

        //Round 3 hp-0 距離太遠 直接棄牌
        //Round 4 hp-1
        //Round 5 hp-0 本人
//        {
//            Round 3: player A, player B, card kill,
//            Round 4: player C, player B, card kill
//        }

        //Round 6 hp-1
        //Round 7 hp-0 距離太遠
        //Round 8 hp-1
        //Round 9 hp-0 本人
        //Round 10 hp-1
    }

    private void shouldPlayerBPlayedCard(String targetPlayerId) throws Exception {
        /*
        Given
        輪到 B 玩家出牌
        B 玩家手牌有殺x2, 閃x2, 桃x2
        A 玩家在 B 玩家的攻擊距離

        When
        B 玩家對 A 玩家出殺

        Then
        B 玩家出殺成功
        A 玩家hp = 3
        B 玩家手牌剩五張
        */
        Game game = inMemoryGameRepository.findGameById("my-id");

        Player currentRoundPlayer = game.getCurrentRoundPlayer();
        List<HandCard> cards = currentRoundPlayer.getHand().getCards();
        String cardId = cards.stream().filter(card -> card instanceof Kill).findFirst().get().getId();

        this.mockMvc.perform(post("/api/games/my-id/player:playCard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                { "playerId": "%s",
                                  "targetPlayerId": "%s",
                                  "cardId": "%s"
                                }""", currentRoundPlayer.getId(), targetPlayerId, cardId)))
                .andExpect(status().isOk())
                .andReturn();

        game = inMemoryGameRepository.findGameById("my-id");
        assertEquals(5, game.getPlayer(currentRoundPlayer.getId()).getHandSize());
        assertEquals(4, game.getPlayer(targetPlayerId).getBloodCard().getHp());
    }

    public void shouldCreateGame() throws Exception {

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

        Stack<HandCard> stack = new Stack<>();

        Arrays.stream(values())
                .filter(x -> x.getCardName().equals("閃"))
                .map(Dodge::new)
                .forEach(stack::push);

        Arrays.stream(values())
                .filter(x -> x.getCardName().equals("殺"))
                .map(Kill::new)
                .forEach(stack::push);

        Game game = inMemoryGameRepository.findGameById("my-id");
        game.setDeck(new Deck(stack));
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

    public void shouldInitialHP() {
        Game game = inMemoryGameRepository.findGameById("my-id");
        game.assignHpToPlayers();

        assertEquals(5, game.getPlayer("player-a").getHP());
        assertEquals(4, game.getPlayer("player-b").getHP());
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertEquals(3, game.getPlayer("player-d").getHP());
    }

    private void shouldDealCardToPlayers() {
        Game game = inMemoryGameRepository.findGameById("my-id");
        game.assignHandCardToPlayers();

        assertEquals(Phase.Judgement, game.getCurrentRoundPhase());
        assertEquals(4, game.getPlayer("player-a").getHandSize());
        assertEquals(4, game.getPlayer("player-b").getHandSize());
        assertEquals(4, game.getPlayer("player-c").getHandSize());
        assertEquals(4, game.getPlayer("player-d").getHandSize());
    }

    private void shouldJudgementPhase() {
        /*
        * Given
            系統發牌完成
            輪到玩家 A 的回合
            玩家身上沒有延遲類錦囊卡

            When
            系統判定階段

            Then
            玩家 A進入摸牌階段

        * */
        // given
        Game game = inMemoryGameRepository.findGameById("my-id");
        // when
        game.judgePlayerShouldDelay();
        // then
        assertEquals(Phase.Drawing, game.getCurrentRoundPhase());
    }

    private void shouldDrawCardToPlayer() {
        Game game = inMemoryGameRepository.findGameById("my-id");
        String playerId = game.getCurrentRoundPlayer().getId();
        game.drawCardToPlayer(playerId);
        assertEquals(Phase.Action, game.getCurrentRoundPhase());
        assertEquals(6, game.getPlayer(playerId).getHandSize());
    }

    private void shouldPlayerAPlayedCard(String targetPlayerId) throws Exception {
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
        A
         */
        Game game = inMemoryGameRepository.findGameById("my-id");
        Player currentRoundPlayer = game.getCurrentRoundPlayer();
        List<HandCard> cards = currentRoundPlayer.getHand().getCards();
        String cardId = cards.stream().filter(card -> card instanceof Kill).findFirst().get().getId();

        this.mockMvc.perform(post("/api/games/my-id/player:playCard")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                { "playerId": "%s",
                                  "targetPlayerId": "%s",
                                  "cardId": "%s"
                                }""", currentRoundPlayer.getId(), targetPlayerId, cardId)))
                .andExpect(status().isOk())
                .andReturn();
        game = inMemoryGameRepository.findGameById("my-id");
        assertEquals(5, game.getPlayer("player-a").getHandSize());
        Assertions.assertTrue(Utils.compareArrayLists(Arrays.asList(new Kill(BD0088), new Kill(BD9087), new Kill(BD8086), new Kill(BC5057), new Kill(BC4056)), game.getPlayer("player-a").getHand().getCards()));
        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
    }

    private void shouldPlayerFinishAction() throws Exception {
        /*
        Given
        現在是 A 玩家的出牌階段

        When
        A 玩家結束出牌

        Then
        A 玩家進入棄牌階段
        */
        Game game = inMemoryGameRepository.findGameById("my-id");
        String currentPlayerId = game.getCurrentRoundPlayer().getId();
        this.mockMvc.perform(post("/api/games/my-id/player:finishAction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                "playerId": "%s"
                                }
                                """, currentPlayerId)))
                .andExpect(status().isOk())
                .andReturn();


        assertEquals(Phase.Discard, game.getCurrentRoundPhase());

    }

    private void shouldPlayerADiscardCard() {

    /*
       Given
           A玩家進入棄牌階段(Discard)
           A體力5
           A玩家手牌有殺x1, 閃x2, 桃x2

           When
           系統進行棄牌判斷

           Then
           不用棄牌，Ａ回合結束
           換B玩家回合
           B Phase 是判斷階段
    */

        // given
        Game game = inMemoryGameRepository.findGameById("my-id");
        // when
        game.judgePlayerShouldDiscardCard();
        // then
        assertEquals(Phase.Judgement, game.getCurrentRoundPhase());
        assertEquals("player-b", game.getCurrentRoundPlayer().getId());
    }

    private void shouldPlayerBDiscardCard() throws Exception {
    /*
       Given
           B玩家進入棄牌階段(Discard)
           B體力3
           B玩家手牌有殺x5

           When
           系統進行棄牌判斷
           B回合棄BD6084,BCJ076

           Then
           B玩家剩BC0075,BC3055,BC2054
           換C玩家回合
           C Phase 是判斷階段

    */
        // given
        Game game = inMemoryGameRepository.findGameById("my-id");
        // when
        game.judgePlayerShouldDiscardCard(); //true

        this.mockMvc.perform(post("/api/games/my-id/player:discardCards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                ["%s", "%s"]
                                """, "BD6084", "BCJ076")))
                .andExpect(status().isOk())
                .andReturn();

        // then
        assertEquals(Phase.Judgement, game.getCurrentRoundPhase());
        assertEquals("player-c", game.getCurrentRoundPlayer().getId());
        assertEquals(3, game.getPlayer("player-b").getHandSize());
        Assertions.assertTrue(Utils.compareArrayLists(Arrays.asList(new Kill(BC0075), new Kill(BC3055), new Kill(BC2054)), game.getPlayer("player-b").getHand().getCards()));
        assertEquals(2, game.getGraveyard().size());

    }
}

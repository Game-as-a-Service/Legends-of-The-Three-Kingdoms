package com.gaas.threeKingdoms.e2e;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.controller.dto.GameRequest;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.exception.NotFoundException;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.presenter.*;
import com.gaas.threeKingdoms.presenter.common.PlayerDataViewModel;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.round.RoundPhase;
import com.gaas.threeKingdoms.utils.ShuffleWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.handcard.PlayCard.values;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class GameTest extends AbstractBaseIntegrationTest {

    private WebSocketStompClient stompClient;
    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    final ConcurrentHashMap<String, BlockingQueue<String>> map = new ConcurrentHashMap<>();

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() throws Exception {
        //初始化前端 WebSocket 連線，模擬前端收到的 WebSocket 訊息
        System.out.println("GameTest port:" + port);
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        this.stompClient = new WebSocketStompClient(webSocketClient);
        this.stompClient.setMessageConverter(new StringMessageConverter());
        map.computeIfAbsent("player-a", k -> new LinkedBlockingQueue<>());
        map.computeIfAbsent("player-b", k -> new LinkedBlockingQueue<>());
        map.computeIfAbsent("player-c", k -> new LinkedBlockingQueue<>());
        map.computeIfAbsent("player-d", k -> new LinkedBlockingQueue<>());
        setupClientSubscribe("my-id", "player-a");
        setupClientSubscribe("my-id", "player-b");
        setupClientSubscribe("my-id", "player-c");
        setupClientSubscribe("my-id", "player-d");
        Thread.sleep(1000);
    }

    private void setupClientSubscribe(String gameId, String playerId) throws Exception {
        final AtomicReference<Throwable> failure = new AtomicReference<>(); // 創建一個原子型的引用變量，用於存放發生的異常

        StompSessionHandler handler = new TestSessionHandler(failure) {
            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                throw new RuntimeException("Failure in WebSocket handling", exception);
            }

            @Override
            public void afterConnected(final StompSession session, StompHeaders connectedHeaders) {
                session.subscribe(String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", gameId, playerId), new StompFrameHandler() {  // 訂閱伺服器的 "/websocket/legendsOfTheThreeKingdoms/gameId/playerId" 路徑的訊息
                    @Override
                    public Type getPayloadType(StompHeaders headers) {  // 定義從伺服器收到的訊息內容的類型
                        return String.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        try {
                            map.computeIfAbsent(playerId, k -> new LinkedBlockingQueue<>()).add((String) payload);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        this.stompClient.connectAsync("ws://localhost:{port}/legendsOfTheThreeKingdoms", this.headers, handler, this.port);
    }

    @Test
    public void happyPath() throws Exception {
        // initial game
        shouldCreateGame();

        shouldChooseGeneralsByMonarch();

        shouldGetGeneralCardsByOthers();

        shouldChooseGeneralsByOthers();

        shouldGetInitialEndGameStatus();

        // Round 1 hp-0 playerA 君主本人
        playerATakeTurnRound1();

        // Round 2 hp-1 playerB 攻擊 君主
        playerBTakeTurnRound2();

        // Round 3 hp-0 playerC 距離太遠 無法攻擊 直接棄牌
        playerCTakeTurnRound3();

        // Round 4 hp-1 playerD
        playerDTakeTurnRound4();

        // Round 5 hp-0 playerA
        playerATakeTurnRound5();

        // Round 6 hp-1 playerB
        playerBTakeTurnRound6();

        // ROUND 7 hp-0 playerC 距離太遠 無法攻擊 直接棄牌
        playerCTakeTurnRound7();

        // Round 8 hp-1 playerD
        playerDTakeTurnRound8();

        // Round 9 hp-0 playerA 君主
        playerATakeTurnRound9();

        // Round 10 hp-1 playerB
        playerBTakeTurnRound10();

        // player A 不出桃
        playerAPlayedCardSkip();

        // player B 不出桃
        playerBPlayedCardSkip();

        // player C 不出桃
        playerCPlayedCardSkip();

        // player D 不出桃
        // 遊戲結束
        playerDPlayedCardSkip();

    }


    public void shouldCreateGame() throws Exception {

        // Monarch
        // Minister
        // Rebel
        // Traitors

        String requestBody = objectMapper.writeValueAsString(
                new GameRequest(gameId, List.of("player-a", "player-b", "player-c", "player-d")));


        try (MockedStatic<ShuffleWrapper> mockedStatic = Mockito.mockStatic(ShuffleWrapper.class)) {
            mockedStatic.when(() -> ShuffleWrapper.shuffle(Mockito.anyList()))
                    .thenAnswer(invocation -> null);

            this.mockMvc.perform(post("/api/games")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk());
        }

        Game game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
        Stack<HandCard> stack = new Stack<>();
        Arrays.stream(values())
                .filter(x -> x.getCardName().equals("閃"))
                .map(Dodge::new)
                .forEach(stack::push);
        Arrays.stream(values())
                .filter(x -> x.getCardName().equals("殺"))
                .map(Kill::new)
                .forEach(stack::push);
        game.setDeck(new Deck(stack));
        repository.save(game);

        assertEquals(Role.MONARCH, game.getPlayer("player-a").getRoleCard().getRole());
        assertEquals(Role.MINISTER, game.getPlayer("player-b").getRoleCard().getRole());
        assertEquals(Role.REBEL, game.getPlayer("player-c").getRoleCard().getRole());
        assertEquals(Role.TRAITOR, game.getPlayer("player-d").getRoleCard().getRole());

        // WebSocket 推播給前端資訊 (主公)
        checkPlayerAGetCreateGameEvent();

        String playerBGeneralEvent = map.get("player-b").poll(5, TimeUnit.SECONDS);
        assertNotNull(playerBGeneralEvent);
        CreateGamePresenter.CreateGameViewModel generalCardViewModelB = objectMapper.readValue(playerBGeneralEvent, CreateGamePresenter.CreateGameViewModel.class);
        assertNotNull(generalCardViewModelB);
        assertEquals("請等待主公選擇武將", generalCardViewModelB.getMessage());

        String playerCGeneralEvent = map.get("player-c").poll(5, TimeUnit.SECONDS);
        assertNotNull(playerCGeneralEvent);
        CreateGamePresenter.CreateGameViewModel generalCardViewModelC = objectMapper.readValue(playerCGeneralEvent, CreateGamePresenter.CreateGameViewModel.class);
        assertNotNull(generalCardViewModelC);
        assertEquals("請等待主公選擇武將", generalCardViewModelC.getMessage());

        String playerDGeneralEvent = map.get("player-d").poll(5, TimeUnit.SECONDS);
        assertNotNull(playerDGeneralEvent);
        CreateGamePresenter.CreateGameViewModel generalCardViewModelD = objectMapper.readValue(playerDGeneralEvent, CreateGamePresenter.CreateGameViewModel.class);
        assertNotNull(generalCardViewModelD);
        assertEquals("請等待主公選擇武將", generalCardViewModelD.getMessage());

        // find the game
        this.mockMvc.perform(get("/api/games/my-id?playerId=player-a")).andDo(print())
                .andExpect(status().isOk());

        // WebSocket 推播給前端資訊
        checkGetGameEvent();
    }

    private void checkPlayerAGetCreateGameEvent() throws InterruptedException, JsonProcessingException {
        // We expect two messages for player-a, but the order is not guaranteed.
        // We'll poll twice and check the event type to decide how to process each message.
        boolean createGameEventReceived = false;
        boolean getGeneralCardEventReceived = false;

        for (int i = 0; i < 2; i++) {
            String messageJson = map.get("player-a").poll(5, TimeUnit.SECONDS);
            assertNotNull(messageJson, "Expected to receive a message for player-a, but queue was empty.");

            // First, parse the message as a generic JsonNode to check the event type
            // without causing a deserialization error.
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(messageJson);
            String eventType = rootNode.get("event").asText();

            if ("createGameEvent".equals(eventType)) {
                // Now that we know it's a createGameEvent, deserialize it to the correct class
                CreateGamePresenter.CreateGameViewModel createGameViewModelOfMonarch = objectMapper.readValue(messageJson, CreateGamePresenter.CreateGameViewModel.class);

                ArrayList<CreateGamePresenter.SeatViewModel> seats = new ArrayList<>();
                seats.add(new CreateGamePresenter.SeatViewModel("player-a", "MONARCH"));
                seats.add(new CreateGamePresenter.SeatViewModel("player-b", ""));
                seats.add(new CreateGamePresenter.SeatViewModel("player-c", ""));
                seats.add(new CreateGamePresenter.SeatViewModel("player-d", ""));

                assertNotNull(createGameViewModelOfMonarch);
                assertEquals("my-id", createGameViewModelOfMonarch.getGameId());
                assertEquals("createGameEvent", createGameViewModelOfMonarch.getEvent());
                assertEquals("請選擇武將", createGameViewModelOfMonarch.getMessage());
                assertEquals(seats, createGameViewModelOfMonarch.getData().getSeats());

                createGameEventReceived = true;

            } else if ("getGeneralCardEvent".equals(eventType)) {
                // It's a getGeneralCardEvent, so deserialize it to its corresponding class
                GetGeneralCardPresenter.GetGeneralCardViewModel monarchGetGeneralViewModel = objectMapper.readValue(messageJson, GetGeneralCardPresenter.GetGeneralCardViewModel.class);

                assertNotNull(monarchGetGeneralViewModel);
                assertEquals("WU001", monarchGetGeneralViewModel.getData().get(0));
                assertEquals("WEI001", monarchGetGeneralViewModel.getData().get(1));
                assertEquals("SHU001", monarchGetGeneralViewModel.getData().get(2));
                assertEquals("SHU002", monarchGetGeneralViewModel.getData().get(3));
                assertEquals("SHU003", monarchGetGeneralViewModel.getData().get(4));
                assertEquals(5, monarchGetGeneralViewModel.getData().size());

                getGeneralCardEventReceived = true;
            }
        }

        // Finally, assert that both expected events were received and processed.
        assertTrue(createGameEventReceived, "The createGameEvent was not received.");
        assertTrue(getGeneralCardEventReceived, "The getGeneralCardEvent was not received.");
    }


    private void checkGetGameEvent() throws InterruptedException, JsonProcessingException {
        String findGameViewModelMessage = map.get("player-a").poll(5, TimeUnit.SECONDS);
        FindGamePresenter.FindGameViewModel findGameViewModel = objectMapper.readValue(findGameViewModelMessage, FindGamePresenter.FindGameViewModel.class);
        assertNotNull(findGameViewModelMessage);
        assertEquals("", findGameViewModel.getMessage());
        assertEquals("player-a", findGameViewModel.getData().getSeats().get(0).getId());
        assertEquals("player-b", findGameViewModel.getData().getSeats().get(1).getId());
        assertEquals("player-c", findGameViewModel.getData().getSeats().get(2).getId());
        assertEquals("player-d", findGameViewModel.getData().getSeats().get(3).getId());
    }

    private void shouldChooseGeneralsByMonarch() throws Exception {
        /*
        websocket推播主公拿到可以選的五張武將牌

          選一張 // post api
          牌堆減少剛剛抽出的牌
          主公身上有武將牌

          Happy Path
          玩家總共4人

          When 玩家A選劉備

         拿到可以選的武將牌
         */

        this.mockMvc.perform(post("/api/games/my-id/player:monarchChooseGeneral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                 "playerId": "player-a",
                                 "generalId": "SHU001"
                                 }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        // Then 玩家A武將為劉備 ((主公general是 SHU001 is true)
        Game game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
        assertEquals("SHU001", game.getPlayer("player-a").getGeneralCard().getGeneralId());
        // 牌堆不能有 SHU001
        assertEquals(0, game.getGeneralCardDeck().getGeneralStack()
                .stream().filter(x -> x.getGeneralId().equals("SHU001"))
                .count());

        // WebSocket 推播給前端資訊
        // 主公選擇的腳色全部人都可以知道

        // 所有玩家都會先收到 MonarchChooseGeneralCardEvent
        for (Player player : game.getPlayers()) {
            String monarchChooseGeneralCardMessage = map.get(player.getId()).poll(5, TimeUnit.SECONDS);
            MonarchChooseGeneralCardPresenter.MonarchChooseGeneralCardViewModel monarchChooseGeneralCardViewModel = objectMapper.readValue(monarchChooseGeneralCardMessage, MonarchChooseGeneralCardPresenter.MonarchChooseGeneralCardViewModel.class);
            assertNotNull(monarchChooseGeneralCardMessage);
            assertEquals("主公已選擇 劉備", monarchChooseGeneralCardViewModel.getMessage());
            assertEquals("SHU001", monarchChooseGeneralCardViewModel.getData().getMonarchGeneralCard());
            assertEquals("MonarchGeneralChosenEvent", monarchChooseGeneralCardViewModel.getEvent());
        }

        // 非主公玩家會再收到 GetGeneralCardByOthersEvent
        for (Player player : game.getPlayers()) {
            if (!player.getRoleCard().getRole().equals(Role.MONARCH)) {
                String getGeneralCardByOthersMessage = map.get(player.getId()).poll(5, TimeUnit.SECONDS);
                MonarchChooseGeneralCardPresenter.GetGeneralCardByOthersViewModel getGeneralCardByOthersViewModel = objectMapper.readValue(getGeneralCardByOthersMessage, MonarchChooseGeneralCardPresenter.GetGeneralCardByOthersViewModel.class);
                assertNotNull(getGeneralCardByOthersMessage);
                assertEquals("請選擇武將", getGeneralCardByOthersViewModel.getMessage());
                assertEquals(3, getGeneralCardByOthersViewModel.getData().size());
                assertEquals("getGeneralCardEventByOthers", getGeneralCardByOthersViewModel.getEvent());
            }
        }

        // PlayerB打主公選擇角色的API
        // 期望400 Bad Request
        this.mockMvc.perform(post("/api/games/my-id/player:monarchChooseGeneral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                 "playerId": "player-b",
                                 "generalId": "SHU001"
                                 }
                                """))
                .andExpect(status().is4xxClientError())
                .andReturn();


    }

    private void shouldGetGeneralCardsByOthers() throws Exception {
        // GetGeneralCardByOthersEvent 已經在 shouldChooseGeneralsByMonarch() 中處理過了
        // 這個方法現在不需要額外的邏輯
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
      */
        this.mockMvc.perform(post("/api/games/my-id/player:otherChooseGeneral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                 "playerId": "player-b",
                                 "generalId": "SHU006"
                                 }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        // Then 玩家B武將為馬超 ((玩家B general是 general1 is true)
        Game game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
        assertEquals("SHU006", game.getPlayer("player-b").getGeneralCard().getGeneralId());
        // 牌堆不能有 general1
        assertEquals(0, game.getGeneralCardDeck().getGeneralStack()
                .stream().filter(x -> x.getGeneralId().equals("SHU006"))
                .count());

        // 玩家C選諸葛亮
        this.mockMvc.perform(post("/api/games/my-id/player:otherChooseGeneral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                 "playerId": "player-c",
                                 "generalId": "SHU004"
                                 }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
        // Then 玩家C武將為諸葛亮 ((玩家C genera1是 general18 is true)
        assertEquals("SHU004", game.getPlayer("player-c").getGeneralCard().getGeneralId());
        // 牌堆不能有 general1
        assertEquals(0, game.getGeneralCardDeck().getGeneralStack()
                .stream().filter(x -> x.getGeneralId().equals("SHU004"))
                .count());

        // 玩家D選司馬懿
        this.mockMvc.perform(post("/api/games/my-id/player:otherChooseGeneral")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                 "playerId": "player-d",
                                 "generalId": "WEI002"
                                 }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));

        // Then 玩家D武將為司馬懿 ((玩家D general是 general1 is true)
        assertEquals("WEI002", game.getPlayer("player-d").getGeneralCard().getGeneralId());
        // 牌堆不能有 general1
        assertEquals(0, game.getGeneralCardDeck().getGeneralStack()
                .stream().filter(x -> x.getGeneralId().equals("WEI002"))
                .count());
    }

    // 推播 所有玩家的武將 && 手牌

    private void shouldGetInitialEndGameStatus() throws InterruptedException, JsonProcessingException {
        List<List<String>> allRolesList = getDumpRoleLists();
        List<Integer> hps = List.of(5, 4, 3, 3);
        List<String> generals = List.of("SHU001", "SHU006", "SHU004", "WEI002");

        Game game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player currentPlayer = game.getPlayers().get(i);
            String initialEndViewModelMessageT = map.get(currentPlayer.getId()).poll(5, TimeUnit.SECONDS);

            assertNotNull(initialEndViewModelMessageT);
            InitialEndPresenter.InitialEndViewModel initialEndViewModel = objectMapper.readValue(initialEndViewModelMessageT, InitialEndPresenter.InitialEndViewModel.class);
            var data = initialEndViewModel.getData();
            var round = data.getRound();
            List<PlayerDataViewModel> seats = data.getSeats();

            // 檢查 game phase
            assertNotNull(initialEndViewModel);
            assertEquals("Normal", data.getGamePhase());
            assertEquals("Judgement", round.getRoundPhase());
            assertEquals(currentPlayer.getId(), initialEndViewModel.getPlayerId());
            assertEquals("player-a", round.getCurrentRoundPlayer());
            assertEquals("", round.getDyingPlayer());

            // 檢查 player 狀態

            assertEquals(4, seats.size());
            //每個人都可以看到主公的 Role
            assertEquals(Role.MONARCH.getRoleName(), seats.get(0).getRoleId());

            // 其他玩家知道主公是誰, 主公不知道其他玩家的 role
            // 玩家之間不知道其他玩家的 role
            assertEquals(allRolesList.get(i), seats.stream()
                    .map(PlayerDataViewModel::getRoleId)
                    .collect(Collectors.toList()));

            // generals
            checkEveryPlayerPublicInformation(currentPlayer, game, seats, generals, hps);
        }
    }

    private static List<List<String>> getDumpRoleLists() {
        List<String> playerARolesList = List.of(Role.MONARCH.getRoleName(), "", "", "");
        List<String> playerBRolesList = List.of(Role.MONARCH.getRoleName(), Role.MINISTER.getRoleName(), "", "");
        List<String> playerCRolesList = List.of(Role.MONARCH.getRoleName(), "", Role.REBEL.getRoleName(), "");
        List<String> playerDRolesList = List.of(Role.MONARCH.getRoleName(), "", "", Role.TRAITOR.getRoleName());

        List<List<String>> allRolesList = new ArrayList<>();
        allRolesList.add(playerARolesList);
        allRolesList.add(playerBRolesList);
        allRolesList.add(playerCRolesList);
        allRolesList.add(playerDRolesList);
        return allRolesList;
    }

    private static void checkEveryPlayerPublicInformation(Player currentPlayer, Game game, List<PlayerDataViewModel> seats, List<String> generals, List<Integer> hps) {
        for (int j = 0; j < game.getPlayers().size(); j++) {
            Player otherPlayer = game.getPlayers().get(j);

            var playerDataViewModel = seats.get(j);
            assertEquals(otherPlayer.getId(), playerDataViewModel.getId());
            assertEquals(4, playerDataViewModel.getHand().getSize());

            assertEquals(generals.get(j), playerDataViewModel.getGeneralId());
            assertEquals(hps.get(j), playerDataViewModel.getHp());

            if (!currentPlayer.equals(otherPlayer)) {
                // 看不到其他玩家的手牌
                assertEquals(0, playerDataViewModel.getHand().getCardIds().size());
            }
        }
    }

    private void playerATakeTurnRound1() throws Exception {
        shouldGetRoundStartStatus();
        shouldPlayerAPlayedCardRound1("player-b");
        shouldPlayerBSkipPlayCardRound1();
        shouldPlayerFinishActionRound1();
    }

    private void shouldPlayerBSkipPlayCardRound1() throws Exception {
        /*
        * Given
            玩家 A 的回合，對B出殺
            玩家身上沒有延遲類錦囊卡

            When
            B跳過出牌(skip)

            Then
            B血量 4 -> 3

        * */

        playCard("player-b", "player-a", "", "skip")
                .andExpect(status().isOk()).andReturn();
        String playerBSkipJsonForA = map.get("player-a").poll(5, TimeUnit.SECONDS);
        Path path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/PlayCard/round_playcard_player_b_skip_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBSkipJsonForA);

        String playerBSkipJsonForB = map.get("player-b").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/PlayCard/round_playcard_player_b_skip_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBSkipJsonForB);

        String playerBSkipJsonForC = map.get("player-c").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/PlayCard/round_playcard_player_b_skip_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBSkipJsonForC);

        String playerBSkipJsonForD = map.get("player-d").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/PlayCard/round_playcard_player_b_skip_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBSkipJsonForD);

    }

    // 玩家 A 抽牌結束後推播發生的 domain event

    private void shouldGetRoundStartStatus() throws InterruptedException, IOException {
        String actualJson = map.get("player-a").poll(5, TimeUnit.SECONDS);
        Path path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/RoundStart/round_start_monarch_player_a.json");
        String expectedJson = Files.readString(path);
        assertNotNull(expectedJson);
        assertEquals(expectedJson, actualJson);

        actualJson = map.get("player-b").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/RoundStart/round_start_player_b.json");
        expectedJson = Files.readString(path);
        assertNotNull(expectedJson);
        assertEquals(expectedJson, actualJson);

        actualJson = map.get("player-c").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/RoundStart/round_start_player_c.json");
        expectedJson = Files.readString(path);
        assertNotNull(expectedJson);
        assertEquals(expectedJson, actualJson);

        actualJson = map.get("player-d").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/RoundStart/round_start_player_d.json");
        expectedJson = Files.readString(path);
        assertNotNull(expectedJson);
        assertEquals(expectedJson, actualJson);
    }

    private void shouldPlayerAPlayedCardRound1(String targetPlayerId) throws Exception {
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

        String currentPlayer = "player-a";
        String playedCardId = "BDK091";

        playCard(currentPlayer, targetPlayerId, playedCardId, "active")
                .andExpect(status().isOk()).andReturn();

        String playerAGetPlayerAPlayCardJson = getJsonByPlayerId("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/PlayCard/round_playcard_monarch_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAGetPlayerAPlayCardJson);

        String playerBGetPlayerBPlayCardJson = getJsonByPlayerId("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/PlayCard/round_playcard_monarch_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBGetPlayerBPlayCardJson);

        String playerCGetPlayerCPlayCardJson = getJsonByPlayerId("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/PlayCard/round_playcard_monarch_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerCGetPlayerCPlayCardJson);

        String playerDGetPlayerDPlayCardJson = getJsonByPlayerId("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/PlayCard/round_playcard_monarch_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerDGetPlayerDPlayCardJson);

        playCard(currentPlayer, targetPlayerId, "BD7085", "active")
                .andExpect(status().is4xxClientError())
                .andReturn();

    }

    private String getJsonByPlayerId(String playerId) throws InterruptedException {
        return map.get(playerId).poll(5, TimeUnit.SECONDS);
    }

    private void shouldDrawCardToPlayer(int expectHandSize) {
        Game game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
        String playerId = game.getCurrentRoundPlayer().getId();
        assertEquals(RoundPhase.Action, game.getCurrentRoundPhase());
        assertEquals(expectHandSize, game.getPlayer(playerId).getHandSize());
    }


    private void shouldPlayerFinishActionRound1() throws Exception {
        /*
        Given
        現在是 A 玩家的出牌階段

        When
        A 玩家結束出牌

        Then
        A 玩家進入棄牌階段
        */
        Game game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
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

        String playerAFinishActionForA = map.get("player-a").poll(5, TimeUnit.SECONDS);
        Path path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/FinishAction/round_finishaction_player_a_to_drawcard_player_b_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAFinishActionForA);

        String playerAFinishActionForB = map.get("player-b").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/FinishAction/round_finishaction_player_a_to_drawcard_player_b_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAFinishActionForB);

        String playerAFinishActionForC = map.get("player-c").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/FinishAction/round_finishaction_player_a_to_drawcard_player_b_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAFinishActionForC);

        String playerAFinishActionForD = map.get("player-d").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round1/FinishAction/round_finishaction_player_a_to_drawcard_player_b_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAFinishActionForD);


    }

    private void playerBTakeTurnRound2() throws Exception {
        shouldDrawCardToPlayer(6);
        shouldPlayerBPlayedCard("player-a");
        shouldPlayerASkipPlayCardRound2();
        shouldPlayerFinishActionRound2();
        shouldPlayerBDiscardCardRound2();
    }

    private void shouldPlayerFinishActionRound2() throws Exception {

                /*
        Given
        現在是 B 玩家的出牌階段

        When
        B 玩家結束出牌

        Then
        B 玩家進入棄牌階段
        */
        Game game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
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

        String playerBFinishActionForA = map.get("player-a").poll(5, TimeUnit.SECONDS);
        Path path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/FinishAction/round_finishaction_player_b_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBFinishActionForA);

        String playerBFinishActionForB = map.get("player-b").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/FinishAction/round_finishaction_player_b_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBFinishActionForB);

        String playerBFinishActionForC = map.get("player-c").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/FinishAction/round_finishaction_player_b_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBFinishActionForC);

        String playerBFinishActionForD = map.get("player-d").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/FinishAction/round_finishaction_player_b_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBFinishActionForD);


    }

    private void shouldPlayerASkipPlayCardRound2() throws Exception {
        /*
         * Given
            玩家 B 的回合，對A出殺
            玩家身上沒有延遲類錦囊卡

            When
            A跳過出牌(skip)

            Then
            A血量 5 -> 4

        * */

        playCard("player-a", "player-b", "", "skip")
                .andExpect(status().isOk()).andReturn();

        String playerASkipJsonForA = map.get("player-a").poll(5, TimeUnit.SECONDS);
        Path path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/PlayCard/round_playcard_player_a_skip_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerASkipJsonForA);

        String playerASkipJsonForB = map.get("player-b").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/PlayCard/round_playcard_player_a_skip_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerASkipJsonForB);

        String playerASkipJsonForC = map.get("player-c").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/PlayCard/round_playcard_player_a_skip_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerASkipJsonForC);

        String playerASkipJsonForD = map.get("player-d").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/PlayCard/round_playcard_player_a_skip_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerASkipJsonForD);
    }

    private void shouldPlayerBPlayedCard(String targetPlayerId) throws Exception {
        /*
        Given
        輪到 B 玩家出牌
        B 玩家手牌有殺x2, 閃x2, 桃x2
        A 玩家在 B 玩家的攻擊距離

        When
        B 玩家對 A 玩家出殺 (BD7085)

        Then
        B 玩家出殺成功
        A 玩家hp = 3
        B 玩家手牌剩五張
        */

        playCard("player-b", targetPlayerId, "BD7085", "active")
                .andExpect(status().isOk()).andReturn();

        String playCardJson = map.get("player-a").poll(5, TimeUnit.SECONDS);
        Path path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/PlayCard/round_playcard_player_b_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJson);

        String playerBGetPlayerBPlayCardJson = getJsonByPlayerId("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/PlayCard/round_playcard_player_b_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBGetPlayerBPlayCardJson);

        String playerCGetPlayerCPlayCardJson = getJsonByPlayerId("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/PlayCard/round_playcard_player_b_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerCGetPlayerCPlayCardJson);

        String playerDGetPlayerDPlayCardJson = getJsonByPlayerId("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/PlayCard/round_playcard_player_b_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerDGetPlayerDPlayCardJson);

    }


    private void shouldPlayerBDiscardCardRound2() throws Exception {
    /*
       Given
       B玩家進入棄牌階段(Discard)
       B體力3
       B玩家手牌有殺x5
       "BD6084", "BCJ076", "BC0075", "BC3055", "BC2054"

       When
       系統進行棄牌判斷
       B回合棄BD6084,BCJ076

       Then
       B玩家剩BC0075,BC3055,BC2054
       換C玩家回合
       C Phase 是判斷階段
    */

        this.mockMvc.perform(post("/api/games/my-id/player:discardCards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                ["%s", "%s"]
                                """, "BD6084", "BCJ076")))
                .andExpect(status().isOk())
                .andReturn();

        String playCardJson = map.get("player-a").poll(5, TimeUnit.SECONDS);
        Path path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/DiscardCard/round_discard_player_b_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJson);

        String playerBGetPlayerBPlayCardJson = getJsonByPlayerId("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/DiscardCard/round_discard_player_b_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBGetPlayerBPlayCardJson);

        String playerCGetPlayerCPlayCardJson = getJsonByPlayerId("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/DiscardCard/round_discard_player_b_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerCGetPlayerCPlayCardJson);

        String playerDGetPlayerDPlayCardJson = getJsonByPlayerId("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round2/DiscardCard/round_discard_player_b_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerDGetPlayerDPlayCardJson);

    }

    private void playerCTakeTurnRound3() throws Exception {
        shouldDrawCardToPlayer(6);
        shouldPlayerFinishAction();
        shouldPlayerCDiscardCardRound3();
    }

    private void shouldPlayerFinishAction() throws Exception {
                        /*
        Given
        現在是 C 玩家的出牌階段

        When
        C 玩家結束出牌

        Then
        C 玩家進入棄牌階段
        */
        Game game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
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

        map.get("player-a").poll(5, TimeUnit.SECONDS);
        map.get("player-b").poll(5, TimeUnit.SECONDS);
        map.get("player-c").poll(5, TimeUnit.SECONDS);
        map.get("player-d").poll(5, TimeUnit.SECONDS);
    }

    private void shouldPlayerCDiscardCardRound3() throws Exception {
    /*
       Given
       C玩家進入棄牌階段(Discard)
       C體力3
       C玩家手牌有殺x6
       "BC9074", "BC8073", "BCJ063", "BC0062", "BH0049", "BHJ037"

       When
       系統進行棄牌判斷
       C 棄"BC9074", "BC8073", "BCJ063"

       Then
       C玩家剩"BC0062", "BH0049", "BHJ037"
       換D玩家回合
       D Phase 是判斷階段
    */

        this.mockMvc.perform(post("/api/games/my-id/player:discardCards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                ["%s", "%s", "%s"]
                                """, "BC9074", "BH0049", "BCJ063")))
                .andExpect(status().isOk())
                .andReturn();

        map.get("player-a").poll(5, TimeUnit.SECONDS);
        map.get("player-b").poll(5, TimeUnit.SECONDS);
        map.get("player-c").poll(5, TimeUnit.SECONDS);
        map.get("player-d").poll(5, TimeUnit.SECONDS);
    }

    private void playerDTakeTurnRound4() throws Exception {
        currentPlayerPlayedCardToTargetPlayer("player-d", "player-a", "BC9061");
        currentPlayerSkipToTargetPlayer("player-a", "player-d");
        shouldPlayerFinishAction();
        playerDiscardCard(List.of("\"BH0036\"", "\"BS0023\"").toArray(new String[0]));
    }

    private void playerDiscardCard(String[] cardIds) throws Exception {
        this.mockMvc.perform(post("/api/games/my-id/player:discardCards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Arrays.toString(cardIds)))
                .andExpect(status().isOk())
                .andReturn();

        // 推播
        map.get("player-a").poll(5, TimeUnit.SECONDS);
        map.get("player-b").poll(5, TimeUnit.SECONDS);
        map.get("player-c").poll(5, TimeUnit.SECONDS);
        map.get("player-d").poll(5, TimeUnit.SECONDS);
    }


    private void currentPlayerPlayedCardToTargetPlayer(String currentPlayer, String targetPlayerId, String cardId) throws Exception {
        /*
        Given
        輪到 D 玩家出牌
        D 玩家手牌有殺x2, 閃x2, 桃x2
        A 玩家在 B 玩家的攻擊距離
        A 玩家hp = 5

        When
        D 玩家對 A 玩家出殺 (BC9061)

        Then
        D 玩家出殺成功
        A 玩家hp = 4
        D 玩家手牌剩五張
        */

        playCard(currentPlayer, targetPlayerId, cardId, "active")
                .andExpect(status().isOk()).andReturn();

        String playCardJson = map.get("player-a").poll(5, TimeUnit.SECONDS);

        String playerBGetPlayerBPlayCardJson = getJsonByPlayerId("player-b");

        String playerCGetPlayerCPlayCardJson = getJsonByPlayerId("player-c");

        String playerDGetPlayerDPlayCardJson = getJsonByPlayerId("player-d");

    }

    private void currentPlayerSkipToTargetPlayer(String currentPlayer, String targetPlayerId) throws Exception {
        /*
         * Given
            玩家 B 的回合，對A出殺
            玩家身上沒有延遲類錦囊卡

            When
            A跳過出牌(skip)

            Then
            A血量 - 1

        * */

        playCard(currentPlayer, targetPlayerId, "", "skip")
                .andExpect(status().isOk()).andReturn();

        map.get("player-a").poll(5, TimeUnit.SECONDS);
        map.get("player-b").poll(5, TimeUnit.SECONDS);
        map.get("player-c").poll(5, TimeUnit.SECONDS);
        map.get("player-d").poll(5, TimeUnit.SECONDS);
    }

    private void shouldPlayerDiscardCard() throws Exception {
       /*
       Given
           D 玩家進入棄牌階段(Discard)
           D 體力3
           D 玩家手牌有殺x5

           When
           系統進行棄牌判斷
           D 回合棄 2 張

           Then
           D 玩家剩 3 張手牌
           換A玩家回合
           A Phase 是判斷階段
        */

        // given
        Game game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
        // when 因為 c 不重要直接隨便丟牌就好
//        game.getPlayerDiscardCount(); //true
        List<HandCard> cards = game.getPlayer("player-d").getHand().getCards();
        List<String> ids = cards.stream()
                .skip(Math.max(0, cards.size() - 2))
                .map(HandCard::getId)
                .toList();
        this.mockMvc.perform(post("/api/games/my-id/player:discardCards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                ["%s", "%s"]
                                """, ids.get(0), ids.get(1))))
                .andExpect(status().isOk())
                .andReturn();

        // then
        assertEquals(3, game.getPlayer("player-d").getHandSize());
        assertEquals(RoundPhase.Judgement, game.getCurrentRoundPhase());
        assertEquals("player-a", game.getCurrentRoundPlayer().getId());
        assertEquals(10, game.getGraveyard().size());
    }

    private void playerATakeTurnRound5() throws Exception {
        shouldPlayerFinishAction();
        playerDiscardCard(List.of("\"BD0088\"", "\"BD9087\"", "\"BD8086\"", "\"BC5057\"").toArray(new String[0]));
    }

    private void shouldPlayerADiscardCardRound5() throws Exception {
    /*
       Given
           A 玩家進入棄牌階段(Discard)
           A 體力3
           A 玩家手牌有殺x7

           When
           系統進行棄牌判斷
           A 回合棄前 4 張

           Then
           A 玩家剩 3 張手牌
           換 B 玩家回合
           B Phase 是判斷階段
    */
        // given
        Game game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
        // when 因為這邊不重要直接隨便丟牌就好
//        game.getPlayerDiscardCount(); //true
        List<HandCard> cards = game.getPlayer("player-a").getHand().getCards();
        List<String> ids = cards.stream()
                .skip(Math.max(0, cards.size() - 4))
                .map(HandCard::getId)
                .map(id -> "\"" + id + "\"")
                .toList();
        this.mockMvc.perform(post("/api/games/my-id/player:discardCards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ids.toString()))
                .andExpect(status().isOk())
                .andReturn();

        // then
        assertEquals(3, game.getPlayer("player-a").getHandSize());
        assertEquals(RoundPhase.Judgement, game.getCurrentRoundPhase());
        assertEquals("player-b", game.getCurrentRoundPlayer().getId());
        assertEquals(14, game.getGraveyard().size());
    }

    private void playerBTakeTurnRound6() throws Exception {
        currentPlayerPlayedCardToTargetPlayer("player-b", "player-a", "BC0075");
        currentPlayerSkipToTargetPlayer("player-a", "player-b");
        shouldPlayerFinishAction();
        playerDiscardCard(List.of("\"BS8010\"").toArray(new String[0]));
    }

    private void shouldPlayerBDiscardCardRound6() throws Exception {
        /*
       Given
           B 玩家進入棄牌階段(Discard)
           B 體力3
           B 玩家手牌有殺x4

           When
           系統進行棄牌判斷
           B 回合棄前 1 張

           Then
           B 玩家剩 3 張手牌
           換 C 玩家回合
           C Phase 是判斷階段
    */
        // given
        Game game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
        // when 因為這邊不重要直接隨便丟牌就好
//        game.getPlayerDiscardCount(); //true
        List<HandCard> cards = game.getPlayer("player-b").getHand().getCards();
        List<String> ids = cards.stream()
                .skip(Math.max(0, cards.size() - 1))
                .map(HandCard::getId)
                .map(id -> "\"" + id + "\"")
                .toList();
        this.mockMvc.perform(post("/api/games/my-id/player:discardCards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ids.toString()))
                .andExpect(status().isOk())
                .andReturn();

        // then
        assertEquals(3, game.getPlayer("player-b").getHandSize());
        assertEquals(RoundPhase.Judgement, game.getCurrentRoundPhase());
        assertEquals("player-c", game.getCurrentRoundPlayer().getId());
        assertEquals(16, game.getGraveyard().size());
    }

    private void playerCTakeTurnRound7() throws Exception {
        shouldPlayerFinishAction();
        playerDiscardCard(List.of("\"BS8009\"", "\"BS8008\"").toArray(new String[0]));
    }

    private void shouldPlayerCDiscardCardRound7() throws Exception {
            /*
           Given
               C 玩家進入棄牌階段(Discard)
               C 體力3
               C 玩家手牌有殺x5

               When
               系統進行棄牌判斷
               C 回合棄前 2 張

               Then
               C玩家剩 3 張手牌
               換D玩家回合
               D Phase 是判斷階段
            */
        // given
        Game game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
        // when 因為 c 不重要直接隨便丟牌就好
//        game.getPlayerDiscardCount(); //true
        List<HandCard> cards = game.getPlayer("player-c").getHand().getCards();
        List<String> ids = cards.stream()
                .skip(Math.max(0, cards.size() - 2))
                .map(HandCard::getId)
                .map(id -> "\"" + id + "\"")
                .toList();
        this.mockMvc.perform(post("/api/games/my-id/player:discardCards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ids.toString()))
                .andExpect(status().isOk())
                .andReturn();

        // then
        assertEquals(3, game.getPlayer("player-c").getHandSize());
        assertEquals(RoundPhase.Judgement, game.getCurrentRoundPhase());
        assertEquals("player-d", game.getCurrentRoundPlayer().getId());
        assertEquals(18, game.getGraveyard().size());
    }

    private void playerDTakeTurnRound8() throws Exception {
        currentPlayerPlayedCardToTargetPlayer("player-d", "player-a", "BC8060");
        currentPlayerSkipToTargetPlayer("player-a", "player-d");
        shouldPlayerFinishAction();
        playerDiscardCard(List.of("\"BD0101\"").toArray(new String[0]));
    }

    private void shouldPlayerDDiscardCardRound8() throws Exception {
       /*
       Given
           D 玩家進入棄牌階段(Discard)
           D 體力3
           D 玩家手牌有殺x4

           When
           系統進行棄牌判斷
           D 回合棄 1 張

           Then
           D 玩家剩 3 張手牌
           換A玩家回合
           A Phase 是判斷階段
        */

        // given
        Game game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
        // when 因為 c 不重要直接隨便丟牌就好
//        game.getPlayerDiscardCount(); //true
        List<HandCard> cards = game.getPlayer("player-d").getHand().getCards();
        List<String> ids = cards.stream()
                .skip(Math.max(0, cards.size() - 1))
                .map(HandCard::getId)
                .toList();
        this.mockMvc.perform(post("/api/games/my-id/player:discardCards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                ["%s"]
                                """, ids.get(0))))
                .andExpect(status().isOk())
                .andReturn();

        // then
        assertEquals(3, game.getPlayer("player-d").getHandSize());
        assertEquals(RoundPhase.Judgement, game.getCurrentRoundPhase());
        assertEquals("player-a", game.getCurrentRoundPlayer().getId());
        assertEquals(20, game.getGraveyard().size());
    }

    private void playerATakeTurnRound9() throws Exception {
        shouldPlayerFinishAction();
        playerDiscardCard(List.of("\"BC4056\"", "\"BS9022\"", "\"BS8021\"", "\"BD9100\"").toArray(new String[0]));
    }

    private void shouldPlayerADiscardCardRound9() throws Exception {
    /*
       Given
           A 玩家進入棄牌階段(Discard)
           A 體力1
           A 玩家手牌有殺x5

           When
           系統進行棄牌判斷
           A 回合棄前 4 張

           Then
           A 玩家剩 1 張手牌
           換 B 玩家回合
           B Phase 是判斷階段
    */
        // given
        Game game = repository.findById("my-id")
                .orElseThrow(() -> new NotFoundException("Game not found"));
        // when 因為這邊不重要直接隨便丟牌就好
//        game.getPlayerDiscardCount(); //true
        List<HandCard> cards = game.getPlayer("player-a").getHand().getCards();
        List<String> ids = cards.stream()
                .skip(Math.max(0, cards.size() - 4))
                .map(HandCard::getId)
                .map(id -> "\"" + id + "\"")
                .toList();
        this.mockMvc.perform(post("/api/games/my-id/player:discardCards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ids.toString()))
                .andExpect(status().isOk())
                .andReturn();

        // then
        assertEquals(1, game.getPlayer("player-a").getHandSize());
        assertEquals(RoundPhase.Judgement, game.getCurrentRoundPhase());
        assertEquals("player-b", game.getCurrentRoundPlayer().getId());
        assertEquals(24, game.getGraveyard().size());
    }

    private void playerBTakeTurnRound10() throws Exception {
        currentPlayerPlayedCardToTargetPlayer("player-b", "player-a", "BC3055");
        shouldPlayerASkipAndAllPlayerReceiveDyingEvents("player-a", "player-b");
    }

    private void shouldPlayerASkipAndAllPlayerReceiveDyingEvents(String currentPlayer, String targetPlayerId) throws Exception {
        /*
        Given
        A 玩家 HP <= 0

        When
        A 玩家 skip 不出桃
        A 玩家已瀕臨死亡

        Then
        全部玩家收到 A 玩家瀕臨死亡 event
        全部玩家收到要求玩家 B 出桃 event
        */

        playCard(currentPlayer, targetPlayerId, "", "skip")
                .andExpect(status().isOk()).andReturn();

        String playCardJson = getJsonByPlayerId("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/PlayCard/player_a_skip_and_dying_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJson);

        String playerBGetPlayerBPlayCardJson = getJsonByPlayerId("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/PlayCard/player_a_skip_and_dying_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerBGetPlayerBPlayCardJson);

        String playerCGetPlayerCPlayCardJson = getJsonByPlayerId("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/PlayCard/player_a_skip_and_dying_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerCGetPlayerCPlayCardJson);

        String playerDGetPlayerDPlayCardJson = getJsonByPlayerId("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/PlayCard/player_a_skip_and_dying_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerDGetPlayerDPlayCardJson);
    }

    private void playerAPlayedCardSkip() throws Exception {
        playCard("player-a", "player-a", "", "skip")
                .andExpect(status().isOk()).andReturn();

        String playCardJsonForPlayerA = getJsonByPlayerId("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/AskPeach/player_a_skip_ask_peach_to_player_a_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerA);

        String playCardJsonForPlayerB = getJsonByPlayerId("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/AskPeach/player_a_skip_ask_peach_to_player_a_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerB);

        String playCardJsonForPlayerC = getJsonByPlayerId("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/AskPeach/player_a_skip_ask_peach_to_player_a_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerC);

        String playCardJsonForPlayerD = getJsonByPlayerId("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/AskPeach/player_a_skip_ask_peach_to_player_a_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerD);
    }

    private void playerBPlayedCardSkip() throws Exception {
        playCard("player-b", "player-a", "", "skip")
                .andExpect(status().isOk()).andReturn();

        String playCardJsonForPlayerA = getJsonByPlayerId("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/AskPeach/player_b_skip_ask_peach_to_player_a_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerA);

        String playCardJsonForPlayerB = getJsonByPlayerId("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/AskPeach/player_b_skip_ask_peach_to_player_a_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerB);

        String playCardJsonForPlayerC = getJsonByPlayerId("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/AskPeach/player_b_skip_ask_peach_to_player_a_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerC);

        String playCardJsonForPlayerD = getJsonByPlayerId("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/AskPeach/player_b_skip_ask_peach_to_player_a_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerD);
    }

    private void playerCPlayedCardSkip() throws Exception {
        playCard("player-c", "player-a", "", "skip")
                .andExpect(status().isOk()).andReturn();

        String playCardJsonForPlayerA = getJsonByPlayerId("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/AskPeach/player_c_skip_ask_peach_to_player_a_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerA);

        String playCardJsonForPlayerB = getJsonByPlayerId("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/AskPeach/player_c_skip_ask_peach_to_player_a_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerB);

        String playCardJsonForPlayerC = getJsonByPlayerId("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/AskPeach/player_c_skip_ask_peach_to_player_a_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerC);

        String playCardJsonForPlayerD = getJsonByPlayerId("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/AskPeach/player_c_skip_ask_peach_to_player_a_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerD);
    }

    private void playerDPlayedCardSkip() throws Exception {
        playCard("player-d", "player-a", "", "skip")
                .andExpect(status().isOk()).andReturn();

        String playCardJsonForPlayerA = getJsonByPlayerId("player-a");
        Path path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/GameOver/player_d_skip_ask_peach_to_player_a_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerA);

        String playCardJsonForPlayerB = getJsonByPlayerId("player-b");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/GameOver/player_d_skip_ask_peach_to_player_a_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerB);

        String playCardJsonForPlayerC = getJsonByPlayerId("player-c");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/GameOver/player_d_skip_ask_peach_to_player_a_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerC);

        String playCardJsonForPlayerD = getJsonByPlayerId("player-d");
        path = Paths.get("src/test/resources/TestJsonFile/HappyPath/Round10/GameOver/player_d_skip_ask_peach_to_player_a_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playCardJsonForPlayerD);
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

    public class TestSessionHandler extends StompSessionHandlerAdapter {
        private final AtomicReference<Throwable> failure;

        public TestSessionHandler(AtomicReference failure) {
            this.failure = failure;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            this.failure.set(new Exception(headers.toString()));
        }

        @Override
        public void handleException(StompSession s, StompCommand c, StompHeaders h, byte[] p, Throwable ex) {
            this.failure.set(ex);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable ex) {
            this.failure.set(ex);
        }
    }

}

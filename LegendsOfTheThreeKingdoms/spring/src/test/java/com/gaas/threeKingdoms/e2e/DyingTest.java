package com.gaas.threeKingdoms.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.outport.GameRepository;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.repository.InMemoryGameRepository;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
@AutoConfigureMockMvc
public class DyingTest {

//    @MockBean
//    private GameRepository repository;

    @Autowired
    private InMemoryGameRepository inMemoryGameRepository;

    private WebSocketStompClient stompClient;
    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    final ConcurrentHashMap<String, BlockingQueue<String>> map = new ConcurrentHashMap<>();

    @Autowired
    private MockMvc mockMvc;

    @Value(value = "${local.server.port}")
    private Integer port;

    @Autowired
    private ObjectMapper objectMapper;

    private final String gameId = "dyingTestGame";


    @BeforeEach
    public void setUp() throws Exception {
//        mockMvcUtil = new MockMvcUtil(mockMvc);
        //初始化前端 WebSocket 連線，模擬前端收到的 WebSocket 訊息
        System.out.println("GameTest port:" + port);
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        this.stompClient = new WebSocketStompClient(webSocketClient);
        this.stompClient.setMessageConverter(new StringMessageConverter());
        map.computeIfAbsent("player-a", k -> new LinkedBlockingQueue<>());
        map.computeIfAbsent("player-b", k -> new LinkedBlockingQueue<>());
        map.computeIfAbsent("player-c", k -> new LinkedBlockingQueue<>());
        map.computeIfAbsent("player-d", k -> new LinkedBlockingQueue<>());
        setupClientSubscribe(gameId, "player-a");
        setupClientSubscribe(gameId, "player-b");
        setupClientSubscribe(gameId, "player-c");
        setupClientSubscribe(gameId, "player-d");
        givenPlayerAIsEnterDyingStatus();
    }

    private void setupClientSubscribe(String gameId, String playerId) throws Exception {
        final AtomicReference<Throwable> failure = new AtomicReference<>(); // 創建一個原子型的引用變量，用於存放發生的異常

        StompSessionHandler handler = new GameTest.TestSessionHandler(failure) {
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
    public void testPlayerAIsEnterDyingStatus() throws Exception {
        givenPlayerAIsEnterDyingStatus();
        //Given A玩家瀕臨死亡
        Game game = inMemoryGameRepository.findById(gameId);

        //When A玩家出桃
        String currentPlayer = "player-a";
        String targetPlayerId = "player-a";
        String playedCardId = "BH3029";

        this.mockMvc.perform(post("/api/games/" + gameId + "/player:playCard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        { "playerId": "%s",
                          "targetPlayerId": "%s",
                          "cardId": "%s",
                          "playType": "%s"
                        }""", currentPlayer, targetPlayerId, playedCardId, "inactive")));

//        mockMvcUtil.playCard(gameId, currentPlayer, targetPlayerId, playedCardId, "inactive")
//                .andExpect(status().isOk()).andReturn();

        //Then A玩家還有一滴血
        String playerAPlayPeachJsonForA = map.get("player-a").poll(5, TimeUnit.SECONDS);
        Path path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndPlayerPeach/player_a_playpeach_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = map.get("player-b").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndPlayerPeach/player_a_playpeach_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = map.get("player-c").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndPlayerPeach/player_a_playpeach_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = map.get("player-d").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndPlayerPeach/player_a_playpeach_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);

//        String playerAPlayPeachJsonForA = websocketUtil.getValue("player-a");
//        Path path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndPlayerPeach/player_a_playpeach_for_player_a.json");
//        String expectedJson = Files.readString(path);
//        assertEquals(expectedJson, playerAPlayPeachJsonForA);
//
//        String playerAPlayPeachJsonForB = websocketUtil.getValue("player-b");
//        path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndPlayerPeach/player_a_playpeach_for_player_b.json");
//        expectedJson = Files.readString(path);
//        assertEquals(expectedJson, playerAPlayPeachJsonForB);
//
//        String playerAPlayPeachJsonForC = websocketUtil.getValue("player-c");
//        path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndPlayerPeach/player_a_playpeach_for_player_c.json");
//        expectedJson = Files.readString(path);
//        assertEquals(expectedJson, playerAPlayPeachJsonForC);
//
//        String playerAPlayPeachJsonForD = websocketUtil.getValue("player-d");
//        path = Paths.get("src/test/resources/TestJsonFile/DyingTest/PlayerADyingAndPlayerPeach/player_a_playpeach_for_player_d.json");
//        expectedJson = Files.readString(path);
//        assertEquals(expectedJson, playerAPlayPeachJsonForD);
    }


    private void givenPlayerAIsEnterDyingStatus() {
        Player playerA = createPlayer(
                "player-a",
                1,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Player playerB = createPlayer("player-b",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.MINISTER,
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)
        );

        Player playerC = createPlayer(
                "player-c",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.REBEL,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Player playerD = createPlayer(
                "player-d",
                4,
                General.劉備,
                HealthStatus.ALIVE,
                Role.TRAITOR,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)
        );

        Game game = initGame(gameId, playerA, playerB, playerC, playerD);

        // B對A出殺
        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");

        // A玩家出skip
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");
        inMemoryGameRepository.save(game);
//        Mockito.when(repository.findById(gameId)).thenReturn(game);
    }
}

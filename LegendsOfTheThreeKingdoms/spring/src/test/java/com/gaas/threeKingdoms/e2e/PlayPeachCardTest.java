package com.gaas.threeKingdoms.e2e;

import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.outport.GameRepository;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext
@AutoConfigureMockMvc
public class PlayPeachCardTest {

    @MockBean
    private GameRepository repository;

    @Autowired
    private MockMvc mockMvc;

    private WebSocketStompClient stompClient;
    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    final ConcurrentHashMap<String, BlockingQueue<String>> map = new ConcurrentHashMap<>();

    @Value(value = "${local.server.port}")
    private Integer port;
    private final String gameId = "my-id";

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
    public void testPlayerAPlayPeachCard() throws Exception {
        //Given A玩家hp為3
        givenPlayerAPlayCardStatus();

        //When A玩家出桃
//        mockMvcUtil.playCard(gameId, "player-a", "player-a", "BH3029", "active")
//                .andExpect(status().isOk());

        this.mockMvc.perform(post("/api/games/" + gameId + "/player:playCard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        { "playerId": "%s",
                          "targetPlayerId": "%s",
                          "cardId": "%s",
                          "playType": "%s"
                        }""", "player-a", "player-a", "BH3029", "active")));

        //Then A玩家hp為4
        String playerAPlayPeachJsonForA = map.get("player-a").poll(5, TimeUnit.SECONDS);
        Path path = Paths.get("src/test/resources/TestJsonFile/PlayPeachCardTest/PlayerPlayPeachCard/player_a_playpeach_for_player_a.json");
        String expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForA);

        String playerAPlayPeachJsonForB = map.get("player-b").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/PlayPeachCardTest/PlayerPlayPeachCard/player_a_playpeach_for_player_b.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForB);

        String playerAPlayPeachJsonForC = map.get("player-c").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/PlayPeachCardTest/PlayerPlayPeachCard/player_a_playpeach_for_player_c.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForC);

        String playerAPlayPeachJsonForD = map.get("player-d").poll(5, TimeUnit.SECONDS);
        path = Paths.get("src/test/resources/TestJsonFile/PlayPeachCardTest/PlayerPlayPeachCard/player_a_playpeach_for_player_d.json");
        expectedJson = Files.readString(path);
        assertEquals(expectedJson, playerAPlayPeachJsonForD);

    }


    private void givenPlayerAPlayCardStatus() {
        Player playerA = createPlayer(
                "player-a",
                4,
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

        playerA.damage(1);
        Mockito.when(repository.findById(gameId)).thenReturn(initGame(gameId, playerA, playerB, playerC, playerD));
    }




}

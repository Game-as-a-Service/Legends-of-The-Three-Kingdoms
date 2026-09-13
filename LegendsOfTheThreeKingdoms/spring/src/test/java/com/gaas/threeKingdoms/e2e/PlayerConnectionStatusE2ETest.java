package com.gaas.threeKingdoms.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 連線狀態廣播（issue #239）：STOMP 訂閱/斷線 → PlayerConnectionStatusEvent 廣播該局全員。
 * 用獨立 gameId — 測試基建的 @BeforeEach 會對預設 gameId 訂閱 a~g（在建局前），
 * 走預設 gameId 會與那些 session 混流、斷言不穩定。
 */
public class PlayerConnectionStatusE2ETest extends AbstractBaseIntegrationTest {

    private static final String CONN_GAME_ID = "conn-status-test";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    public void deleteConnGame() {
        repository.deleteById(CONN_GAME_ID);
    }

    @DisplayName("玩家逐一連線廣播 1/4→2/4；斷線回落 1/4；非該局玩家訂閱不廣播")
    @Test
    public void connectionStatusBroadcastOnSubscribeAndDisconnect() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH);
        Player playerB = createPlayer("player-b", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(CONN_GAME_ID, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        repository.save(game);

        // A 連線 → A 收到 1/4
        BlockingQueue<String> queueA = new LinkedBlockingQueue<>();
        StompSession sessionA = connectAndSubscribe("player-a", queueA);
        JsonNode first = nextEvent(queueA);
        assertEquals("PlayerConnectionStatusEvent", first.get("event").asText());
        assertEquals(List.of("player-a"),
                objectMapper.convertValue(first.get("data").get("connectedPlayerIds"), List.class));
        assertEquals(1, first.get("data").get("connectedCount").asInt());
        assertEquals(4, first.get("data").get("totalCount").asInt());
        assertEquals("player-a 已連線（1/4）", first.get("message").asText());

        // B 連線 → A、B 都收到 2/4（座位順序）
        BlockingQueue<String> queueB = new LinkedBlockingQueue<>();
        StompSession sessionB = connectAndSubscribe("player-b", queueB);
        JsonNode second = nextEvent(queueA);
        assertEquals(List.of("player-a", "player-b"),
                objectMapper.convertValue(second.get("data").get("connectedPlayerIds"), List.class));
        assertEquals("player-b 已連線（2/4）", second.get("message").asText());
        JsonNode secondForB = nextEvent(queueB);
        assertEquals(2, secondForB.get("data").get("connectedCount").asInt());

        // B 斷線 → A 收到回落 1/4
        sessionB.disconnect();
        JsonNode third = nextEvent(queueA);
        assertEquals(List.of("player-a"),
                objectMapper.convertValue(third.get("data").get("connectedPlayerIds"), List.class));
        assertEquals("player-b 已離線（1/4）", third.get("message").asText());

        // 非該局玩家（觀戰者）訂閱 → 不廣播
        BlockingQueue<String> queueX = new LinkedBlockingQueue<>();
        StompSession sessionX = connectAndSubscribe("spectator-x", queueX);
        assertNull(queueA.poll(1, TimeUnit.SECONDS), "非該局玩家連線不應廣播");

        sessionA.disconnect();
        sessionX.disconnect();
    }

    /** 建立單一 STOMP 連線並訂閱該玩家的推播路徑，收到的訊息放入 queue。 */
    private StompSession connectAndSubscribe(String playerId, BlockingQueue<String> queue) throws Exception {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new StringMessageConverter());
        StompSession session = client
                .connectAsync("ws://localhost:" + port + "/legendsOfTheThreeKingdoms", new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
        session.subscribe("/websocket/legendsOfTheThreeKingdoms/" + CONN_GAME_ID + "/" + playerId,
                new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return String.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        queue.add((String) payload);
                    }
                });
        return session;
    }

    private JsonNode nextEvent(BlockingQueue<String> queue) throws Exception {
        String message = queue.poll(5, TimeUnit.SECONDS);
        assertNotNull(message, "未收到連線狀態推播");
        return objectMapper.readTree(message);
    }
}

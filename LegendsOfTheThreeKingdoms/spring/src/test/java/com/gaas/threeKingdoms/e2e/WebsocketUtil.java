package com.gaas.threeKingdoms.e2e;

import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WebsocketUtil {

    /**
     * 固定的走訪順序，{@code player-a} 必須在第一個 —— 見
     * {@link #popOneMessagePerPlayer}，牠是用來等「這批推播開始抵達」的探針。
     * 全部 49 個用到 {@link #popAllPlayerMessage()} 的測試檔都有 player-a。
     */
    static final List<String> PLAYER_KEYS = List.of(
            "player-a", "player-b", "player-c", "player-d", "player-e", "player-f", "player-g");

    /** 等第一位玩家的訊息抵達的上限。STOMP 推播是非同步的，HTTP response 回來不代表訊息已進佇列。 */
    static final long BATCH_ARRIVAL_TIMEOUT_MILLIS = 1000L;

    /** 同一批推播是 server 端連續發給每位玩家的，抵達時間只差幾 ms，其餘玩家沿用原本的短 timeout。 */
    static final long SAME_BATCH_TIMEOUT_MILLIS = 50L;

    WebSocketClient webSocketClient;
    private WebSocketStompClient stompClient;
    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    private final ConcurrentHashMap<String, BlockingQueue<String>> map = new ConcurrentHashMap<>();
    private final Integer port;
    private volatile boolean isClearing = false;

    public WebsocketUtil(Integer port, String gameId) throws Exception {
        this.port = port;
        setUp(gameId);
    }

    public void setUp(String gameId) throws Exception {
        //初始化前端 WebSocket 連線，模擬前端收到的 WebSocket 訊息
        webSocketClient = new StandardWebSocketClient();
        this.stompClient = new WebSocketStompClient(webSocketClient);
        this.stompClient.setMessageConverter(new StringMessageConverter());
        map.computeIfAbsent("player-a", k -> new LinkedBlockingQueue<>());
        map.computeIfAbsent("player-b", k -> new LinkedBlockingQueue<>());
        map.computeIfAbsent("player-c", k -> new LinkedBlockingQueue<>());
        map.computeIfAbsent("player-d", k -> new LinkedBlockingQueue<>());
        map.computeIfAbsent("player-e", k -> new LinkedBlockingQueue<>());
        map.computeIfAbsent("player-f", k -> new LinkedBlockingQueue<>());
        map.computeIfAbsent("player-g", k -> new LinkedBlockingQueue<>());
        setupClientSubscribe(gameId, "player-a");
        setupClientSubscribe(gameId, "player-b");
        setupClientSubscribe(gameId, "player-c");
        setupClientSubscribe(gameId, "player-d");
        setupClientSubscribe(gameId, "player-e");
        setupClientSubscribe(gameId, "player-f");
        setupClientSubscribe(gameId, "player-g");
    }

    public String getValue(String key) {
        try {
            return map.get(key).poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearAllQueues() {
        synchronized (map) {
            map.values().forEach(Queue::clear); // 清空每個佇列
        }
    }

    /**
     * 清掉上一個步驟推播出來的訊息：每位玩家各 pop 一則。
     * <p>
     * 「每次呼叫每位玩家 pop 一則」的語意不能改 —— 有測試靠 pop 次數對應訊息數
     * （見 {@code FanKuiFullFlowTest} 選將那段）。這裡只調整等待時間，見
     * {@link #popOneMessagePerPlayer}。
     */
    public void popAllPlayerMessage() {
        try {
            popOneMessagePerPlayer(PLAYER_KEYS, map,
                    BATCH_ARRIVAL_TIMEOUT_MILLIS, SAME_BATCH_TIMEOUT_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * 每位玩家 pop 一則訊息。抽成 static 是為了能不開 websocket 直接單元測試（見 WebsocketUtilTest）。
     * <p>
     * 原本的寫法是每位玩家固定 {@code poll(50ms)}。但 STOMP 推播是非同步的，HTTP response 回來
     * 不代表訊息已經進到佇列；整批晚到超過 50ms（GC、container 抖動）時這批就沒被清掉、留在佇列裡，
     * 於是下一個 {@link #getValue} 讀到的是上一步的舊訊息 → 斷在 JSON 比對上（issue #249 的間歇性失敗）。
     * <p>
     * 改法只有一處：第一位玩家（player-a，一定在局中）改用長 timeout 當探針，等「這批推播開始抵達」；
     * 一旦抵達，同批其餘玩家的訊息只差幾 ms，沿用原本的短 timeout 即可。
     * <p>
     * 其餘玩家**一律**給短 timeout，不要試圖跳過「看起來沒上桌」的玩家來省時間 ——
     * 曾經試過用「收過訊息 = 在局中」來判斷並讓沒上桌的玩家 0ms，結果第一次呼叫時
     * player-a 的訊息已到、b/c/d 還沒到，roster 被誤判成已知，b/c/d 拿到 0ms 反而更容易漏清，
     * 打壞了 FanKuiAskPushTest / FanKuiFullFlowTest / QingGuoPushTest / JianXiongTest。
     * player-e/f/g 每次白等 50ms（334 個呼叫點約 50 秒）是刻意保留的原行為。
     */
    static void popOneMessagePerPlayer(List<String> orderedKeys,
                                       Map<String, BlockingQueue<String>> queues,
                                       long batchArrivalTimeoutMillis,
                                       long sameBatchTimeoutMillis) throws InterruptedException {
        boolean probeSpent = false;
        for (String key : orderedKeys) {
            BlockingQueue<String> queue = queues.get(key);
            if (queue == null) {
                continue;
            }
            long timeoutMillis = probeSpent ? sameBatchTimeoutMillis : batchArrivalTimeoutMillis;
            probeSpent = true;
            queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
        }
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
                StompSession.Subscription subscription = session.subscribe(String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", gameId, playerId), new StompFrameHandler() {  // 訂閱伺服器的 "/websocket/legendsOfTheThreeKingdoms/gameId/playerId" 路徑的訊息
                    @Override
                    public Type getPayloadType(StompHeaders headers) {  // 定義從伺服器收到的訊息內容的類型
                        return String.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        if (isClearing) {
                            return; // 忽略資料處理
                        }
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

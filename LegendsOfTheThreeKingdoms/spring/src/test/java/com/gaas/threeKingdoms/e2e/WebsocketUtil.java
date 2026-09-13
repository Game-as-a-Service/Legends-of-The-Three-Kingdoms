package com.gaas.threeKingdoms.e2e;

import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class WebsocketUtil {

    /** 走訪順序固定，讓「先收到誰的訊息」不受 map 迭代順序影響（結果可重現）。 */
    static final List<String> PLAYER_KEYS = List.of(
            "player-a", "player-b", "player-c", "player-d", "player-e", "player-f", "player-g");

    /** 這批推播完全還沒開始抵達時的等待上限。STOMP 推播是非同步的，HTTP response 回來不代表訊息已進佇列。 */
    static final long BATCH_ARRIVAL_TIMEOUT_MILLIS = 1000L;

    /**
     * 這批推播已經開始抵達之後，還要再等多久才認定「沒收到的玩家不在局中」。
     * 同批訊息是 server 端逐個 destination 送出的，正常只差幾 ms，300ms 是很寬的餘裕。
     */
    static final long ABSENT_PLAYER_GRACE_MILLIS = 300L;

    /** 輪詢佇列的間隔。等待是「所有佇列同時等」，不是逐個 queue 各自 poll，所以不會累加。 */
    static final long POLL_INTERVAL_MILLIS = 2L;

    /**
     * 就緒哨兵的 payload 前綴。凡是以此開頭的訊息都**不會**進測試佇列（見 {@link #setupClientSubscribe}），
     * 所以就算某個哨兵姍姍來遲也絕不可能被誤讀成遊戲事件。
     */
    static final String SENTINEL_PREFIX = "__e2e-subscription-ready__";

    /** 等所有訂閱就緒的上限。正常在數十 ms 內完成，逾時代表基建壞了，該讓測試大聲失敗而不是默默慢下去。 */
    static final long SUBSCRIPTION_READY_TIMEOUT_MILLIS = 5000L;

    /** 補送哨兵的間隔。SimpleBroker 對還沒有 subscriber 的 destination 是直接丟棄，所以必須重送。 */
    static final long SENTINEL_RETRY_INTERVAL_MILLIS = 10L;

    WebSocketClient webSocketClient;
    private WebSocketStompClient stompClient;
    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    private final ConcurrentHashMap<String, BlockingQueue<String>> map = new ConcurrentHashMap<>();
    private final Integer port;
    private final String gameId;
    private volatile boolean isClearing = false;

    /** 這個 instance 的哨兵識別碼，避免收到別的 instance（別支測試）的哨兵就誤判自己已就緒。 */
    private final String readyNonce = UUID.randomUUID().toString();

    /** 已經確認「推播真的送得到」的玩家。 */
    private final Set<String> readyPlayers = ConcurrentHashMap.newKeySet();

    /**
     * 每條 STOMP 連線的 future。{@link #close()} 靠它逐條 disconnect ——
     * 以前這個 future 直接被丟棄，session 連 field 都沒存，導致每支測試洩漏 7 條連線（見 close 的 javadoc）。
     */
    private final List<CompletableFuture<StompSession>> sessionFutures = new ArrayList<>();

    /** 確定在局中的玩家（曾收到過訊息）。跨 pop 呼叫累積，per-instance = per-test。 */
    private final Set<String> seatedPlayers = ConcurrentHashMap.newKeySet();

    /** 確定不在局中的玩家（整批推播都抵達了還是沒有牠的訊息）。學會之後就不再為牠等待。 */
    private final Set<String> absentPlayers = ConcurrentHashMap.newKeySet();

    public WebsocketUtil(Integer port, String gameId) throws Exception {
        this.port = port;
        this.gameId = gameId;
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

    /**
     * 等到 7 條訂閱**真的收得到推播**才回來 —— 取代原本 {@code @BeforeEach} 裡那行
     * {@code Thread.sleep(1000)}。
     *
     * <h4>為什麼原本要睡 1 秒</h4>
     * {@code connectAsync} 是非同步的，建構子回來時 7 條連線都還沒訂閱完；測試接著就打 HTTP
     * 建局，而 SimpleBroker 對「沒有 subscriber 的 destination」是**直接丟棄、不排隊**，
     * 所以訂閱沒趕上就等於永久漏掉第一批事件。1 秒是「應該夠了吧」的猜測：
     * 既不保證正確（負載高時仍可能不夠），又對 259 支測試各收 1 秒的固定稅
     * （量過：5 個 class / 53 支測試，72.4s 裡有 53s 是這行）。
     *
     * <h4>改成什麼</h4>
     * 由呼叫端（{@code AbstractBaseIntegrationTest}）提供一個「往 destination 送訊息」的 sender，
     * 這裡對每個還沒就緒的玩家送一枚哨兵，收到自己的哨兵才算該條訂閱就緒。
     * 這是端到端的證據，不是計時器：訂閱已註冊、broker 走訪得到它、frame handler 也接得到。
     * <ul>
     *   <li>**必須重送** —— 訂閱還沒註冊時哨兵會被丟棄，送一次而不重試就會永遠等不到。</li>
     *   <li>**哨兵絕不進測試佇列** —— {@link #setupClientSubscribe} 的 frame handler 依
     *       {@link #SENTINEL_PREFIX} 過濾。重送本來就可能讓多枚哨兵在路上，若靠事後「清到安靜」
     *       來收拾，遲到的那枚就會變成下一次 {@code getValue()} 讀到的東西 —— 正是這套測試
     *       最常見的失敗形態（訊息位移）。在來源就攔掉才是真的安全。</li>
     *   <li>nonce 讓別支測試殘留的哨兵不會被誤認成自己的。</li>
     * </ul>
     *
     * @param sender (destination, payload) → 送出，通常是 {@code SimpMessagingTemplate::convertAndSend}
     */
    public void awaitSubscriptionsReady(BiConsumer<String, String> sender) throws InterruptedException {
        long deadlineNanos = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(SUBSCRIPTION_READY_TIMEOUT_MILLIS);
        while (readyPlayers.size() < PLAYER_KEYS.size()) {
            for (String playerId : PLAYER_KEYS) {
                if (!readyPlayers.contains(playerId)) {
                    sender.accept(destinationOf(playerId), sentinelPayload(readyNonce, playerId));
                }
            }
            if (readyPlayers.size() == PLAYER_KEYS.size()) {
                break;
            }
            if (System.nanoTime() >= deadlineNanos) {
                List<String> notReady = PLAYER_KEYS.stream().filter(key -> !readyPlayers.contains(key)).toList();
                throw new IllegalStateException(String.format(
                        "等了 %d ms 這些玩家的訂閱還是收不到推播：%s（gameId=%s）",
                        SUBSCRIPTION_READY_TIMEOUT_MILLIS, notReady, gameId));
            }
            Thread.sleep(SENTINEL_RETRY_INTERVAL_MILLIS);
        }
    }

    String destinationOf(String playerId) {
        return String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", gameId, playerId);
    }

    /** 哨兵帶上 nonce 與 playerId，收到時才能確認「是我這個 instance 的、這條訂閱的」。 */
    static String sentinelPayload(String nonce, String playerId) {
        return SENTINEL_PREFIX + ":" + nonce + ":" + playerId;
    }

    /** 只要看起來是哨兵就一律不進測試佇列，包含別的 instance 送的。 */
    static boolean isSentinel(String payload) {
        return payload != null && payload.startsWith(SENTINEL_PREFIX);
    }

    public String getValue(String key) {
        try {
            return map.get(key).poll(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /** 不等待，只看佇列現在有沒有東西。用來斷言「這裡本來就該是空的」（見 WebsocketReadinessBarrierTest）。 */
    public String pollNow(String key) {
        return map.get(key).poll();
    }

    public void clearAllQueues() {
        synchronized (map) {
            map.values().forEach(Queue::clear); // 清空每個佇列
        }
    }

    /**
     * 關掉這個 instance 開的 7 條 STOMP 連線。**每支 e2e 測試結束都必須呼叫**
     * （見 {@code AbstractBaseIntegrationTest#tearDown}）。
     * <p>
     * 以前沒有這個方法：{@link #setupClientSubscribe} 把 {@code connectAsync} 的 future 直接丟棄，
     * session 沒有任何 reference，所以事後不可能 disconnect。全套 e2e 共用一個 Spring context
     * 與一個 broker，每支測試又在 {@code @BeforeEach} 開 7 條連線，於是跑到後段時
     * {@code /websocket/.../my-id/player-a} 這個 destination 上累積了「已跑過的測試數」個訂閱
     * （264 支測試 × 7 ≈ 1800 條 session）。後果有兩層：
     * <ul>
     *   <li>每次 {@code convertAndSend} 的 fan-out 被放大上百倍；</li>
     *   <li>SimpleBroker 按註冊順序走訪訂閱，**當前測試的 session 是最新註冊的、排在最後**，
     *       等於每則訊息都最後才送到唯一在意它的 subscriber。</li>
     * </ul>
     * 這條曲線隨測試支數單調惡化，是 issue #249「每次跑都有 1 支間歇性失敗、且每次不同支」
     * 的放大器 —— 也是 #253 把 timeout 拉長只能把撞牆點往後推、無法消除的原因。
     */
    public void close() {
        for (CompletableFuture<StompSession> sessionFuture : sessionFutures) {
            try {
                StompSession session = sessionFuture.get(1, TimeUnit.SECONDS);
                if (session != null && session.isConnected()) {
                    session.disconnect();
                }
            } catch (Exception e) {
                sessionFuture.cancel(true); // 連不上就算了，測試已經跑完，重點是不要留著訂閱
            }
        }
        sessionFutures.clear();
        try {
            stompClient.stop();
        } catch (Exception ignored) {
            // stop() 失敗不影響測試結果，session 已經 disconnect
        }
    }

    /**
     * 清掉上一個步驟推播出來的訊息：每位玩家各 pop 一則。
     * <p>
     * 「每次呼叫每位玩家 pop 一則」的語意不能改 —— 有測試靠 pop 次數對應訊息數
     * （見 {@code FanKuiFullFlowTest} 選將那段）。等待策略見 {@link #popOneMessagePerPlayer}。
     */
    public void popAllPlayerMessage() {
        try {
            popOneMessagePerPlayer(PLAYER_KEYS, map, seatedPlayers, absentPlayers,
                    BATCH_ARRIVAL_TIMEOUT_MILLIS, ABSENT_PLAYER_GRACE_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * 每位玩家 pop 一則訊息。抽成 static 是為了能不開 websocket 直接單元測試（見 WebsocketUtilTest）。
     *
     * <h4>為什麼不是逐個 queue 各自 poll</h4>
     * 舊版是逐個 queue 依序 {@code poll(timeout)}：player-a 拿 1000ms 當「這批推播開始抵達」的探針，
     * 其餘 6 位只有 50ms（issue #249 / PR #253）。這個作法有兩個問題：
     * <ul>
     *   <li>探針的前提不成立 —— 7 位玩家各走一條**獨立**的 WebSocket 連線（見 {@link #setUp}），
     *       彼此沒有時序耦合，「player-a 到了所以 b/c/d 也到了」只是機率。50ms 的跨連線 skew
     *       在負載下完全正常，於是還是會漏清。</li>
     *   <li>逐個 poll 的等待會**累加**，所以想給每位玩家足夠預算就得付 7 倍時間，
     *       只能被迫把其餘玩家壓在 50ms。</li>
     * </ul>
     * 改成「同時等所有佇列」的輪詢迴圈之後，等待時間是各玩家的 max 而不是 sum，
     * 就有本錢讓**每位在局中的玩家都拿到完整預算** —— 這正是 20 個測試檔自己那份
     * {@code getValue()} 版 helper（每個 queue 各有 5 秒獨立預算）從來不 flaky 的原因，
     * 現在共用版跟牠語意一致了。
     *
     * <h4>怎麼知道誰不在局中</h4>
     * 這個 util 固定訂閱 a~g，但多數測試只有 4 人（也有 7 人局，見 {@code QilinBowTest}），
     * 沒上桌的玩家永遠不會有訊息，為牠等待是純成本。判定規則刻意保守：
     * <ul>
     *   <li>收過訊息 → 記入 {@code seatedPlayers}，之後**一律**給到完整的
     *       {@code batchArrivalTimeoutMillis}（牠一定有訊息，等到就走，等待是免費的）。</li>
     *   <li>從沒收過訊息、且這批推播已經開始抵達 → 再寬限 {@code absentPlayerGraceMillis}
     *       才認定不在局中，記入 {@code absentPlayers}，後續呼叫不再為牠等待。</li>
     *   <li>這批推播**完全沒有**任何訊息抵達時，不判定任何人缺席（避免把「這一步沒有推播」
     *       誤判成「所有人都沒上桌」）。</li>
     * </ul>
     * 保守的理由是踩過的坑：原本想用「收過訊息 = 在局中」直接讓沒上桌的玩家 0ms，
     * 但第一次呼叫時 player-a 的訊息已到、b/c/d 還沒到，roster 被誤判成已知，
     * b/c/d 拿到 0ms 反而更容易漏清，打壞了 FanKuiAskPushTest / FanKuiFullFlowTest /
     * QingGuoPushTest / JianXiongTest。現在缺席判定必須等到寬限期結束，且只要曾收過訊息就永不缺席。
     */
    static void popOneMessagePerPlayer(List<String> orderedKeys,
                                       Map<String, BlockingQueue<String>> queues,
                                       Set<String> seatedPlayers,
                                       Set<String> absentPlayers,
                                       long batchArrivalTimeoutMillis,
                                       long absentPlayerGraceMillis) throws InterruptedException {
        Set<String> pending = new LinkedHashSet<>();
        for (String key : orderedKeys) {
            if (queues.get(key) != null && !absentPlayers.contains(key)) {
                pending.add(key);
            }
        }
        long hardDeadlineNanos = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(batchArrivalTimeoutMillis);
        long graceDeadlineNanos = Long.MAX_VALUE; // 這批開始抵達後才有意義

        while (!pending.isEmpty()) {
            for (Iterator<String> iterator = pending.iterator(); iterator.hasNext(); ) {
                String key = iterator.next();
                if (queues.get(key).poll() != null) {
                    seatedPlayers.add(key);
                    iterator.remove();
                    if (graceDeadlineNanos == Long.MAX_VALUE) {
                        graceDeadlineNanos = System.nanoTime()
                                + TimeUnit.MILLISECONDS.toNanos(absentPlayerGraceMillis);
                    }
                }
            }
            if (pending.isEmpty()) {
                break;
            }
            // 還在等已知在局中的玩家 → 給到完整預算；只剩沒上桌的候選人 → 寬限期到就收工
            boolean waitingForSeatedPlayer = pending.stream().anyMatch(seatedPlayers::contains);
            long deadlineNanos = waitingForSeatedPlayer
                    ? hardDeadlineNanos
                    : Math.min(graceDeadlineNanos, hardDeadlineNanos);
            if (System.nanoTime() >= deadlineNanos) {
                break;
            }
            Thread.sleep(POLL_INTERVAL_MILLIS);
        }

        boolean batchStartedArriving = graceDeadlineNanos != Long.MAX_VALUE;
        if (batchStartedArriving) {
            pending.stream().filter(key -> !seatedPlayers.contains(key)).forEach(absentPlayers::add);
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
                        if (isSentinel((String) payload)) {
                            // 就緒哨兵不是遊戲事件，不能進佇列（見 awaitSubscriptionsReady）
                            if (sentinelPayload(readyNonce, playerId).equals(payload)) {
                                readyPlayers.add(playerId);
                            }
                            return;
                        }
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
        // future 必須留著，否則 session 沒有 reference、事後無法 disconnect（見 close()）
        sessionFutures.add(this.stompClient.connectAsync(
                "ws://localhost:{port}/legendsOfTheThreeKingdoms", this.headers, handler, this.port));
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

package com.gaas.threeKingdoms.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.gaas.threeKingdoms.e2e.WebsocketUtil.PLAYER_KEYS;
import static com.gaas.threeKingdoms.e2e.WebsocketUtil.popOneMessagePerPlayer;
import static org.junit.jupiter.api.Assertions.*;

/**
 * issue #249：spring e2e 每次跑都有約 1 支間歇性失敗，且每次是不同支。
 * <p>
 * 症狀是整包訊息位移一格：{@link WebsocketUtil#popAllPlayerMessage()} 沒把這一步的推播清乾淨，
 * 殘留的舊訊息讓後續每個 {@code getValue()} 都錯開一格（位置性 FIFO，錯開後不會自癒）。
 * <p>
 * 等待邏輯抽成 static 的 {@link WebsocketUtil#popOneMessagePerPlayer} 就是為了能在這裡
 * 不開 websocket、不進 Spring context 直接驗證，把原本只能靠「跑很多次看會不會壞」的
 * 計時性問題變成確定性測試。
 */
public class WebsocketUtilTest {

    private static final long BATCH_ARRIVAL_TIMEOUT = 1000L;
    private static final long ABSENT_GRACE = 300L;
    private static final List<String> SEATED = List.of("player-a", "player-b", "player-c", "player-d");
    private static final List<String> NOT_SEATED = List.of("player-e", "player-f", "player-g");

    private ScheduledExecutorService scheduler;
    private Set<String> seatedPlayers;
    private Set<String> absentPlayers;

    @BeforeEach
    void setUp() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        seatedPlayers = ConcurrentHashMap.newKeySet();
        absentPlayers = ConcurrentHashMap.newKeySet();
    }

    @AfterEach
    void tearDown() {
        scheduler.shutdownNow();
    }

    private Map<String, BlockingQueue<String>> emptyQueues() {
        Map<String, BlockingQueue<String>> queues = new LinkedHashMap<>();
        PLAYER_KEYS.forEach(key -> queues.put(key, new LinkedBlockingQueue<>()));
        return queues;
    }

    private void pop(Map<String, BlockingQueue<String>> queues) throws InterruptedException {
        popOneMessagePerPlayer(PLAYER_KEYS, queues, seatedPlayers, absentPlayers,
                BATCH_ARRIVAL_TIMEOUT, ABSENT_GRACE);
    }

    @DisplayName("整批推播晚到 300ms：仍會清乾淨（issue #249 根因）")
    @Test
    void lateArrivingBatchStillGetsDrained() throws Exception {
        // Given 四人局，這批推播 300ms 後才抵達（模擬 GC / container 抖動）
        Map<String, BlockingQueue<String>> queues = emptyQueues();
        scheduler.schedule(
                () -> SEATED.forEach(key -> queues.get(key).add("第 1 步的訊息")),
                300, TimeUnit.MILLISECONDS);

        // When
        pop(queues);

        // Then 四位玩家的佇列都清空了 —— 下一步的 getValue() 不會讀到舊訊息
        for (String key : SEATED) {
            assertTrue(queues.get(key).isEmpty(), key + " 的舊訊息沒被清掉，下一步會讀到它");
        }
    }

    @DisplayName("同一情境若預算只有 50ms：訊息留在佇列裡 → 這就是 flaky 的來源")
    @Test
    void tooShortBudgetMissesLateBatch() throws Exception {
        Map<String, BlockingQueue<String>> queues = emptyQueues();
        scheduler.schedule(() -> SEATED.forEach(key -> queues.get(key).add("第 1 步的訊息")),
                300, TimeUnit.MILLISECONDS);

        popOneMessagePerPlayer(PLAYER_KEYS, queues, seatedPlayers, absentPlayers, 50L, 50L);

        // 300ms 後訊息才到，早就放棄了 → 殘留
        assertNotNull(queues.get("player-a").poll(1, TimeUnit.SECONDS),
                "預算不足時應該漏清這則訊息（若這裡是 null，表示這支測試已經無法重現 #249 的根因）");
    }

    @DisplayName("每位玩家只 pop 一則：同批有兩則時第二則要留著（FanKuiFullFlowTest 靠 pop 次數對應訊息數）")
    @Test
    void popsExactlyOneMessagePerPlayer() throws Exception {
        Map<String, BlockingQueue<String>> queues = emptyQueues();
        for (String key : SEATED) {
            queues.get(key).add("第 1 則");
            queues.get(key).add("第 2 則");
        }

        pop(queues);

        for (String key : SEATED) {
            assertEquals(1, queues.get(key).size(), key + " 應該只被 pop 掉一則");
            assertEquals("第 2 則", queues.get(key).peek(), key + " 剩下的應該是第 2 則");
        }
    }

    /**
     * 這支守的是踩過的坑：原本想用「收過訊息 = 在局中」來讓沒上桌的玩家 0ms 省時間，
     * 但第一次呼叫時 player-a 的訊息已到、b/c/d 還沒到，roster 會被誤判成已知，
     * b/c/d 拿到 0ms 反而更容易漏清 —— 打壞了 FanKuiAskPushTest / FanKuiFullFlowTest /
     * QingGuoPushTest / JianXiongTest。
     */
    @DisplayName("player-a 先到、b/c/d 稍晚到也不能漏清，而且不能被誤判成沒上桌")
    @Test
    void playersArrivingAfterTheFirstOneAreStillDrained() throws Exception {
        Map<String, BlockingQueue<String>> queues = emptyQueues();
        queues.get("player-a").add("立刻拿到");
        // b/c/d 的訊息比 player-a 晚一點才到（同批，但跨連線排程有落差）
        scheduler.schedule(
                () -> List.of("player-b", "player-c", "player-d")
                        .forEach(key -> queues.get(key).add("同批稍晚抵達")),
                120, TimeUnit.MILLISECONDS);

        pop(queues);

        for (String key : SEATED) {
            assertTrue(queues.get(key).isEmpty(), key + " 的訊息不該被漏掉");
            assertFalse(absentPlayers.contains(key), key + " 在局中，不該被判定為缺席");
        }
    }

    @DisplayName("已知在局中的玩家就算晚到 500ms（超過缺席寬限）也要等到牠")
    @Test
    void knownSeatedPlayerGetsFullBudgetEvenBeyondTheGrace() throws Exception {
        Map<String, BlockingQueue<String>> queues = emptyQueues();
        seatedPlayers.addAll(SEATED); // 前幾次呼叫已經確認這四位在局中
        queues.get("player-a").add("立刻拿到");
        scheduler.schedule(() -> queues.get("player-d").add("落後 500ms 才到"),
                500, TimeUnit.MILLISECONDS);

        pop(queues);

        assertTrue(queues.get("player-d").isEmpty(),
                "player-d 已知在局中，預算應該是完整的 batchArrivalTimeout 而不是缺席寬限");
        assertFalse(absentPlayers.contains("player-d"), "曾收過訊息的玩家永遠不該被判定為缺席");
    }

    @DisplayName("沒上桌的玩家學會一次就不再等：第二次呼叫幾乎不花時間")
    @Test
    void absentPlayersAreLearnedOnceThenSkipped() throws Exception {
        Map<String, BlockingQueue<String>> queues = emptyQueues();
        SEATED.forEach(key -> queues.get(key).add("第 1 步"));

        pop(queues);

        assertEquals(new HashSet<>(NOT_SEATED), absentPlayers, "e/f/g 應該被判定為沒上桌");
        assertEquals(new HashSet<>(SEATED), seatedPlayers);

        // 第二次呼叫不該再為 e/f/g 等待
        SEATED.forEach(key -> queues.get(key).add("第 2 步"));
        long startNanos = System.nanoTime();
        pop(queues);
        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;

        assertTrue(elapsedMillis < ABSENT_GRACE,
                "學會缺席名單後不該再付寬限時間，實際花了 " + elapsedMillis + "ms");
        for (String key : SEATED) {
            assertTrue(queues.get(key).isEmpty(), key + " 第 2 步的訊息也要清掉");
        }
    }

    @DisplayName("這一步完全沒有推播時，不能把所有人都誤判成沒上桌")
    @Test
    void noMessageAtAllDoesNotMarkAnyoneAbsent() throws Exception {
        Map<String, BlockingQueue<String>> queues = emptyQueues();

        pop(queues);

        assertTrue(absentPlayers.isEmpty(),
                "整批都沒抵達 ≠ 所有人都沒上桌；誤判會讓後續呼叫完全不等待");
        assertTrue(seatedPlayers.isEmpty());
    }

    @DisplayName("沒有任何訊息可清時不會卡死：最多花掉 batchArrivalTimeout")
    @Test
    void boundedWhenThereIsNothingToPop() throws Exception {
        Map<String, BlockingQueue<String>> queues = emptyQueues();

        long startNanos = System.nanoTime();
        pop(queues);
        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;

        // 關鍵是「同時等」而不是逐個 queue 累加：7 位玩家也只花一份 timeout
        assertTrue(elapsedMillis < BATCH_ARRIVAL_TIMEOUT * 2,
                "空佇列的等待要有上限，實際花了 " + elapsedMillis + "ms");
    }

    @DisplayName("7 人局：a~g 全部在局中都要清乾淨（QilinBowTest 是 7 人場）")
    @Test
    void sevenPlayerGameDrainsEveryQueue() throws Exception {
        Map<String, BlockingQueue<String>> queues = emptyQueues();
        PLAYER_KEYS.forEach(key -> queues.get(key).add("七人局的推播"));

        pop(queues);

        for (String key : PLAYER_KEYS) {
            assertTrue(queues.get(key).isEmpty(), key + " 的訊息沒被清掉");
        }
        assertTrue(absentPlayers.isEmpty(), "7 人局沒有人缺席");
    }

    @DisplayName("就緒哨兵認得出自己送的、也認得出別人送的（都不能進佇列）")
    @Test
    void sentinelIsRecognisableRegardlessOfSender() {
        String mine = WebsocketUtil.sentinelPayload("nonce-1", "player-a");
        String otherInstance = WebsocketUtil.sentinelPayload("nonce-2", "player-a");

        assertTrue(WebsocketUtil.isSentinel(mine));
        // 別支測試殘留的哨兵也要被攔掉，否則會被當成遊戲事件讀進來
        assertTrue(WebsocketUtil.isSentinel(otherInstance));
        // 但不能被誤認成「我這條訂閱已就緒」的證據
        assertNotEquals(mine, otherInstance);
    }

    @DisplayName("真的遊戲事件 JSON 不會被當成哨兵過濾掉")
    @Test
    void realEventIsNotMistakenForSentinel() {
        assertFalse(WebsocketUtil.isSentinel("{\"event\":\"GetGeneralCardEvent\",\"gameId\":\"my-id\"}"));
        assertFalse(WebsocketUtil.isSentinel(""));
        assertFalse(WebsocketUtil.isSentinel(null));
    }
}

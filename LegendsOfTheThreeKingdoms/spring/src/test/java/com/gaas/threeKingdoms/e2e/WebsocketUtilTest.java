package com.gaas.threeKingdoms.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
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
 * 根因在 {@link WebsocketUtil#popAllPlayerMessage()} 原本每位玩家固定 {@code poll(50ms)}：
 * STOMP 推播晚到就漏清，下一個 {@code getValue()} 讀到上一步的舊訊息 → 斷在 JSON 比對上。
 * <p>
 * 等待邏輯抽成 static 的 {@link WebsocketUtil#popOneMessagePerPlayer} 就是為了能在這裡
 * 不開 websocket、不進 Spring context 直接驗證，把原本只能靠「跑很多次看會不會壞」的
 * 計時性問題變成確定性測試。
 */
public class WebsocketUtilTest {

    private static final long PROBE_TIMEOUT = 1000L;
    private static final long SAME_BATCH_TIMEOUT = 50L;
    private static final List<String> SEATED = List.of("player-a", "player-b", "player-c", "player-d");

    private ScheduledExecutorService scheduler;

    @BeforeEach
    void setUp() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
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

    @DisplayName("整批推播晚到 300ms：探針 timeout 仍會清乾淨（issue #249 根因）")
    @Test
    void lateArrivingBatchStillGetsDrained() throws Exception {
        // Given 四人局，這批推播 300ms 後才抵達（模擬 GC / container 抖動）
        Map<String, BlockingQueue<String>> queues = emptyQueues();
        scheduler.schedule(
                () -> SEATED.forEach(key -> queues.get(key).add("第 1 步的訊息")),
                300, TimeUnit.MILLISECONDS);

        // When
        popOneMessagePerPlayer(PLAYER_KEYS, queues, PROBE_TIMEOUT, SAME_BATCH_TIMEOUT);

        // Then 四位玩家的佇列都清空了 —— 下一步的 getValue() 不會讀到舊訊息
        for (String key : SEATED) {
            assertTrue(queues.get(key).isEmpty(), key + " 的舊訊息沒被清掉，下一步會讀到它");
        }
    }

    @DisplayName("同一情境用舊的固定 50ms：訊息留在佇列裡 → 這就是 flaky 的來源")
    @Test
    void oldFixedFiftyMillisTimeoutMissesLateBatch() throws Exception {
        Map<String, BlockingQueue<String>> queues = emptyQueues();
        scheduler.schedule(() -> queues.get("player-a").add("第 1 步的訊息"),
                300, TimeUnit.MILLISECONDS);

        // 舊行為 = 探針也只有 50ms
        popOneMessagePerPlayer(PLAYER_KEYS, queues, SAME_BATCH_TIMEOUT, SAME_BATCH_TIMEOUT);

        // 300ms 後訊息才到，早就放棄了 → 殘留
        assertNotNull(queues.get("player-a").poll(1, TimeUnit.SECONDS),
                "舊行為應該漏清這則訊息（若這裡是 null，表示這支測試已經無法重現 #249 的根因）");
    }

    @DisplayName("每位玩家只 pop 一則：同批有兩則時第二則要留著（FanKuiFullFlowTest 靠 pop 次數對應訊息數）")
    @Test
    void popsExactlyOneMessagePerPlayer() throws Exception {
        Map<String, BlockingQueue<String>> queues = emptyQueues();
        for (String key : SEATED) {
            queues.get(key).add("第 1 則");
            queues.get(key).add("第 2 則");
        }

        popOneMessagePerPlayer(PLAYER_KEYS, queues, PROBE_TIMEOUT, SAME_BATCH_TIMEOUT);

        for (String key : SEATED) {
            assertEquals(1, queues.get(key).size(), key + " 應該只被 pop 掉一則");
            assertEquals("第 2 則", queues.get(key).peek(), key + " 剩下的應該是第 2 則");
        }
    }

    /**
     * 這支守的是我自己踩過的坑：原本想用「收過訊息 = 在局中」來讓沒上桌的玩家 0ms 省時間，
     * 但第一次呼叫時 player-a 的訊息已到、b/c/d 還沒到，roster 會被誤判成已知，
     * b/c/d 拿到 0ms 反而更容易漏清 —— 打壞了 FanKuiAskPushTest / FanKuiFullFlowTest /
     * QingGuoPushTest / JianXiongTest。探針以外的玩家一律要有等待預算。
     */
    @DisplayName("探針之外的玩家一律要等：player-a 先到、b/c/d 稍晚到也不能漏清")
    @Test
    void everyPlayerAfterTheProbeStillGetsAWaitBudget() throws Exception {
        Map<String, BlockingQueue<String>> queues = emptyQueues();
        queues.get("player-a").add("探針立刻拿到");
        // b/c/d 的訊息比 player-a 晚一點才到（同批，但執行緒排程有落差）
        scheduler.schedule(
                () -> List.of("player-b", "player-c", "player-d")
                        .forEach(key -> queues.get(key).add("同批稍晚抵達")),
                30, TimeUnit.MILLISECONDS);

        popOneMessagePerPlayer(PLAYER_KEYS, queues, PROBE_TIMEOUT, SAME_BATCH_TIMEOUT);

        for (String key : SEATED) {
            assertTrue(queues.get(key).isEmpty(), key + " 的訊息不該被漏掉");
        }
    }

    @DisplayName("沒有任何訊息可清時不會卡死：最多花掉探針的預算")
    @Test
    void boundedWhenThereIsNothingToPop() throws Exception {
        Map<String, BlockingQueue<String>> queues = emptyQueues();

        long startNanos = System.nanoTime();
        popOneMessagePerPlayer(PLAYER_KEYS, queues, PROBE_TIMEOUT, SAME_BATCH_TIMEOUT);
        long elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000;

        // 探針 1000ms + 其餘 6 位 × 50ms，留餘裕
        assertTrue(elapsedMillis < 2000, "空佇列的等待要有上限，實際花了 " + elapsedMillis + "ms");
    }
}

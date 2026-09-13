package com.gaas.threeKingdoms.e2e;

import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.websocket.PlayerConnectionRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 守 e2e 測試基建的 STOMP session 生命週期。
 * <p>
 * 在這之前 {@link WebsocketUtil} 沒有 {@code close()}：{@code connectAsync} 的 future 被直接丟棄，
 * session 沒有任何 reference，所以每支測試在 {@code @BeforeEach} 開的 7 條連線永遠不會關。
 * 全套 e2e 共用一個 Spring context 與一個 broker、又共用 gameId {@code my-id}，
 * 於是同一個 destination 上的訂閱數 = 已跑過的測試數（264 支 × 7 ≈ 1800 條 session）。
 * 每次推播的 fan-out 被放大上百倍，而 SimpleBroker 按註冊順序走訪訂閱、當前測試的 session
 * 排在最後，等於每則訊息都最後才送到唯一在意它的 subscriber —— 這是 issue #249
 * 「每次跑都有 1 支間歇性失敗、且每次不同支」的放大器。
 * <p>
 * {@link PlayerConnectionRegistry} 是 server 端真正的連線表，所以洩漏可以直接斷言，
 * 不必只靠推理。
 */
public class WebsocketSessionLifecycleTest extends AbstractBaseIntegrationTest {

    /** 這個 util 訂閱 a~g 共 7 條。 */
    private static final int SUBSCRIBED_SESSION_COUNT = 7;

    @Autowired
    private PlayerConnectionRegistry registry;

    @DisplayName("@BeforeEach 開的 7 條 STOMP session 在 close() 之後必須全部從連線表消失")
    @Test
    public void websocketSessionsAreFullyReleasedOnClose() throws Exception {
        // Given @BeforeEach 已經開好 7 條連線並訂閱 my-id 的 a~g
        Set<String> connectedBeforeClose = awaitConnectedPlayerCount(SUBSCRIBED_SESSION_COUNT);
        assertEquals(SUBSCRIBED_SESSION_COUNT, connectedBeforeClose.size(),
                "測試基建應該訂閱 player-a~g，實際為 " + connectedBeforeClose);

        // When
        websocketUtil.close();

        // Then 連線表清空 —— 沒有 session 留給下一支測試累積
        Set<String> connectedAfterClose = awaitConnectedPlayerCount(0);
        assertTrue(connectedAfterClose.isEmpty(),
                "close() 之後仍有玩家掛在連線表上（session 洩漏）：" + connectedAfterClose);
    }

    /**
     * DISCONNECT frame 是非同步處理的，輪詢到期望的連線數再斷言。
     * 逾時就回傳當下的值讓斷言自己失敗（附上實際內容比 timeout 例外好讀）。
     */
    private Set<String> awaitConnectedPlayerCount(int expectedCount) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 5000L;
        Set<String> connected = registry.getConnectedPlayerIds(gameId);
        while (connected.size() != expectedCount && System.currentTimeMillis() < deadline) {
            Thread.sleep(20L);
            connected = registry.getConnectedPlayerIds(gameId);
        }
        return connected;
    }
}

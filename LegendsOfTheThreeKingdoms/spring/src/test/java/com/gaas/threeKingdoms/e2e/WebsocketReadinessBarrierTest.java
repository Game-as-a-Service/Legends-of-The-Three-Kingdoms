package com.gaas.threeKingdoms.e2e;

import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gaas.threeKingdoms.e2e.WebsocketUtil.PLAYER_KEYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 守 {@code @BeforeEach} 的就緒屏障（取代原本的 {@code Thread.sleep(1000)}）。
 * <p>
 * 屏障要成立必須同時滿足兩件事，這裡各有一支測試：
 * <ol>
 *   <li>回來時 7 條訂閱**真的收得到推播** —— 否則測試建局後的第一批事件會被 SimpleBroker
 *       當成「沒有 subscriber」直接丟棄（不排隊），永久漏事件。</li>
 *   <li>屏障自己用的哨兵**不會留在佇列裡** —— 哨兵是會重送的，若殘留就會被下一個
 *       {@code getValue()} 讀到，變成整包訊息位移，也就是 issue #249 的失敗形態。</li>
 * </ol>
 */
public class WebsocketReadinessBarrierTest extends AbstractBaseIntegrationTest {

    WebsocketReadinessBarrierTest() {
        this.gameId = "websocket-readiness-barrier";
    }

    @DisplayName("屏障回來後，往 7 個 destination 送的推播每一條都收得到")
    @Test
    public void everySubscriptionIsDeliverableRightAfterSetup() {
        // Given @BeforeEach 的 awaitSubscriptionsReady 已經回來
        // When 立刻推播（不再多睡任何時間）
        for (String playerId : PLAYER_KEYS) {
            messagingTemplate.convertAndSend(destinationOf(playerId), payloadFor(playerId));
        }

        // Then 每條訂閱都收到自己那則
        for (String playerId : PLAYER_KEYS) {
            assertEquals(payloadFor(playerId), websocketUtil.getValue(playerId),
                    playerId + " 的訂閱在屏障回來後仍收不到推播（屏障沒擋住）");
        }
    }

    @DisplayName("屏障用的哨兵不會殘留在佇列裡")
    @Test
    public void sentinelsNeverLeakIntoTestQueues() throws Exception {
        // Given @BeforeEach 期間為了等就緒，往每個 destination 送了數枚哨兵
        // 給還在路上的哨兵時間抵達，否則這支測試會因為「還沒到」而假綠
        Thread.sleep(200L);

        // When / Then 佇列裡不該有任何東西 —— 有的話下一個 getValue() 就會位移
        for (String playerId : PLAYER_KEYS) {
            assertNull(websocketUtil.pollNow(playerId),
                    playerId + " 的佇列在 setup 後就有殘留訊息（哨兵沒被過濾掉）");
        }

        // 佇列本身是通的（上面的 assertNull 不是因為訂閱壞掉才空）
        messagingTemplate.convertAndSend(destinationOf("player-a"), payloadFor("player-a"));
        assertNotNull(websocketUtil.getValue("player-a"));
    }

    private String destinationOf(String playerId) {
        return String.format("/websocket/legendsOfTheThreeKingdoms/%s/%s", gameId, playerId);
    }

    private String payloadFor(String playerId) {
        return "{\"event\":\"BarrierProbe\",\"playerId\":\"" + playerId + "\"}";
    }
}

package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.events.ContentmentEvent;
import com.gaas.threeKingdoms.events.LightningEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gaas.threeKingdoms.handcard.PlayCard.findById;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 判定訊息要直接寫出判定牌（花色 + 點數 + 牌名），不能只給卡牌 id。
 * 使用者回報：「樂不思蜀效果判定！抽出卡牌BD2080，花色是方塊，效果成功」看不出是哪張牌。
 */
public class JudgementMessageTest {

    @DisplayName("點數 10 顯示為「10」而不是 representation 的「0」")
    @Test
    public void rankTenIsShownAsTen() {
        assertEquals("黑桃10 殺", findById("BS0010").judgementDescription());
        assertEquals("紅心10 殺", findById("BH0036").judgementDescription());
    }

    @DisplayName("樂不思蜀判定訊息寫出判定牌與結果")
    @Test
    public void contentmentMessageShowsJudgementCard() {
        assertEquals("樂不思蜀判定：方塊2 閃 → 非紅心，生效，跳過出牌階段",
                new ContentmentEvent(true, "player-a", findById("BD2080")).getMessage());
        assertEquals("樂不思蜀判定：紅心3 桃 → 紅心，未生效",
                new ContentmentEvent(false, "player-a", findById("BH3029")).getMessage());
    }

    @DisplayName("閃電判定訊息寫出判定牌與結果")
    @Test
    public void lightningMessageShowsJudgementCard() {
        assertEquals("閃電判定：黑桃10 殺 → 未生效",
                new LightningEvent(false, "player-a", findById("BS0010")).getMessage());
        assertEquals("閃電判定：紅心3 桃 → 未生效",
                new LightningEvent(false, "player-a", findById("BH3029")).getMessage());
    }

    @DisplayName("判定事件仍帶判定牌 id，前端可據以顯示牌面")
    @Test
    public void eventsStillCarryDrawCardId() {
        assertEquals("BD2080", new ContentmentEvent(true, "player-a", findById("BD2080")).getDrawCardId());
        assertEquals("BH3029", new LightningEvent(false, "player-a", findById("BH3029")).getDrawCardId());
    }
}

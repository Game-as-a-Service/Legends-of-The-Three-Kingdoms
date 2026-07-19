package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #214：南蠻入侵 + 奸雄 + 無懈可擊。
 * B=曹操 持有無懈可擊，skip 南蠻扣血後奸雄 ACCEPT 取得南蠻牌；
 * 輪詢推進到 C 時會產生對 C 的無懈可擊詢問 —
 * 此時只有曹操該收到 AskPlayWardEvent，其他玩家收 WaitForWardEvent。
 * 根因：UseSkillEffectPresenter 未做 per-player 個人化（WaitForWard → AskPlayWard）。
 */
public class BarbarianJianXiongWardTest extends AbstractBaseIntegrationTest {

    @Test
    public void testJianXiongAcceptThenWardAsk_OnlyCaoCaoGetsAskPlayWardEvent() throws Exception {
        // A 出南蠻；B=曹操（有無懈可擊、無殺）；C、D 有殺
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new BarbarianInvasion(SS7007));
        Player playerB = createPlayer("player-b", 4, General.曹操, HealthStatus.ALIVE, Role.MINISTER,
                new Ward(SSJ011));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS8008));
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR,
                new Kill(BS9009));
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        repository.save(game);

        // A 出南蠻 → Phase 1 無懈詢問（B 持有）
        mockMvcUtil.playCard(gameId, "player-a", "player-a", SS7007.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        // B 放棄 phase 1 → phase 2 無懈詢問（目標 B 自己）
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        // B 放棄 → 被問殺
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        // B 不出殺 → 扣血 → 奸雄詢問
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());

        // 前面 4 個步驟各廣播一則訊息給每位玩家 — 逐一消費，下一則即奸雄 resolve 的廣播
        // （不能用 clearAllQueues：廣播非同步送達，清空時機有 race）
        for (String playerId : List.of("player-a", "player-b", "player-c", "player-d")) {
            for (int i = 0; i < 4; i++) {
                assertNotNull(websocketUtil.getValue(playerId), "前置步驟訊息未送達：" + playerId);
            }
        }

        // B ACCEPT 奸雄 → 輪詢推進到 C，B 仍持無懈 → 無懈詢問（issue #214 的爆點）
        mockMvcUtil.useJianXiongEffect(gameId, "player-b", "ACCEPT")
                .andExpect(status().isOk());

        String messageB = websocketUtil.getValue("player-b");
        String messageC = websocketUtil.getValue("player-c");
        String messageD = websocketUtil.getValue("player-d");

        assertTrue(messageB.contains("AskPlayWardEvent"),
                "issue #214：持有無懈可擊的曹操應收到 AskPlayWardEvent");
        assertFalse(messageC.contains("AskPlayWardEvent"), "C 沒有無懈可擊，不應被詢問");
        assertTrue(messageC.contains("WaitForWardEvent"), "C 應收到等待無懈可擊 event");
        assertFalse(messageD.contains("AskPlayWardEvent"), "D 沒有無懈可擊，不應被詢問");
        assertTrue(messageD.contains("WaitForWardEvent"), "D 應收到等待無懈可擊 event");

        // B 放棄無懈 → C 被問殺，流程繼續到結束（不卡住）
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals("player-c", saved.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.playCard(gameId, "player-c", "player-a", BS8008.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        // B 手上仍有無懈（skip 不棄牌）→ 輪到 D 前再問一次無懈，B 放棄
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-d", "player-a", BS9009.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getTopBehavior().isEmpty(), "南蠻流程應完整結束");
        assertTrue(saved.getPlayer("player-b").getHand().getCards().stream()
                .anyMatch(c -> c.getId().equals(SS7007.getCardId())), "奸雄取得南蠻牌");
        assertEquals(3, saved.getPlayer("player-b").getHP());
        assertEquals(4, saved.getPlayer("player-c").getHP());
        assertEquals(4, saved.getPlayer("player-d").getHP());
    }

    /**
     * 奸雄 resolve 後 WaitingJianXiongResponseBehavior（card=null）會被新的無懈詢問
     * 壓在 stack 中間、來不及移除；此時 B「確定發動」無懈可擊曾觸發
     * NPE：getCard() is null（WardBehavior 組抵銷訊息時撞到殘留 behavior）。
     */
    @Test
    public void testJianXiongAcceptThenPlayWardActive_NoNPE() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new BarbarianInvasion(SS7007));
        Player playerB = createPlayer("player-b", 4, General.曹操, HealthStatus.ALIVE, Role.MINISTER,
                new Ward(SSJ011));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS8008));
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR,
                new Kill(BS9009));
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-a", SS7007.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.useJianXiongEffect(gameId, "player-b", "ACCEPT")
                .andExpect(status().isOk());

        // B 確定發動無懈可擊保護 C —— 修復前此步 500（NPE）
        mockMvcUtil.playWardCard(gameId, "player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals("player-d", saved.getCurrentRound().getActivePlayer().getId(), "C 被無懈保護後輪詢推進到 D");

        mockMvcUtil.playCard(gameId, "player-d", "player-a", BS9009.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getTopBehavior().isEmpty(), "南蠻流程應完整結束");
        assertEquals(3, saved.getPlayer("player-b").getHP());
        assertEquals(4, saved.getPlayer("player-c").getHP(), "C 被無懈保護不扣血");
        assertEquals(4, saved.getPlayer("player-d").getHP(), "D 出殺不扣血");
    }
}

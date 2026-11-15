package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.DyingAskPeachBehavior;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingJianXiongResponseBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.basiccard.VirtualKill;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 奸雄 × 轉化殺（跨 HTTP 請求，每步經 MongoDB 讀回）：
 * 關羽武聖把紅桃閃當殺打曹操 → 曹操可獲得被轉化的真實來源牌。
 * 座位 a（關羽，回合主）→ b（曹操）→ c → d。
 */
public class JianXiongConversionKillFlowTest extends AbstractBaseIntegrationTest {

    private static final String SKIP = PlayType.SKIP.getPlayType();

    private void givenGuanYuAndCaoCao(int caoCaoHp) {
        Player a = createPlayer("player-a", 4, General.關羽, HealthStatus.ALIVE, Role.MONARCH, new Dodge(BH2028));
        Player b = createPlayer("player-b", caoCaoHp, General.曹操, HealthStatus.ALIVE, Role.REBEL);
        Player c = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER, new Peach(BH3029));
        Player d = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        repository.save(initGame(gameId, Arrays.asList(a, b, c, d), a));
    }

    private void wuShengKillOnCaoCao() throws Exception {
        mockMvcUtil.useSkillEffect(gameId, "player-a", "武聖", "KILL", List.of(BH2028.getCardId()), "player-b")
                .andExpect(status().isOk());
        Game asked = repository.findById(gameId).orElseThrow();
        NormalActiveKillBehavior kill = assertInstanceOf(NormalActiveKillBehavior.class, asked.peekTopBehavior());
        VirtualKill virtualKill = assertInstanceOf(VirtualKill.class, kill.getCard(),
                "轉化殺從 MongoDB 讀回仍是 VirtualKill");
        assertEquals(List.of(BH2028.getCardId()), virtualKill.getSourceCardIds());
    }

    @DisplayName("武聖殺打曹操 → 不出閃 → 問奸雄（來源牌 BH2028）→ ACCEPT → 曹操手上多一張 BH2028")
    @Test
    public void wuShengKill_jianXiongAccept() throws Exception {
        givenGuanYuAndCaoCao(4);
        wuShengKillOnCaoCao();
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", SKIP).andExpect(status().isOk());

        Game asking = repository.findById(gameId).orElseThrow();
        WaitingJianXiongResponseBehavior waiting =
                assertInstanceOf(WaitingJianXiongResponseBehavior.class, asking.peekTopBehavior());
        assertEquals(List.of(BH2028.getCardId()), waiting.getSourceCardIds());
        assertEquals("player-b", asking.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.useJianXiongEffect(gameId, "player-b", "ACCEPT").andExpect(status().isOk());

        Game done = repository.findById(gameId).orElseThrow();
        assertEquals(3, done.getPlayer("player-b").getHP());
        assertTrue(done.getPlayer("player-b").getHand().getCard(BH2028.getCardId()).isPresent());
        assertFalse(done.getGraveyard().contains(BH2028.getCardId()));
        assertTrue(done.isTopBehaviorEmpty(), done.getTopBehavior().toString());
        assertEquals("player-a", done.getCurrentRound().getActivePlayer().getId());
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
    }

    @DisplayName("武聖殺打到 1 血曹操 → 瀕死（pending 來源牌存進 MongoDB）→ 救回 → 補問奸雄 → ACCEPT 拿 BH2028")
    @Test
    public void wuShengKill_dyingRescued_jianXiongAccept() throws Exception {
        givenGuanYuAndCaoCao(1);
        wuShengKillOnCaoCao();
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", SKIP).andExpect(status().isOk());

        Game dying = repository.findById(gameId).orElseThrow();
        DyingAskPeachBehavior dyingBehavior = assertInstanceOf(DyingAskPeachBehavior.class, dying.peekTopBehavior());
        assertEquals(List.of(BH2028.getCardId()), dyingBehavior.getPendingViperSpearDiscardCardIds(),
                "轉化殺的來源牌要從 MongoDB 讀回");

        mockMvcUtil.playCard(gameId, "player-b", "player-b", "", SKIP).andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-c", "player-b", BH3029.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        Game asking = repository.findById(gameId).orElseThrow();
        assertEquals(1, asking.getPlayer("player-b").getHP());
        WaitingJianXiongResponseBehavior waiting =
                assertInstanceOf(WaitingJianXiongResponseBehavior.class, asking.peekTopBehavior());
        assertEquals(List.of(BH2028.getCardId()), waiting.getSourceCardIds());

        mockMvcUtil.useJianXiongEffect(gameId, "player-b", "ACCEPT").andExpect(status().isOk());

        Game done = repository.findById(gameId).orElseThrow();
        assertTrue(done.getPlayer("player-b").getHand().getCard(BH2028.getCardId()).isPresent());
        assertTrue(done.isTopBehaviorEmpty(), done.getTopBehavior().toString());
        assertEquals("player-a", done.getCurrentRound().getActivePlayer().getId());
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
    }
}

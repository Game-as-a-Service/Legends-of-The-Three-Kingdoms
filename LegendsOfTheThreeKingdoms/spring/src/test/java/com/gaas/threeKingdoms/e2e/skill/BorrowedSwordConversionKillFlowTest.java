package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingJianXiongResponseBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.VirtualKill;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
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
 * 借刀殺人 × 龍膽轉化殺（跨 HTTP 請求，每步經 MongoDB 讀回）：
 * 趙雲被借刀，龍膽把閃當殺打曹操 → 殺 behavior 是一般殺（不是丈八蛇矛），讀回後仍帶來源牌，
 * 曹操不出閃受傷 → 奸雄可獲得那張閃。
 * 座位 a（甘寧，回合主）→ b（趙雲，裝諸葛連弩）→ c（曹操）→ d。
 */
public class BorrowedSwordConversionKillFlowTest extends AbstractBaseIntegrationTest {

    @DisplayName("借刀殺人：趙雲龍膽閃當殺打曹操 → 讀回是 NormalActiveKill(VirtualKill[BH2028]) → 不出閃 → 奸雄 ACCEPT 拿 BH2028")
    @Test
    public void longDanKillForBorrowedSword_jianXiongTakesSourceCard() throws Exception {
        Player a = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH, new BorrowedSword(SCK065));
        Player b = createPlayer("player-b", 4, General.趙雲, HealthStatus.ALIVE, Role.REBEL,
                new Dodge(BH2028), new Kill(BS9009));
        b.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));
        Player c = createPlayer("player-c", 4, General.曹操, HealthStatus.ALIVE, Role.MINISTER);
        Player d = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        repository.save(initGame(gameId, Arrays.asList(a, b, c, d), a));

        mockMvcUtil.playCard(gameId, "player-a", "player-b", SCK065.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c").andExpect(status().isOk());
        mockMvcUtil.useSkillEffect(gameId, "player-b", "龍膽", "KILL", List.of(BH2028.getCardId()), "player-c")
                .andExpect(status().isOk());

        Game asked = repository.findById(gameId).orElseThrow();
        NormalActiveKillBehavior kill = assertInstanceOf(NormalActiveKillBehavior.class, asked.peekTopBehavior());
        assertEquals(NormalActiveKillBehavior.class, kill.getClass(), "不是 ViperSpearKillBehavior");
        assertEquals(BH2028.getCardId(), kill.getCardId());
        VirtualKill virtualKill = assertInstanceOf(VirtualKill.class, kill.getCard(), "讀回仍是轉化殺");
        assertEquals(List.of(BH2028.getCardId()), virtualKill.getSourceCardIds());
        assertEquals("player-c", asked.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.playCard(gameId, "player-c", "player-b", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());

        Game asking = repository.findById(gameId).orElseThrow();
        assertEquals(3, asking.getPlayer("player-c").getHP());
        WaitingJianXiongResponseBehavior waiting =
                assertInstanceOf(WaitingJianXiongResponseBehavior.class, asking.peekTopBehavior());
        assertEquals(List.of(BH2028.getCardId()), waiting.getSourceCardIds());

        mockMvcUtil.useJianXiongEffect(gameId, "player-c", "ACCEPT").andExpect(status().isOk());

        Game done = repository.findById(gameId).orElseThrow();
        assertTrue(done.getPlayer("player-c").getHand().getCard(BH2028.getCardId()).isPresent());
        assertTrue(done.isTopBehaviorEmpty(), done.getTopBehavior().toString());
        assertEquals("player-a", done.getCurrentRound().getActivePlayer().getId());
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
    }
}

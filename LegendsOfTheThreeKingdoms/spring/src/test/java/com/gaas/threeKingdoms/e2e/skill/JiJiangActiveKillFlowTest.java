package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.BorrowedSwordBehavior;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingJiJiangResponseBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
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
 * 激將主動出殺 / 借刀殺人代出殺（跨 HTTP 請求，每步經 MongoDB 讀回）。
 * 座位 a → b → c → d。
 */
public class JiJiangActiveKillFlowTest extends AbstractBaseIntegrationTest {

    @DisplayName("劉備主動激將 → 甘寧拒絕 → 張飛交殺 → 視為劉備殺 d，d 不閃扣血、劉備本回合不能再出殺")
    @Test
    public void activeJiJiang() throws Exception {
        Player a = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH, new Kill(BS9009));
        Player b = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player c = createPlayer("player-c", 4, General.關羽, HealthStatus.ALIVE, Role.MINISTER);
        Player d = createPlayer("player-d", 4, General.張飛, HealthStatus.ALIVE, Role.TRAITOR, new Kill(BS8008));
        repository.save(initGame(gameId, Arrays.asList(a, b, c, d), a));

        mockMvcUtil.useSkillEffect(gameId, "player-a", "激將", "ACCEPT", null, "player-b").andExpect(status().isOk());
        Game asking = repository.findById(gameId).orElseThrow();
        WaitingJiJiangResponseBehavior waiting =
                assertInstanceOf(WaitingJiJiangResponseBehavior.class, asking.peekTopBehavior());
        assertEquals(WaitingJiJiangResponseBehavior.Mode.ACTIVE_KILL, waiting.getMode(), "MongoDB 讀回模式");
        assertEquals("player-b", waiting.getTargetPlayerId(), "MongoDB 讀回目標");
        assertEquals("player-c", asking.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.useSkillEffect(gameId, "player-c", "激將", "DECLINE", null, null).andExpect(status().isOk());
        Game next = repository.findById(gameId).orElseThrow();
        assertEquals(1, ((WaitingJiJiangResponseBehavior) next.peekTopBehavior()).getCurrentIndex());
        assertEquals("player-d", next.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.useSkillEffect(gameId, "player-d", "激將", "ACCEPT", List.of(BS8008.getCardId()), null)
                .andExpect(status().isOk());
        Game killing = repository.findById(gameId).orElseThrow();
        NormalActiveKillBehavior kill = assertInstanceOf(NormalActiveKillBehavior.class, killing.peekTopBehavior());
        assertEquals("player-a", kill.getBehaviorPlayer().getId());
        assertEquals(List.of("player-b"), kill.getReactionPlayers());
        assertEquals("player-b", killing.getCurrentRound().getActivePlayer().getId());
        assertEquals(0, killing.getPlayer("player-d").getHandSize());

        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());
        Game done = repository.findById(gameId).orElseThrow();
        assertEquals(3, done.getPlayer("player-b").getHP());
        assertTrue(done.getTopBehavior().isEmpty());
        assertTrue(done.getCurrentRound().isShowKill());

        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS9009.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().is4xxClientError());
    }

    @DisplayName("借刀殺人令無殺的劉備殺 a → 劉備發動激將 → 關羽交殺 → a 不閃扣血，劉備保留武器")
    @Test
    public void borrowedSwordJiJiang() throws Exception {
        Player a = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL, new BorrowedSword(SCK065));
        Player b = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH);
        b.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));
        Player c = createPlayer("player-c", 4, General.關羽, HealthStatus.ALIVE, Role.MINISTER, new Kill(BS8008));
        Player d = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        repository.save(initGame(gameId, Arrays.asList(a, b, c, d), a));

        mockMvcUtil.playCard(gameId, "player-a", "player-b", SCK065.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-a").andExpect(status().isOk());
        Game asked = repository.findById(gameId).orElseThrow();
        assertInstanceOf(BorrowedSwordBehavior.class, asked.peekTopBehavior(), "劉備無殺但可激將 → 不直接交武器");
        assertEquals("player-b", asked.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.useSkillEffect(gameId, "player-b", "激將", "ACCEPT", null, null).andExpect(status().isOk());
        Game polling = repository.findById(gameId).orElseThrow();
        WaitingJiJiangResponseBehavior waiting =
                assertInstanceOf(WaitingJiJiangResponseBehavior.class, polling.peekTopBehavior());
        assertEquals(WaitingJiJiangResponseBehavior.Mode.BORROWED_SWORD, waiting.getMode());
        assertEquals("player-a", waiting.getTargetPlayerId());

        mockMvcUtil.useSkillEffect(gameId, "player-c", "激將", "ACCEPT", List.of(BS8008.getCardId()), null)
                .andExpect(status().isOk());
        Game killing = repository.findById(gameId).orElseThrow();
        NormalActiveKillBehavior kill = assertInstanceOf(NormalActiveKillBehavior.class, killing.peekTopBehavior());
        assertEquals("player-b", kill.getBehaviorPlayer().getId());
        assertEquals("player-a", killing.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());
        Game done = repository.findById(gameId).orElseThrow();
        assertEquals(3, done.getPlayer("player-a").getHP());
        assertNotNull(done.getPlayer("player-b").getEquipmentWeaponCard());
        assertTrue(done.getTopBehavior().isEmpty());
    }

    @DisplayName("借刀殺人激將全部拒絕、劉備無殺 → 武器交給 a")
    @Test
    public void borrowedSwordJiJiangAllDecline() throws Exception {
        Player a = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL, new BorrowedSword(SCK065));
        Player b = createPlayer("player-b", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH);
        b.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));
        Player c = createPlayer("player-c", 4, General.關羽, HealthStatus.ALIVE, Role.MINISTER);
        Player d = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        repository.save(initGame(gameId, Arrays.asList(a, b, c, d), a));

        mockMvcUtil.playCard(gameId, "player-a", "player-b", SCK065.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-a").andExpect(status().isOk());
        mockMvcUtil.useSkillEffect(gameId, "player-b", "激將", "ACCEPT", null, null).andExpect(status().isOk());
        mockMvcUtil.useSkillEffect(gameId, "player-c", "激將", "SKIP", null, null).andExpect(status().isOk());

        Game done = repository.findById(gameId).orElseThrow();
        assertNull(done.getPlayer("player-b").getEquipmentWeaponCard());
        assertTrue(done.getPlayer("player-a").getHand().getCard(ECA066.getCardId()).isPresent());
        assertTrue(done.getTopBehavior().isEmpty());
        assertEquals("player-a", done.getCurrentRound().getActivePlayer().getId());
    }
}

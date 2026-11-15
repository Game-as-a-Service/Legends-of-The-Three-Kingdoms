package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.JianXiongCompatibleTopBehavior;
import com.gaas.threeKingdoms.behavior.behavior.HeavenlyDoubleHalberdKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingJianXiongResponseBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.HeavenlyDoubleHalberdCard;
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
 * 方天畫戟中途目標受傷後技能（反饋／奸雄）要結算完才輪到下一位（跨 HTTP 請求，每步經 MongoDB 讀回）。
 * 座位 a → b → c → d。
 */
public class HalberdDamagedSkillFlowTest extends AbstractBaseIntegrationTest {

    private void givenHalberdOn(General b) {
        Player a = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH, new Kill(BS8008));
        a.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        Player playerB = createPlayer("player-b", 4, b, HealthStatus.ALIVE, Role.REBEL);
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        repository.save(initGame(gameId, Arrays.asList(a, playerB, playerC, playerD), a));
    }

    private void thenCIsAskedAndKillFinishes() throws Exception {
        Game resumed = repository.findById(gameId).orElseThrow();
        assertInstanceOf(HeavenlyDoubleHalberdKillBehavior.class, resumed.peekTopBehavior());
        assertEquals("player-c", resumed.getCurrentRound().getActivePlayer().getId(), "技能結算後才輪到 c");

        mockMvcUtil.playCard(gameId, "player-c", "player-a", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());
        Game done = repository.findById(gameId).orElseThrow();
        assertEquals(3, done.getPlayer("player-c").getHP());
        assertTrue(done.getTopBehavior().isEmpty(), done.getTopBehavior().toString());
        assertEquals("player-a", done.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("方天畫戟 [司馬懿, c]：司馬懿受傷 → 問反饋（c 還沒被問）→ 反饋放棄 → 輪到 c")
    @Test
    public void fanKuiThenNextTarget() throws Exception {
        givenHalberdOn(General.司馬懿);
        mockMvcUtil.useHeavenlyDoubleHalberdKill(gameId, "player-a", BS8008.getCardId(), List.of("player-b", "player-c"))
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());

        Game asking = repository.findById(gameId).orElseThrow();
        WaitingSkillEffectBehavior fanKui = assertInstanceOf(WaitingSkillEffectBehavior.class, asking.peekTopBehavior());
        assertEquals("反饋", fanKui.getSkillName());
        assertEquals("player-b", asking.getCurrentRound().getActivePlayer().getId());
        HeavenlyDoubleHalberdKillBehavior halberd = (HeavenlyDoubleHalberdKillBehavior) asking.getTopBehavior().get(0);
        assertEquals("player-b", halberd.getParam(JianXiongCompatibleTopBehavior.PARAM_DEFERRED_ADVANCE_PLAYER_ID),
                "待推進標記（MongoDB 讀回）");

        mockMvcUtil.useSkillEffect(gameId, "player-b", "反饋", "SKIP", null, null).andExpect(status().isOk());
        thenCIsAskedAndKillFinishes();
    }

    @DisplayName("方天畫戟 [曹操, c]：曹操受傷 → 問奸雄（c 還沒被問）→ 奸雄收殺 → 輪到 c")
    @Test
    public void jianXiongThenNextTarget() throws Exception {
        givenHalberdOn(General.曹操);
        mockMvcUtil.useHeavenlyDoubleHalberdKill(gameId, "player-a", BS8008.getCardId(), List.of("player-b", "player-c"))
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());

        Game asking = repository.findById(gameId).orElseThrow();
        assertInstanceOf(WaitingJianXiongResponseBehavior.class, asking.peekTopBehavior());
        assertEquals("player-b", asking.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.useJianXiongEffect(gameId, "player-b", "ACCEPT").andExpect(status().isOk());
        assertTrue(repository.findById(gameId).orElseThrow().getPlayer("player-b").getHand().getCards().stream()
                .anyMatch(c -> c.getId().equals(BS8008.getCardId())), "曹操收下殺");
        thenCIsAskedAndKillFinishes();
    }
}

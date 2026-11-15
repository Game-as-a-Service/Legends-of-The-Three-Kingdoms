package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.BarbarianInvasionBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 南蠻輪詢中司馬懿瀕死 → 救回 → 補問反饋 → 反饋結算後才 resume 輪詢（跨 HTTP 請求，每步經 MongoDB）。
 * 「從誰之後繼續輪詢」的標記要持久化在南蠻 behavior 上，反饋那個請求才讀得到。
 */
public class AoeRevivalFanKuiFlowTest extends AbstractBaseIntegrationTest {

    @DisplayName("南蠻：司馬懿瀕死救回 → 只問反饋（輪詢暫停）→ 反饋 SKIP → 輪到 c 出殺")
    @Test
    public void revivalFanKuiDefersBarbarianPolling() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new BarbarianInvasion(SS7007), new Dodge(BH2028));
        Player siMaYi = createPlayer("player-b", 1, General.司馬懿, HealthStatus.ALIVE, Role.REBEL);
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER,
                new Peach(BH3029), new Kill(BS9009));
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(playerA, siMaYi, playerC, playerD), playerA);
        repository.save(game);

        String skip = PlayType.SKIP.getPlayType();
        mockMvcUtil.playCard(gameId, "player-a", "player-a", SS7007.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", skip).andExpect(status().isOk());     // 1 → 0
        mockMvcUtil.playCard(gameId, "player-b", "player-b", "", skip).andExpect(status().isOk());     // 自己不出桃
        mockMvcUtil.playCard(gameId, "player-c", "player-b", BH3029.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());                                                         // c 救回

        Game asking = repository.findById(gameId).orElseThrow();
        WaitingSkillEffectBehavior fanKui = assertInstanceOf(WaitingSkillEffectBehavior.class, asking.peekTopBehavior());
        assertEquals("反饋", fanKui.getSkillName());
        assertEquals("player-b", asking.getCurrentRound().getActivePlayer().getId(), "輪詢暫停，先等司馬懿決定反饋");

        mockMvcUtil.useSkillEffect(gameId, "player-b", "反饋", "SKIP", null, null).andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertInstanceOf(BarbarianInvasionBehavior.class, saved.peekTopBehavior());
        assertEquals("player-c", saved.getCurrentRound().getActivePlayer().getId(), "反饋結算完才問 c 出殺");
        mockMvcUtil.playCard(gameId, "player-c", "player-a", BS9009.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
    }
}

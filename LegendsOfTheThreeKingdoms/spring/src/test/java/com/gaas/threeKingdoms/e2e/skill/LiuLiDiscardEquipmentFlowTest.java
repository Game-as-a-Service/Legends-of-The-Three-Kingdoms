package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
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
 * 流離可棄裝備（跨 HTTP 請求，每步經 MongoDB）：
 * 大喬沒手牌、只裝赤兔(-1) → 被殺時仍問流離 → 棄赤兔轉給 d（距離以棄牌前計算）→ d 被問閃。
 */
public class LiuLiDiscardEquipmentFlowTest extends AbstractBaseIntegrationTest {

    @DisplayName("大喬只有 -1 馬：問流離 → 棄 -1 馬轉給 d → 赤兔進棄牌堆、d 被問閃")
    @Test
    public void discardMinusHorseToRedirect() throws Exception {
        Player attacker = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH, new Kill(BS8008));
        Player daQiao = createPlayer("player-b", 3, General.大喬, HealthStatus.ALIVE, Role.REBEL);
        daQiao.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(attacker, daQiao, playerC, playerD), attacker);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        Game asking = repository.findById(gameId).orElseThrow();
        WaitingSkillEffectBehavior liuLi = assertInstanceOf(WaitingSkillEffectBehavior.class, asking.peekTopBehavior());
        assertEquals("流離", liuLi.getSkillName(), "沒手牌但有裝備也要問流離");

        mockMvcUtil.useSkillEffect(gameId, "player-b", "流離", "ACCEPT", List.of(EH5044.getCardId()), "player-d")
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        Player savedDaQiao = saved.getPlayer("player-b");
        assertNull(savedDaQiao.getEquipment().getMinusOne(), "赤兔移出裝備區");
        assertTrue(saved.getGraveyard().contains(EH5044.getCardId()), "赤兔進棄牌堆");
        assertEquals(3, savedDaQiao.getHP());
        NormalActiveKillBehavior kill = assertInstanceOf(NormalActiveKillBehavior.class, saved.peekTopBehavior());
        assertEquals("player-d", kill.getCurrentReactionPlayer().getId(), "殺轉給 d");
        assertEquals("player-d", saved.getCurrentRound().getActivePlayer().getId());
    }
}

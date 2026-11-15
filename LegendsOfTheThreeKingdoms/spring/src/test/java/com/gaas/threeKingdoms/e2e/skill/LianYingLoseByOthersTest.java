package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 連營（陸遜）：被他人移走最後一張手牌也要摸牌 — 走 HTTP 多請求 + MongoDB reload。
 */
public class LianYingLoseByOthersTest extends AbstractBaseIntegrationTest {

    @DisplayName("過河拆橋拆掉陸遜最後一張手牌（出牌、選牌兩個請求）→ 連營摸一張")
    @Test
    public void dismantleLastHandCard_luXunDrawsOne() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Dismantle(SS4004));
        Player playerB = createPlayer("player-b", 4, General.陸遜, HealthStatus.ALIVE, Role.MINISTER,
                new Peach(BH3029));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        repository.save(initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA));

        mockMvcUtil.playCard(gameId, "player-a", "player-b", SS4004.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.useDismantleEffect(gameId, "player-a", "player-b", null, 0)
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        Player luXun = saved.getPlayer("player-b");
        assertEquals(1, luXun.getHandSize(), "被拆光 → 連營摸 1");
        assertTrue(luXun.getHand().getCard(BH3029.getCardId()).isEmpty(), "原本的桃已被拆進墓地");
    }

    @DisplayName("陸遜殺司馬懿，反饋拿走陸遜最後一張手牌（出殺、不閃、反饋三個請求）→ 連營摸一張")
    @Test
    public void fanKuiTakesLastHandCard_luXunDrawsOne() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.陸遜, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029));
        Player playerB = createPlayer("player-b", 4, General.司馬懿, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        repository.save(initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA));

        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.useSkillEffect(gameId, "player-b", "反饋", "ACCEPT", null, null)
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getPlayer("player-b").getHand().getCard(BH3029.getCardId()).isPresent(),
                "司馬懿拿到陸遜的桃");
        assertEquals(1, saved.getPlayer("player-a").getHandSize(), "被反饋拿光 → 連營摸 1");
        assertTrue(saved.isTopBehaviorEmpty());
    }
}

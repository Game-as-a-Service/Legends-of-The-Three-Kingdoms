package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.Snatch;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 陸遜 謙遜（標準版）走 HTTP 路徑：順手牽羊/樂不思蜀指定陸遜 → 400；南蠻入侵對陸遜照常生效。
 * 使用者回報「謙遜技能未生效」— 根因是 SnatchBehaviorHandler 完全沒檢查目標免疫技。
 */
public class QianXunImmunityTest extends AbstractBaseIntegrationTest {

    /** a=甘寧（回合主，持順手牽羊/樂不思蜀/南蠻），b=陸遜（謙遜），c/d=甘寧。 */
    private void givenLuXunAtSeatB() {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Snatch(SS3016), new Contentment(SS6006), new BarbarianInvasion(SS7007));
        Player playerB = createPlayer("player-b", 4, General.陸遜, HealthStatus.ALIVE, Role.MINISTER,
                new Peach(BH3029));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        repository.save(game);
    }

    @DisplayName("順手牽羊指定陸遜 → 400，且陸遜手牌未被拿走")
    @Test
    public void snatchTargetingLuXun_returns400() throws Exception {
        givenLuXunAtSeatB();

        mockMvcUtil.playCard(gameId, "player-a", "player-b", SS3016.getCardId(),
                        PlayType.ACTIVE.getPlayType())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("IllegalStateException"));

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(1, saved.getPlayer("player-b").getHandSize(), "陸遜手牌不該被拿走");
        assertEquals(3, saved.getPlayer("player-a").getHandSize(), "順手牽羊不該被打出");
    }

    @DisplayName("樂不思蜀指定陸遜 → 400，判定區無樂不思蜀")
    @Test
    public void contentmentTargetingLuXun_returns400() throws Exception {
        givenLuXunAtSeatB();

        mockMvcUtil.playCard(gameId, "player-a", "player-b", SS6006.getCardId(),
                        PlayType.ACTIVE.getPlayType())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("IllegalStateException"));

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(0, saved.getPlayer("player-b").getDelayScrollCardIds().size());
    }

    @DisplayName("南蠻入侵對陸遜照常生效（標準版謙遜不免疫 AOE）")
    @Test
    public void barbarianInvasionStillHitsLuXun() throws Exception {
        givenLuXunAtSeatB();

        mockMvcUtil.playCard(gameId, "player-a", "player-a", SS7007.getCardId(),
                PlayType.ACTIVE.getPlayType()).andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(3, saved.getPlayer("player-b").getHP(), "陸遜應被南蠻打掉 1 點血");
    }
}

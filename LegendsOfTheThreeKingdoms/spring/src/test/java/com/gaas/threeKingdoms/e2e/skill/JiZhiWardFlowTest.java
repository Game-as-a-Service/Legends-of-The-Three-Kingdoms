package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.SomethingForNothing;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.round.Stage;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 集智 × 無懈可擊（跨請求，經 MongoDB reload）：黃月英每出一張無懈可擊摸一張，
 * 反制鏈 stack / stage / activePlayer 不受影響。
 */
public class JiZhiWardFlowTest extends AbstractBaseIntegrationTest {

    private static final String ACTIVE = PlayType.ACTIVE.getPlayType();

    @Test
    public void huangYueYingWardsAndCounterWards_drawsOncePerWard() throws Exception {
        // A=甘寧 出無中生有；B=黃月英 兩張無懈；C 一張無懈
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new SomethingForNothing(SH7046));
        Player playerB = createPlayer("player-b", 3, General.黃月英, HealthStatus.ALIVE, Role.MINISTER,
                new Ward(SSJ011), new Ward(SCQ077));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL,
                new Ward(SCK078));
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        // 牌堆頂放殺，避免集智摸到無懈改變反制名單
        game.getDeck().putBackOnTop(List.of(new Kill(BS8008), new Kill(BS9009), new Kill(BS0010)));
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-a", SH7046.getCardId(), ACTIVE)
                .andExpect(status().isOk());

        // B 出第一張無懈 → 集智摸一張（殺 BS8008）
        mockMvcUtil.playWardCard(gameId, "player-b", SSJ011.getCardId(), ACTIVE)
                .andExpect(status().isOk());
        Game saved = repository.findById(gameId).orElseThrow();
        Player b = saved.getPlayer("player-b");
        assertEquals(2, b.getHandSize(), "2 - 1 + 集智 1");
        assertTrue(b.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BS8008.getCardId())));
        assertEquals(Stage.Wait_Accept_Ward_Effect, saved.getCurrentRound().getStage());

        // C 反制 → B 不摸
        mockMvcUtil.playWardCard(gameId, "player-c", SCK078.getCardId(), ACTIVE)
                .andExpect(status().isOk());
        saved = repository.findById(gameId).orElseThrow();
        assertEquals(2, saved.getPlayer("player-b").getHandSize());
        assertEquals(Stage.Wait_Accept_Ward_Effect, saved.getCurrentRound().getStage());

        // B 再反制 → 集智再摸一張（殺 BS9009）；三張無懈 → 無中生有被抵銷
        mockMvcUtil.playWardCard(gameId, "player-b", SCQ077.getCardId(), ACTIVE)
                .andExpect(status().isOk());
        saved = repository.findById(gameId).orElseThrow();
        b = saved.getPlayer("player-b");
        assertEquals(2, b.getHandSize(), "2 - 1 + 集智 1");
        assertTrue(b.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BS9009.getCardId())));
        assertTrue(b.getHand().getCards().stream().noneMatch(Ward.class::isInstance));
        assertEquals(0, saved.getPlayer("player-a").getHandSize(), "無中生有被抵銷");
        assertTrue(saved.getTopBehavior().isEmpty());
        assertEquals(Stage.Normal, saved.getCurrentRound().getStage());
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId());

        // 每個請求各推一則給 B：出無中生有、B 無懈、C 無懈、B 無懈
        List<String> toB = new java.util.ArrayList<>();
        for (int k = 0; k < 4; k++) {
            String message = websocketUtil.getValue("player-b");
            assertNotNull(message, "第 " + (k + 1) + " 則推播未送達");
            toB.add(message);
        }
        assertTrue(toB.get(1).contains("\"DrawCardEvent\"") && toB.get(1).contains(BS8008.getCardId()),
                "B 第一張無懈的推播應包含集智摸牌");
        assertFalse(toB.get(2).contains("\"DrawCardEvent\""), "C 出無懈不觸發摸牌");
        assertTrue(toB.get(3).contains("\"DrawCardEvent\"") && toB.get(3).contains(BS9009.getCardId()),
                "B 第二張無懈的推播應包含集智摸牌");
        // 他人看不到 B 摸到的牌
        String toC = null;
        for (int k = 0; k < 4; k++) {
            toC = websocketUtil.getValue("player-c");
        }
        assertNotNull(toC);
        assertTrue(toC.contains("\"DrawCardEvent\""));
        assertFalse(toC.contains(BS9009.getCardId()), "他人看不到 B 集智摸到的牌");
    }
}

package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.DyingAskPeachBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 反間猜錯讓目標 HP 歸 0 → 瀕死求桃（跨多個 HTTP 請求，每步經 MongoDB 重建 behavior）。
 * 傷害來源（周瑜）要存進 DyingAskPeachBehavior，目標死亡時擊殺獎勵才會給周瑜。
 */
public class FanJianDyingFlowTest extends AbstractBaseIntegrationTest {

    private static final String SKIP = PlayType.SKIP.getPlayType();

    /** a = 周瑜（主公、回合主，手上一張黑桃8 殺）；c = 反賊 1 血。 */
    private Game givenZhouYuAndOneHpRebel() {
        Player zhouYu = createPlayer("player-a", 3, General.周瑜, HealthStatus.ALIVE, Role.MONARCH, new Kill(BS8008));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        Player rebel = createPlayer("player-c", 1, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR, new Peach(BH3029));
        Game game = initGame(gameId, Arrays.asList(zhouYu, playerB, rebel, playerD), zhouYu);
        Deck deck = new Deck();
        deck.add(new ArrayList<>(List.of(new Kill(BS9009), new Peach(BH4030), new Peach(BH7033), new Kill(BC0075))));
        game.setDeck(deck);
        return game;
    }

    private void fanJianOnRebelGuessHeart() throws Exception {
        mockMvcUtil.useSkillEffect(gameId, "player-a", "反間", "ACCEPT", List.of(BS8008.getCardId()), "player-c")
                .andExpect(status().isOk());
        mockMvcUtil.useSkillEffect(gameId, "player-c", "反間", "HEART", null, null).andExpect(status().isOk());
    }

    @DisplayName("反間打到 1 血反賊 → 瀕死（傷害來源存進 MongoDB）→ 沒人救 → 周瑜拿擊殺獎勵 3 張")
    @Test
    public void fanJianKillsRebel_zhouYuGetsReward() throws Exception {
        repository.save(givenZhouYuAndOneHpRebel());

        fanJianOnRebelGuessHeart();

        Game dying = repository.findById(gameId).orElseThrow();
        assertEquals(0, dying.getPlayer("player-c").getHP());
        DyingAskPeachBehavior behavior = assertInstanceOf(DyingAskPeachBehavior.class, dying.peekTopBehavior());
        assertEquals("player-a", behavior.getPendingAttackerPlayerId(), "傷害來源（周瑜）要從 MongoDB 讀回");

        for (String p : List.of("player-c", "player-d", "player-a", "player-b")) {
            mockMvcUtil.playCard(gameId, p, "player-c", "", SKIP).andExpect(status().isOk());
        }

        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getPlayer("player-c").isAlreadyDeath());
        assertEquals(3, saved.getPlayer("player-a").getHandSize(), "黑桃8 已給出，再摸擊殺獎勵 3 張");
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId(), "回到周瑜的出牌階段");
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
    }

    @DisplayName("反間打到 1 血反賊 → 內奸出桃救回 → 回 1 血，周瑜繼續出牌")
    @Test
    public void fanJianDyingRescued_turnContinues() throws Exception {
        repository.save(givenZhouYuAndOneHpRebel());

        fanJianOnRebelGuessHeart();
        mockMvcUtil.playCard(gameId, "player-c", "player-c", "", SKIP).andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-d", "player-c", BH3029.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(1, saved.getPlayer("player-c").getHP());
        assertTrue(saved.isTopBehaviorEmpty());
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId());
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
    }
}

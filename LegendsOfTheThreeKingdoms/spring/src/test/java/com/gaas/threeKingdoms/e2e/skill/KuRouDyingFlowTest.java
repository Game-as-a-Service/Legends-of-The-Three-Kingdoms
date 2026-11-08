package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.DyingAskPeachBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.gamephase.Normal;
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

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 黃蓋苦肉 HP 1 發動 → 失去最後 1 點體力進瀕死。
 * 「救回後才摸 2 張」要跨多個 HTTP 請求存活，每次請求都會經 MongoDB 重建 behavior stack，
 * 所以 pendingDrawOnRevival 必須被 BehaviorData 持久化（記憶體 callback 會遺失，見 #209）。
 */
public class KuRouDyingFlowTest extends AbstractBaseIntegrationTest {

    private static final String SKIP = PlayType.SKIP.getPlayType();

    /** a = 黃蓋（反賊、HP 1、回合主）；b = 甘寧（主公，手上一張桃）；c、d = 甘寧。 */
    private Game givenHuangGaiAtOneHp() {
        Player playerA = createPlayer("player-a", 1, General.黃蓋, HealthStatus.ALIVE, Role.REBEL);
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Peach(BH3029));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS8008), new Kill(BS9009), new Peach(BH4030), new Peach(BH7033),
                new Kill(BC0075), new Peach(BH8034)));
        game.setDeck(deck);
        return game;
    }

    @DisplayName("HP 1 苦肉 → 瀕死（救回後摸牌的資訊已存進 MongoDB）→ 主公出桃救回 → 回 1 血並摸 2 張")
    @Test
    public void kuRouAtOneHp_rescued_drawsTwoAfterRevival() throws Exception {
        repository.save(givenHuangGaiAtOneHp());

        mockMvcUtil.useSkillEffect(gameId, "player-a", "苦肉", "ACCEPT", null, null)
                .andExpect(status().isOk());

        Game dyingGame = repository.findById(gameId).orElseThrow();
        assertEquals(0, dyingGame.getPlayer("player-a").getHP());
        assertEquals(0, dyingGame.getPlayer("player-a").getHandSize(), "救回前不摸牌");
        DyingAskPeachBehavior dying = assertInstanceOf(DyingAskPeachBehavior.class, dyingGame.peekTopBehavior());
        assertEquals(2, dying.getPendingDrawOnRevival(), "從 MongoDB 讀回後仍要記得救回後摸 2 張");

        mockMvcUtil.playCard(gameId, "player-a", "player-a", "", SKIP).andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", BH3029.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        Player huangGai = saved.getPlayer("player-a");
        assertEquals(1, huangGai.getHP());
        assertEquals(2, huangGai.getHandSize(), "救回後才摸 2 張");
        assertTrue(saved.getGamePhase() instanceof Normal);
        assertTrue(saved.isTopBehaviorEmpty());
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId(), "回到黃蓋自己的出牌階段");

        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
    }

    @DisplayName("HP 1 苦肉沒人救 → 黃蓋死亡、不摸牌；沒有傷害來源 → 沒人領反賊獎勵，流程不會卡住")
    @Test
    public void kuRouAtOneHp_notRescued_diesWithoutKillerReward() throws Exception {
        repository.save(givenHuangGaiAtOneHp());

        mockMvcUtil.useSkillEffect(gameId, "player-a", "苦肉", "ACCEPT", null, null)
                .andExpect(status().isOk());
        for (String playerId : List.of("player-a", "player-b", "player-c", "player-d")) {
            mockMvcUtil.playCard(gameId, playerId, "player-a", "", SKIP).andExpect(status().isOk());
        }

        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getPlayer("player-a").isAlreadyDeath());
        assertEquals("player-b", saved.getCurrentRound().getCurrentRoundPlayer().getId(), "輪到下一位");
        assertEquals(0, saved.getPlayer("player-c").getHandSize(), "沒有擊殺者，不能有人拿到 3 張獎勵");
        assertEquals(0, saved.getPlayer("player-d").getHandSize());
    }
}

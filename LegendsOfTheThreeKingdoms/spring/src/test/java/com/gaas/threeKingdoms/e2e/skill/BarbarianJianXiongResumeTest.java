package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Issue #209：南蠻入侵 + 奸雄 ACCEPT 後 polling 卡住。
 * 根因：WaitingJianXiongResponseBehavior.onResolved 是 transient，HTTP 請求間
 * 經 MongoDB reload 遺失 → resolveChoice 無 callback，polling 不推進。
 * 本 e2e 走真實 HTTP + persistence 路徑重現並驗證修復。
 */
public class BarbarianJianXiongResumeTest extends AbstractBaseIntegrationTest {

    @Test
    public void testBarbarianInvasion_CaoCaoJianXiongAccept_PollingContinuesToNextReactor() throws Exception {
        // A 出南蠻；B=曹操（無殺，第一個 reactor）；C、D 有殺
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new BarbarianInvasion(SS7007));
        Player playerB = createPlayer("player-b", 4, General.曹操, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS8008));
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR,
                new Kill(BS9009));
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH3029)));
        game.setDeck(deck);
        repository.save(game);

        // A 出南蠻 → B 被要求出殺
        mockMvcUtil.playCard(gameId, "player-a", "player-a", SS7007.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        // B 不出殺 → 扣血 → 觸發奸雄詢問（此時 game 已 persist、onResolved 遺失）
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());

        // B ACCEPT 奸雄 → 應取得南蠻牌，且 polling 推進到 C
        mockMvcUtil.useJianXiongEffect(gameId, "player-b", "ACCEPT")
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getPlayer("player-b").getHand().getCards().stream()
                .anyMatch(c -> c.getId().equals(SS7007.getCardId())), "奸雄取得南蠻牌");
        assertEquals(3, saved.getPlayer("player-b").getHP());
        assertEquals("player-c", saved.getCurrentRound().getActivePlayer().getId(),
                "issue #209：polling 應推進到 C");

        // C 出殺回應 → D 再被問 → D 出殺 → 南蠻結束
        mockMvcUtil.playCard(gameId, "player-c", "player-a", BS8008.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-d", "player-a", BS9009.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getTopBehavior().isEmpty(), "南蠻流程應完整結束，不卡住");
        assertEquals(4, saved.getPlayer("player-c").getHP());
        assertEquals(4, saved.getPlayer("player-d").getHP());
    }
}

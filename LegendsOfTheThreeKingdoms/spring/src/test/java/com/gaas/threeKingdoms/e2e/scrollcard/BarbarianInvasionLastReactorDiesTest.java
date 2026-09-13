package com.gaas.threeKingdoms.e2e.scrollcard;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
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
 * 使用者回報：「馬超被南蠻死了後變殭屍」。
 * <p>
 * 南蠻輪詢的最後一位 reactor 被扣死後，瀕死結算的 resume hook 會對死者再發 AskKill
 * 並把他設成 activePlayer → 死人還能出牌，回合也接不下去。
 * 整條流程跨多個 HTTP request（每步都經 MongoDB 存取），故以 e2e 守住。
 */
public class BarbarianInvasionLastReactorDiesTest extends AbstractBaseIntegrationTest {

    @Test
    public void testBarbarianInvasion_lastReactorDies_noZombieAndRoundContinues() throws Exception {
        // A 出南蠻；B、C 有殺；D = 內奸僅 1 滴血且無殺 → 最後一位 reactor 被扣死
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new BarbarianInvasion(SS7007));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER,
                new Kill(BS8008));
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS9009));
        Player playerD = createPlayer("player-d", 1, General.馬超, HealthStatus.ALIVE, Role.TRAITOR,
                new Dodge(BH2028));
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH3029)));
        game.setDeck(deck);
        repository.save(game);

        // A 出南蠻 → B 被問殺 → B、C 出殺 → 輪到 D
        mockMvcUtil.playCard(gameId, "player-a", "player-a", SS7007.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", BS8008.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-c", "player-a", BS9009.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        // D 不出殺 → 扣血至 0 → 瀕死
        mockMvcUtil.playCard(gameId, "player-d", "player-a", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        Game dying = repository.findById(gameId).orElseThrow();
        assertEquals(0, dying.getPlayer("player-d").getHP());
        assertEquals("player-d", dying.getCurrentRound().getActivePlayer().getId(), "瀕死者自己先被問桃");

        // 全員放棄出桃（順序：瀕死者 → A → B → C）→ D 死亡
        mockMvcUtil.playCard(gameId, "player-d", "player-d", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-a", "player-d", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-d", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-c", "player-d", "", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(HealthStatus.DEATH, saved.getPlayer("player-d").getHealthStatus(), "D 應已死亡");
        assertTrue(saved.getTopBehavior().isEmpty(), "南蠻輪詢已結束，behavior stack 應清空");
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId(),
                "activePlayer 必須回到回合玩家 A，不能留在死者身上（殭屍成因）");

        // 死者不得再出牌
        mockMvcUtil.playCard(gameId, "player-d", "player-d", BH2028.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isBadRequest());

        // A 的回合可正常繼續
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
    }
}

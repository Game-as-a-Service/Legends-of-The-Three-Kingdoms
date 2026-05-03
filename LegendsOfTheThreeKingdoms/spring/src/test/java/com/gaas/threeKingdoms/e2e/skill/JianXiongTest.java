package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JianXiongTest extends AbstractBaseIntegrationTest {

    // @Override protected boolean shouldRegenerateFixtures() { return true; }

    private void givenPlayerBIsCaoCao() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        Player playerB = createPlayer("player-b", 4, General.曹操, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH3029)));
        game.setDeck(deck);
        repository.save(game);
    }

    @Test
    public void testCaoCaoTakesKillDamage_AskJianXiongEffectEmitted_AndAccept() throws Exception {
        // Given B 為曹操
        givenPlayerBIsCaoCao();

        // When A 出殺打 B → B 不出閃 → 受到傷害
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // When B 選 ACCEPT 發動奸雄
        mockMvcUtil.useJianXiongEffect(gameId, "player-b", "ACCEPT")
                .andExpect(status().isOk()).andReturn();

        // Then 驗證 4 個玩家收到的 JSON
        assertAllPlayerJson("src/test/resources/TestJsonFile/SkillTest/JianXiong/jianxiong_accept_for_%s.json");
    }

    @Test
    public void testCaoCaoTakesKillDamage_AskJianXiongEffectEmitted_AndSkip() throws Exception {
        // Given B 為曹操
        givenPlayerBIsCaoCao();

        // When A 出殺打 B → B 不出閃 → 受到傷害
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // When B 選 SKIP 不發動
        mockMvcUtil.useJianXiongEffect(gameId, "player-b", "SKIP")
                .andExpect(status().isOk()).andReturn();

        // Then 驗證 4 個玩家收到的 JSON
        assertAllPlayerJson("src/test/resources/TestJsonFile/SkillTest/JianXiong/jianxiong_skip_for_%s.json");
    }
}

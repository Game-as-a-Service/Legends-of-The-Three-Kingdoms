package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.EighteenSpanViperSpearCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
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
    public void testCaoCaoLosesDuel_AskJianXiongEffectEmitted_AndAccept() throws Exception {
        // Given B 為曹操，無殺；A 有決鬥
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Duel(SSA001));
        Player playerB = createPlayer("player-b", 4, General.曹操, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH3029)));
        game.setDeck(deck);
        repository.save(game);

        // When A 對 B 決鬥（B 沒殺 → 立刻扣血 → 觸發奸雄）
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SSA001", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // When B 選 ACCEPT 發動奸雄
        mockMvcUtil.useJianXiongEffect(gameId, "player-b", "ACCEPT")
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/SkillTest/JianXiong/jianxiong_duel_accept_for_%s.json");
    }

    @Test
    public void testCaoCaoTakesViperSpearDamage_JianXiongTakesTwoDiscards() throws Exception {
        // Given B 為曹操；A 裝丈八蛇矛 + 兩張手牌
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Peach(BH3029), new Peach(BH4030));
        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        Player playerB = createPlayer("player-b", 4, General.曹操, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.劉備, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH6032)));
        game.setDeck(deck);
        repository.save(game);

        // When A 用丈八蛇矛攻擊 B（棄兩張桃當虛擬殺）
        mockMvcUtil.useViperSpearKill(gameId, "player-a", "player-b",
                        List.of(BH3029.getCardId(), BH4030.getCardId()))
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 不出閃
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B 選 ACCEPT 發動奸雄 → 應收兩張棄牌
        mockMvcUtil.useJianXiongEffect(gameId, "player-b", "ACCEPT")
                .andExpect(status().isOk()).andReturn();

        assertAllPlayerJson("src/test/resources/TestJsonFile/SkillTest/JianXiong/jianxiong_viper_spear_accept_for_%s.json");
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

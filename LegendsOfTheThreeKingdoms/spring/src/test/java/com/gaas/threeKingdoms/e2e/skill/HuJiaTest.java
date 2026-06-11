package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
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

public class HuJiaTest extends AbstractBaseIntegrationTest {

    // 若需要重產本 class 的 fixture，把下列 @Override 取消註解並改為 return true，
    // 產完後務必改回 false 或刪除 override 再 commit。
    // @Override protected boolean shouldRegenerateFixtures() { return true; }

    /**
     * 4 人場，B = 曹操 主公，D = 夏侯惇（魏忠臣，代閃者）
     * A 出殺打 B → HuJia 觸發 → D ACCEPT → 代閃成功
     */
    @Test
    public void testCaoCaoMonarchAskedDodge_WeiHelperAcceptsAndSubstitutes() throws Exception {
        // Given
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS8008));
        Player playerB = createPlayer("player-b", 4, General.曹操, HealthStatus.ALIVE, Role.MONARCH);
        Player playerC = createPlayer("player-c", 4, General.趙雲, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.夏侯惇, HealthStatus.ALIVE, Role.MINISTER,
                new Dodge(BH2028));
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH3029)));
        game.setDeck(deck);
        repository.save(game);

        // When A 出殺打 B → 觸發護駕，活躍轉到 D（唯一其他 Wei）
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // D ACCEPT 出閃代替曹操
        mockMvcUtil.useHuJiaEffect(gameId, "player-d", "ACCEPT", BH2028.getCardId())
                .andExpect(status().isOk()).andReturn();

        // Then 驗證所有玩家收到的 JSON：曹操不扣血、D 失去閃
        assertAllPlayerJson("src/test/resources/TestJsonFile/SkillTest/HuJia/hujia_accept_for_%s.json");
    }

    /**
     * 4 人場，B = 曹操 主公，D = 夏侯惇（魏忠臣）
     * A 出殺打 B → HuJia → D DECLINE → fallback emit AskDodge(B) → B SKIP → 扣血
     */
    @Test
    public void testCaoCaoMonarchAskedDodge_AllWeiDeclined_FallbackToCaoCaoDodge() throws Exception {
        // Given
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS8008));
        Player playerB = createPlayer("player-b", 4, General.曹操, HealthStatus.ALIVE, Role.MONARCH);
        Player playerC = createPlayer("player-c", 4, General.趙雲, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.夏侯惇, HealthStatus.ALIVE, Role.MINISTER);
        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        Game game = initGame(gameId, players, playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH3029)));
        game.setDeck(deck);
        repository.save(game);

        // When A 出殺打 B → 護駕 ask D
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // D DECLINE → 應 fallback emit AskDodge(B)
        mockMvcUtil.useHuJiaEffect(gameId, "player-d", "DECLINE", null)
                .andExpect(status().isOk()).andReturn();
        websocketUtil.popAllPlayerMessage();

        // B SKIP → 扣血
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk()).andReturn();

        // Then 曹操 HP=3
        assertAllPlayerJson("src/test/resources/TestJsonFile/SkillTest/HuJia/hujia_decline_then_skip_for_%s.json");
    }
}

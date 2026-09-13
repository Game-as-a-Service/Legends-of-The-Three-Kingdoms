package com.gaas.threeKingdoms.e2e.scrollcard.ward;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.BountifulHarvest;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
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
 * 使用者回報：通過五穀豐登取得的無懈可擊沒辦法立即使用。
 * 根因：Phase 2 逐人詢問沿用「出牌者不能無懈自己的牌」排除 — 出牌者從牌池
 * 拿到無懈後永遠不被詢問（WardBehavior WARD_INCLUDE_TRIGGER_PLAYER 修復）。
 * websocket 訊息層驗證：撿到無懈者必須收到 AskPlayWardEvent（可出）而非 WaitForWardEvent。
 */
public class HarvestPickedWardTest extends AbstractBaseIntegrationTest {

    @Test
    public void pickedWardHolderReceivesAskPlayWardEvent() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new BountifulHarvest(SH3042));
        Player playerB = createPlayer("player-b", 4, General.關羽, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.張飛, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS8008), new Ward(SSJ011), new Peach(BH3029), new Peach(BH0036)));
        game.setDeck(deck);
        repository.save(game);

        // A 出五穀豐登（無人初始持無懈 → 直接輪詢）、A 先選
        mockMvcUtil.playCard(gameId, "player-a", "", "SH3042", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAll();
        mockMvcUtil.chooseCardFromBountifulHarvest(gameId, "player-a", BH0036.getCardId())
                .andExpect(status().isOk());
        popAll();

        // B 從牌池拿走無懈 → C 選牌前詢問無懈
        mockMvcUtil.chooseCardFromBountifulHarvest(gameId, "player-b", SSJ011.getCardId())
                .andExpect(status().isOk());

        String messageB = websocketUtil.getValue("player-b");
        String messageA = websocketUtil.getValue("player-a");
        String messageC = websocketUtil.getValue("player-c");
        String messageD = websocketUtil.getValue("player-d");

        assertTrue(messageB.contains("AskPlayWardEvent"),
                "剛拿到無懈的 B 應收到 AskPlayWardEvent（實際訊息：" + snippet(messageB) + "）");
        assertFalse(messageA.contains("AskPlayWardEvent"), "A 未持無懈，收等待事件");
        assertTrue(messageA.contains("WaitForWardEvent"));
        assertFalse(messageC.contains("AskPlayWardEvent"));
        assertFalse(messageD.contains("AskPlayWardEvent"));
    }

    @Test
    public void casterPickedWardReceivesAskPlayWardEvent() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new BountifulHarvest(SH3042));
        Player playerB = createPlayer("player-b", 4, General.關羽, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.張飛, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS8008), new Peach(BH3029), new Peach(BH0036), new Ward(SSJ011)));
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "", "SH3042", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        popAll();

        // 出牌者 A 自己從牌池拿走無懈 → B 選牌前 A 應被詢問（使用者回報情境）
        mockMvcUtil.chooseCardFromBountifulHarvest(gameId, "player-a", SSJ011.getCardId())
                .andExpect(status().isOk());

        String messageA = websocketUtil.getValue("player-a");
        String messageB = websocketUtil.getValue("player-b");
        assertTrue(messageA.contains("AskPlayWardEvent"),
                "出牌者拿到無懈也應收到 AskPlayWardEvent（實際訊息：" + snippet(messageA) + "）");
        assertFalse(messageB.contains("AskPlayWardEvent"));
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");

        // A 立即無懈 B 的效果 → B 沒拿牌、輪到 C
        mockMvcUtil.playWardCard(gameId, "player-a", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        Game after = repository.findById(gameId).orElseThrow();
        assertEquals(0, after.getPlayer("player-b").getHandSize(), "B 的效果被無懈 → 沒拿牌");
    }

    private void popAll() {
        websocketUtil.getValue("player-a");
        websocketUtil.getValue("player-b");
        websocketUtil.getValue("player-c");
        websocketUtil.getValue("player-d");
    }

    private String snippet(String s) {
        return s == null ? "null" : s.substring(0, Math.min(400, s.length()));
    }
}

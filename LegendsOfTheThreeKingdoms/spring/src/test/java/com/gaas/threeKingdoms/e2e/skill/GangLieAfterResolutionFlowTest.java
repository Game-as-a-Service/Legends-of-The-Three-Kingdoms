package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
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
 * 使用者回報的卡局情境（決鬥 → 剛烈 → 來源選擇）**結算之後**，後續操作要照常。
 * <p>
 * 這裡看的是跨 request（每一步都經 MongoDB 存回再讀出，#209 同型坑）：剛烈收束後
 * behavior stack 要真的清空 —— 沒清空的話下一步 {@code finishAction} 會 500
 * （{@code current topBehavior is not null}），玩家就卡在自己的回合裡出不去。
 */
public class GangLieAfterResolutionFlowTest extends AbstractBaseIntegrationTest {

    /** a（劉備，決鬥來源）、b（夏侯惇）、c/d 孫權；牌堆頂為黑桃 3 → 剛烈判定生效。 */
    private Game givenDuelOnXiaHouDun() {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Duel(SSA001), new Kill(BS8008), new Peach(BH3029), new Peach(BH4030));
        // b 手上有殺 → 決鬥先問 b 出殺（照使用者的實戰路徑）；無殺時決鬥會直接結算扣血
        Player playerB = createPlayer("player-b", 4, General.夏侯惇, HealthStatus.ALIVE, Role.MINISTER,
                new Kill(BS9009));
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dismantle(SS3003))); // 剛烈判定牌：黑桃 3 → 生效
        game.setDeck(deck);
        return game;
    }

    /** a 出決鬥 → b 不出殺受傷 → b 發動剛烈（判定生效）→ 停在「問來源 a」。 */
    private void duelUntilSourceAsked() throws Exception {
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SSA001", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk());
        mockMvcUtil.useSkillEffect(gameId, "player-b", "剛烈", "ACCEPT", null, null)
                .andExpect(status().isOk());

        Game askSource = repository.findById(gameId).orElseThrow();
        assertEquals("player-a", askSource.getCurrentRound().getActivePlayer().getId(),
                "判定生效 → 換傷害來源回答");
        assertEquals(3, askSource.getPlayer("player-b").getHP());
    }

    /** a 剛烈收束後繼續出殺打 d、d 不出閃，然後結束回合。 */
    private void playKillOnDThenFinishRound() throws Exception {
        mockMvcUtil.playCard(gameId, "player-a", "player-d", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-d", "player-a", "", "skip")
                .andExpect(status().isOk());

        Game afterKill = repository.findById(gameId).orElseThrow();
        assertEquals(3, afterKill.getPlayer("player-d").getHP(), "剛烈之後出的殺照常結算");
        assertEquals("player-a", afterKill.getCurrentRound().getActivePlayer().getId());

        // 這一步就是使用者卡住的地方：stack 沒清乾淨的話會 500
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());

        Game nextRound = repository.findById(gameId).orElseThrow();
        assertEquals("player-b", nextRound.getCurrentRoundPlayer().getId(), "回合換到下一位");
        assertTrue(nextRound.getTopBehavior().isEmpty());
    }

    @DisplayName("來源選受傷後：還能出殺、能結束回合換下一位（跨 request）")
    @Test
    public void sourceTakesDamage_thenRoundContinuesAcrossRequests() throws Exception {
        repository.save(givenDuelOnXiaHouDun());
        duelUntilSourceAsked();

        mockMvcUtil.useSkillEffect(gameId, "player-a", "剛烈", "DAMAGE", null, null)
                .andExpect(status().isOk());

        Game resolved = repository.findById(gameId).orElseThrow();
        assertEquals(3, resolved.getPlayer("player-a").getHP(), "來源受剛烈 1 點傷害");
        assertTrue(resolved.getTopBehavior().isEmpty(), "剛烈結束，stack 要清空");
        assertEquals("player-a", resolved.getCurrentRound().getActivePlayer().getId(),
                "activePlayer 回到回合玩家");

        playKillOnDThenFinishRound();
    }

    @DisplayName("來源棄兩張手牌後：還能出殺、能結束回合換下一位（跨 request）")
    @Test
    public void sourceDiscardsTwoCards_thenRoundContinuesAcrossRequests() throws Exception {
        repository.save(givenDuelOnXiaHouDun());
        duelUntilSourceAsked();

        mockMvcUtil.useSkillEffect(gameId, "player-a", "剛烈", "DISCARD",
                        List.of("BH3029", "BH4030"), null)
                .andExpect(status().isOk());

        Game resolved = repository.findById(gameId).orElseThrow();
        assertEquals(4, resolved.getPlayer("player-a").getHP(), "棄牌則不受傷");
        assertEquals(1, resolved.getPlayer("player-a").getHandSize(), "只剩下那張殺");
        assertTrue(resolved.getGraveyard().contains(BH3029.getCardId()));
        assertTrue(resolved.getTopBehavior().isEmpty(), "剛烈結束，stack 要清空");

        playKillOnDThenFinishRound();
    }

    @DisplayName("剛烈詢問中送錯的請求（出牌 / 結束回合）只會被擋掉，答完剛烈後回合照常打完")
    @Test
    public void wrongRequestsWhileGangLieIsPending_doNotBreakTheFlow() throws Exception {
        repository.save(givenDuelOnXiaHouDun());
        duelUntilSourceAsked();

        // 前端在等來源回答時誤送出牌／結束回合：要被擋掉且不能動到狀態
        // （目前回 4xx，錯誤訊息還不夠明確 — 訊息品質另案）
        mockMvcUtil.playCard(gameId, "player-a", "player-d", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().is4xxClientError());
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().is4xxClientError());

        Game stillPending = repository.findById(gameId).orElseThrow();
        assertEquals("player-a", stillPending.getCurrentRound().getActivePlayer().getId(),
                "誤送的請求不應改變輪到誰");
        assertEquals(3, stillPending.getPlayer("player-a").getHandSize(), "殺還在手上，沒有被打出去");
        assertFalse(stillPending.getTopBehavior().isEmpty(), "剛烈詢問仍在等回答");

        // 正確回答後，回合照常走完
        mockMvcUtil.useSkillEffect(gameId, "player-a", "剛烈", "DAMAGE", null, null)
                .andExpect(status().isOk());
        playKillOnDThenFinishRound();
    }

    @DisplayName("夏侯惇放棄剛烈後：回合照常打完（跨 request）")
    @Test
    public void xiaHouDunSkipsGangLie_thenRoundContinuesAcrossRequests() throws Exception {
        repository.save(givenDuelOnXiaHouDun());
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "SSA001", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk());

        mockMvcUtil.useSkillEffect(gameId, "player-b", "剛烈", "SKIP", null, null)
                .andExpect(status().isOk());

        Game resolved = repository.findById(gameId).orElseThrow();
        assertEquals(4, resolved.getPlayer("player-a").getHP(), "放棄剛烈 → 來源不受影響");
        assertTrue(resolved.getTopBehavior().isEmpty());
        assertEquals("player-a", resolved.getCurrentRound().getActivePlayer().getId());

        playKillOnDThenFinishRound();
    }
}

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
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 通用武將技 endpoint `player:useSkillEffect` e2e — 以反饋（司馬懿）為代表路徑。
 * 各技能的完整行為已在 domain Batch2TriggeredSkillsTest 覆蓋；此處驗證 HTTP 層
 * + persistence round-trip（WaitingSkillEffectBehavior 經 MongoDB 存取後仍可 resolve）。
 */
public class SkillEffectTest extends AbstractBaseIntegrationTest {

    @Test
    public void testFanKuiViaGenericSkillEffectEndpoint() throws Exception {
        // Given：A 出殺打 B（司馬懿），A 手上還有一張桃可被反饋拿走
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029));
        Player playerB = createPlayer("player-b", 4, General.司馬懿, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH4030)));
        game.setDeck(deck);
        repository.save(game);

        // When：A 殺 B、B 不出閃受傷 → 觸發反饋詢問（過程經 MongoDB persistence）
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk());

        // B ACCEPT 反饋，cardIds[0] = 來源手牌 index（0-based，同順手牽羊 targetCardIndex）
        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-b",
                                  "skillName": "反饋",
                                  "choice": "ACCEPT",
                                  "cardIds": ["0"]
                                }"""))
                .andExpect(status().isOk());

        // Then：B 拿走 A 的桃
        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getPlayer("player-b").getHand().getCards().stream()
                .anyMatch(c -> c.getId().equals(BH3029.getCardId())), "司馬懿應取得攻擊者手牌");
        assertEquals(0, saved.getPlayer("player-a").getHandSize());
        assertEquals(3, saved.getPlayer("player-b").getHP());
        assertTrue(saved.getTopBehavior().isEmpty());
    }

    @Test
    public void testGuiCaiReplaceTieQiJudgementAcrossRequests() throws Exception {
        // Given：A（馬超）殺 B（司馬懿）；鐵騎判定牌疊黑桃（原本生效 B 不能閃）
        Player playerA = createPlayer("player-a", 4, General.馬超, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        Player playerB = createPlayer("player-b", 4, General.司馬懿, HealthStatus.ALIVE, Role.MINISTER,
                new Peach(BH3029), new com.gaas.threeKingdoms.handcard.basiccard.Dodge(BH2028));
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS9009))); // 鐵騎判定牌：黑桃 → 原本生效
        game.setDeck(deck);
        repository.save(game);

        // When：A 殺 B → 鐵騎判定抽牌後暫停，詢問鬼才（WaitingSkillEffectBehavior 經 MongoDB 存取）
        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        Game paused = repository.findById(gameId).orElseThrow();
        assertFalse(paused.getTopBehavior().isEmpty(), "鬼才詢問中，判定暫停");
        assertEquals(4, paused.getPlayer("player-b").getHP(), "尚未結算");

        // B 發動鬼才：以紅心桃替換判定牌 → 鐵騎不生效 → 照常問閃
        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-b",
                                  "skillName": "鬼才",
                                  "choice": "ACCEPT",
                                  "cardIds": ["BH3029"]
                                }"""))
                .andExpect(status().isOk());

        // B 出閃擋下
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BH2028", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        // Then：鐵騎被鬼才化解，B 無傷
        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(4, saved.getPlayer("player-b").getHP(), "替換成紅心 → 鐵騎不生效，閃擋下殺");
        assertEquals(0, saved.getPlayer("player-b").getHandSize(), "替換牌與閃都已打出");
        assertTrue(saved.getGraveyard().contains(BH3029.getCardId()), "替換牌進墓地");
        assertTrue(saved.getTopBehavior().isEmpty());
    }

    @Test
    public void testFanKuiInBarbarianInvasionPollingAcrossRequests() throws Exception {
        // Given：A 出南蠻，B（司馬懿）無殺 skip 受傷 → 反饋詢問（polling defer 經 MongoDB 存取）
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion(SS7007), new Peach(BH3029));
        Player playerB = createPlayer("player-b", 4, General.司馬懿, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        game.setDeck(new Deck());
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-a", "SS7007", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk());

        // 反饋詢問中：輪詢暫停（activePlayer = B），底層南蠻 behavior 保留
        Game paused = repository.findById(gameId).orElseThrow();
        assertEquals("player-b", paused.getCurrentRound().getActivePlayer().getId());
        assertEquals(2, paused.getTopBehavior().size(), "南蠻 behavior + 反饋 waiting");

        // B ACCEPT 反饋（拿 A 手牌）→ resume 輪詢問 C
        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-b",
                                  "skillName": "反饋",
                                  "choice": "ACCEPT"
                                }"""))
                .andExpect(status().isOk());

        Game resumed = repository.findById(gameId).orElseThrow();
        assertTrue(resumed.getPlayer("player-b").getHand().getCards().stream()
                .anyMatch(c -> c.getId().equals(BH3029.getCardId())), "司馬懿拿走攻擊者手牌");
        assertEquals("player-c", resumed.getCurrentRound().getActivePlayer().getId(),
                "反饋解決後 resume 輪詢問 C");

        // C、D skip → 輪詢正常結束
        mockMvcUtil.playCard(gameId, "player-c", "player-a", "", "skip")
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-d", "player-a", "", "skip")
                .andExpect(status().isOk());

        Game finalState = repository.findById(gameId).orElseThrow();
        assertTrue(finalState.getTopBehavior().isEmpty());
        assertEquals(3, finalState.getPlayer("player-b").getHP());
        assertEquals(3, finalState.getPlayer("player-c").getHP());
        assertEquals(3, finalState.getPlayer("player-d").getHP());
    }
    @Test
    public void testLuoShenAskThenAcceptAcrossRequests() throws Exception {
        // Given：A 回合結束後輪到 B（甄姬）；判定牌疊 BS9009(黑收) → BH3029(紅停)，摸 2 = BH2028 + BH4030
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH);
        Player playerB = createPlayer("player-b", 4, General.甄姬, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH4030), new com.gaas.threeKingdoms.handcard.basiccard.Dodge(BH2028),
                new Peach(BH3029), new Kill(BS9009)));
        game.setDeck(deck);
        repository.save(game);

        // When：A 結束回合 → B 回合開始，先詢問洛神並暫停（WaitingSkillEffectBehavior 經 MongoDB 存取）
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());

        Game paused = repository.findById(gameId).orElseThrow();
        assertFalse(paused.getTopBehavior().isEmpty(), "洛神詢問中，回合開始流程暫停");
        assertEquals(0, paused.getPlayer("player-b").getHandSize(), "尚未判定，未收牌未摸牌");
        assertEquals("player-b", paused.getCurrentRound().getActivePlayer().getId());

        // B ACCEPT 洛神 → 黑色 BS9009 收入手牌、紅色 BH3029 停 → 接著摸 2
        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-b",
                                  "skillName": "洛神",
                                  "choice": "ACCEPT"
                                }"""))
                .andExpect(status().isOk());

        // Then：收 1 張黑判定牌 + 摸 2，紅判定牌進墓地，流程推進到 B 的出牌階段
        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getPlayer("player-b").getHand().getCards().stream()
                .anyMatch(c -> c.getId().equals(BS9009.getCardId())), "黑色判定牌收入手牌");
        assertEquals(3, saved.getPlayer("player-b").getHandSize(), "收 1 + 摸 2");
        assertTrue(saved.getGraveyard().contains(BH3029.getCardId()), "紅色判定牌進墓地");
        assertTrue(saved.getTopBehavior().isEmpty());
        assertEquals("player-b", saved.getCurrentRound().getActivePlayer().getId());
    }

    @Test
    public void testLuoShenSkipAcrossRequests() throws Exception {
        // Given：同上，但 B 放棄洛神
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH);
        Player playerB = createPlayer("player-b", 4, General.甄姬, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH4030), new com.gaas.threeKingdoms.handcard.basiccard.Dodge(BH2028)));
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());

        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-b",
                                  "skillName": "洛神",
                                  "choice": "SKIP"
                                }"""))
                .andExpect(status().isOk());

        // Then：不判定，直接摸 2 進出牌階段
        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(2, saved.getPlayer("player-b").getHandSize(), "SKIP 不判定，直接摸 2");
        assertTrue(saved.getTopBehavior().isEmpty());
        assertEquals("player-b", saved.getCurrentRound().getActivePlayer().getId());
    }

    @Test
    public void testGangLieInBarbarianInvasionPollingAcrossRequests() throws Exception {
        // Given：A 出南蠻，B（夏侯惇）無殺 skip 受傷 → 剛烈兩段式詢問鏈
        // （ASK_XIAHOU → 判定 → ASK_SOURCE，defer 標記與 waiting 經 3 次 MongoDB 存取）
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion(SS7007),
                new Peach(BH3029), new Kill(BS9009));
        Player playerB = createPlayer("player-b", 4, General.夏侯惇, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS8008))); // 剛烈判定牌：黑桃 → 生效
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-a", "SS7007", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk());

        // 剛烈詢問中：輪詢暫停（activePlayer = B），底層南蠻 behavior 保留
        Game paused = repository.findById(gameId).orElseThrow();
        assertEquals("player-b", paused.getCurrentRound().getActivePlayer().getId());
        assertEquals(2, paused.getTopBehavior().size(), "南蠻 behavior + 剛烈 waiting");
        assertEquals(3, paused.getPlayer("player-b").getHP());

        // B ACCEPT 剛烈 → 判定黑桃生效 → 問來源 A（第二段 waiting，輪詢仍暫停）
        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-b",
                                  "skillName": "剛烈",
                                  "choice": "ACCEPT"
                                }"""))
                .andExpect(status().isOk());

        Game askSource = repository.findById(gameId).orElseThrow();
        assertEquals("player-a", askSource.getCurrentRound().getActivePlayer().getId(),
                "判定生效後問來源選擇");

        // A 選 DISCARD 棄兩張手牌 → 剛烈鏈收斂 → resume 輪詢問 C
        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-a",
                                  "skillName": "剛烈",
                                  "choice": "DISCARD",
                                  "cardIds": ["BH3029", "BS9009"]
                                }"""))
                .andExpect(status().isOk());

        Game resumed = repository.findById(gameId).orElseThrow();
        assertEquals(0, resumed.getPlayer("player-a").getHandSize(), "來源棄兩張手牌");
        assertTrue(resumed.getGraveyard().contains(BH3029.getCardId()));
        assertEquals("player-c", resumed.getCurrentRound().getActivePlayer().getId(),
                "剛烈鏈收斂後 resume 輪詢問 C");

        // C、D skip → 輪詢正常結束
        mockMvcUtil.playCard(gameId, "player-c", "player-a", "", "skip")
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-d", "player-a", "", "skip")
                .andExpect(status().isOk());

        Game finalState = repository.findById(gameId).orElseThrow();
        assertTrue(finalState.getTopBehavior().isEmpty());
        assertEquals(3, finalState.getPlayer("player-c").getHP());
        assertEquals(3, finalState.getPlayer("player-d").getHP());
        assertEquals("player-a", finalState.getCurrentRound().getActivePlayer().getId(),
                "輪詢結束 activePlayer 回到出牌者");
    }
}

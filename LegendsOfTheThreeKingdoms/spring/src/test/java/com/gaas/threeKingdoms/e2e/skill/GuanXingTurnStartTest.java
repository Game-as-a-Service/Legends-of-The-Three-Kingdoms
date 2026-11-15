package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.round.RoundPhase;
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
 * 觀星（諸葛亮）回合開始詢問 e2e：A 結束回合 → B（諸葛亮）回合開始先詢問觀星並暫停，
 * WaitingSkillEffectBehavior（含觀看牌 id param）經 MongoDB 存取後再以 ARRANGE / SKIP 接續判定 → 摸牌。
 */
public class GuanXingTurnStartTest extends AbstractBaseIntegrationTest {

    /** 堆頂抽序 = BS8008, BS9009, BH3029, BH4030。 */
    private Game givenZhugeLiangNext(boolean withContentment) {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH);
        Player playerB = createPlayer("player-b", 3, General.諸葛亮, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        if (withContentment) {
            playerB.addDelayScrollCard(new Contentment(SH6045));
        }
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BH2028), new Peach(BH4030), new Peach(BH3029), new Kill(BS9009), new Kill(BS8008)));
        game.setDeck(deck);
        repository.save(game);
        return game;
    }

    private void useGuanXing(String choice, String cardIdsJson) throws Exception {
        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-b",
                                  "skillName": "觀星",
                                  "choice": "%s",
                                  "cardIds": %s
                                }""".formatted(choice, cardIdsJson)))
                .andExpect(status().isOk());
    }

    private boolean handHas(Game game, String playerId, String cardId) {
        return game.getPlayer(playerId).getHand().getCards().stream().anyMatch(c -> c.getId().equals(cardId));
    }

    @Test
    public void testGuanXingAskedAtTurnStartThenArrangeAcrossRequests() throws Exception {
        givenZhugeLiangNext(false);

        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());

        Game paused = repository.findById(gameId).orElseThrow();
        assertEquals("player-b", paused.getCurrentRoundPlayer().getId());
        assertEquals(RoundPhase.Judgement, paused.getCurrentRoundPhase(), "觀星詢問中，尚未進入判定 / 摸牌");
        assertTrue(paused.peekTopBehavior() instanceof WaitingSkillEffectBehavior w && "觀星".equals(w.getSkillName()));
        assertEquals(List.of(BS8008.getCardId(), BS9009.getCardId(), BH3029.getCardId(), BH4030.getCardId()),
                paused.peekTopBehavior().getParam("GUANXING_PEEKED_CARD_IDS"), "觀看牌 id 經 MongoDB 還原");
        assertEquals(0, paused.getPlayer("player-b").getHandSize());
        assertEquals("player-b", paused.getCurrentRound().getActivePlayer().getId());

        // BH4030, BH3029 回堆頂；BS8008, BS9009 置底
        useGuanXing("ARRANGE", "[\"BH4030\", \"BH3029\"]");

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(2, saved.getPlayer("player-b").getHandSize());
        assertTrue(handHas(saved, "player-b", BH4030.getCardId()) && handHas(saved, "player-b", BH3029.getCardId()),
                "摸到重排後的堆頂");
        assertEquals(RoundPhase.Action, saved.getCurrentRoundPhase());
        assertTrue(saved.getTopBehavior().isEmpty());
        List<String> bottom = saved.getDeck().getCardDeck().subList(0, 2).stream().map(HandCard::getId).toList();
        assertTrue(bottom.containsAll(List.of(BS8008.getCardId(), BS9009.getCardId())), "未選的牌置牌堆底");
    }

    @Test
    public void testGuanXingSkipAcrossRequests() throws Exception {
        givenZhugeLiangNext(false);

        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
        useGuanXing("SKIP", "[]");

        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(handHas(saved, "player-b", BS8008.getCardId()) && handHas(saved, "player-b", BS9009.getCardId()),
                "SKIP 牌堆不變，照原順序摸 2");
        assertEquals(RoundPhase.Action, saved.getCurrentRoundPhase());
        assertTrue(saved.getTopBehavior().isEmpty());
    }

    @Test
    public void testGuanXingBeforeContentmentJudgementAcrossRequests() throws Exception {
        // 原判定牌 BS8008（黑桃 → 樂不思蜀生效）；觀星把 BH3029 排到最上 → 判定紅桃不生效
        givenZhugeLiangNext(true);

        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
        Game paused = repository.findById(gameId).orElseThrow();
        assertTrue(paused.getPlayer("player-b").hasThisDelayScrollCard(SH6045.getCardId()), "觀星先於判定");

        useGuanXing("ARRANGE", "[\"BH3029\", \"BS8008\", \"BS9009\"]");

        Game saved = repository.findById(gameId).orElseThrow();
        assertFalse(saved.getPlayer("player-b").hasThisDelayScrollCard(SH6045.getCardId()));
        assertTrue(saved.getGraveyard().contains(BH3029.getCardId()));
        assertTrue(handHas(saved, "player-b", BS8008.getCardId()) && handHas(saved, "player-b", BS9009.getCardId()));
        assertEquals("player-b", saved.getCurrentRoundPlayer().getId());
        assertEquals(RoundPhase.Action, saved.getCurrentRoundPhase(), "樂不思蜀未生效 → 出牌階段");
    }
}

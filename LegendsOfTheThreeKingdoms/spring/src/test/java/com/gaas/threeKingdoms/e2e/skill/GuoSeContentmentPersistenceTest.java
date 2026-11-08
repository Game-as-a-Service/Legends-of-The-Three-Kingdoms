package com.gaas.threeKingdoms.e2e.skill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.ScrollCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
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
 * 使用者回報：大喬發動國色後出現
 * 「Kill cannot be cast to class ScrollCard」。
 * <p>
 * 國色把方塊殺（BD7085）當樂不思蜀放進判定區。判定區在 MongoDB 只存卡牌 id，
 * 讀回時用 id 還原出原本的「殺」再強轉 ScrollCard → ClassCastException，
 * 之後這局每個請求都讀不出遊戲（線上 my-id 因此卡死）。
 * domain 測試全程在記憶體，抓不到 —— 只有經 MongoDB round-trip 的 e2e 才會壞。
 */
public class GuoSeContentmentPersistenceTest extends AbstractBaseIntegrationTest {

    /** a = 大喬（回合主，手上一張方塊7 殺）；b = 國色目標；c 視情境持有無懈可擊；牌堆頂為黑桃（樂不思蜀生效）。 */
    private Game givenDaQiaoHoldingDiamondKill(boolean cHasWard) {
        Player playerA = createPlayer("player-a", 4, General.大喬, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BD7085));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerC = cHasWard
                ? createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER, new Ward(SSJ011))
                : createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        // Deck 是 Stack：list 最後一張最先被抽 → BS8008（黑桃8）是 player-b 的判定牌
        deck.add(List.of(new Peach(BH4030), new Peach(BH7033), new Peach(BH8034), new Kill(BS9009),
                new Kill(BS8008)));
        game.setDeck(deck);
        return game;
    }

    private void daQiaoUsesGuoSeOnPlayerB() throws Exception {
        mockMvcUtil.useSkillEffect(gameId, "player-a", "國色", "CONTENTMENT", List.of(BD7085.getCardId()), "player-b")
                .andExpect(status().isOk());
    }

    @DisplayName("國色後從 MongoDB 讀回遊戲 → 判定區的方塊殺還原成樂不思蜀，不會 ClassCastException")
    @Test
    public void guoSeContentmentSurvivesReload() throws Exception {
        repository.save(givenDaQiaoHoldingDiamondKill(false));

        daQiaoUsesGuoSeOnPlayerB();

        Game saved = assertDoesNotThrow(() -> repository.findById(gameId).orElseThrow());
        List<ScrollCard> delay = saved.getPlayer("player-b").getDelayScrollCards();
        assertEquals(1, delay.size());
        HandCard card = delay.get(0);
        assertInstanceOf(Contentment.class, card, "國色轉化的牌要以樂不思蜀身份還原");
        assertEquals(BD7085.getCardId(), card.getId(), "仍是原本那張方塊7 殺");
    }

    @DisplayName("國色後換目標回合 → 沒人有無懈時直接判定，樂不思蜀生效：跳過出牌階段並離開判定區")
    @Test
    public void guoSeContentmentJudgedOnTargetTurn() throws Exception {
        repository.save(givenDaQiaoHoldingDiamondKill(false));

        daQiaoUsesGuoSeOnPlayerB();
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getPlayer("player-b").getDelayScrollCards().isEmpty(), "判定完要離開判定區");
        // 判定黑桃8 → 樂不思蜀生效：player-b 跳過出牌階段（摸牌階段摸的 2 張沒出、未超過上限不用棄），直接輪到 player-c
        assertEquals("player-c", saved.getCurrentRound().getCurrentRoundPlayer().getId(), "player-b 的出牌階段被跳過");
        assertEquals(2, saved.getPlayer("player-b").getHandSize(), "只有摸牌階段的 2 張，沒有出牌");
    }

    @DisplayName("國色後換目標回合 → 有人有無懈時先問無懈（跨請求），放棄後照常判定並跳過出牌階段")
    @Test
    public void guoSeContentmentJudgedAfterWardSkipped() throws Exception {
        repository.save(givenDaQiaoHoldingDiamondKill(true));

        daQiaoUsesGuoSeOnPlayerB();
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
        mockMvcUtil.playWardCard(gameId, "player-c", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getPlayer("player-b").getDelayScrollCards().isEmpty(), "判定完要離開判定區");
        // 判定黑桃8 → 樂不思蜀生效：player-b 跳過出牌階段（摸牌階段摸的 2 張沒出、未超過上限不用棄），直接輪到 player-c
        assertEquals("player-c", saved.getCurrentRound().getCurrentRoundPlayer().getId(), "player-b 的出牌階段被跳過");
        assertEquals(2, saved.getPlayer("player-b").getHandSize(), "只有摸牌階段的 2 張，沒有出牌");
        assertTrue(saved.isTopBehaviorEmpty(), "無懈與判定流程都要收斂");
    }

    @DisplayName("使用者回報：國色後推播的 delayScrolls 要寫出效果，前端才知道方塊7 殺是樂不思蜀")
    @Test
    public void guoSePushShowsDelayScrollEffect() throws Exception {
        repository.save(givenDaQiaoHoldingDiamondKill(false));
        websocketUtil.popAllPlayerMessage();

        daQiaoUsesGuoSeOnPlayerB();

        ObjectMapper objectMapper = new ObjectMapper();
        for (String viewer : List.of("player-a", "player-b", "player-c", "player-d")) {
            JsonNode push = objectMapper.readTree(websocketUtil.getValue(viewer));
            JsonNode targetSeat = null;
            for (JsonNode seat : push.get("data").get("seats")) {
                if ("player-b".equals(seat.get("id").asText())) {
                    targetSeat = seat;
                }
            }
            assertNotNull(targetSeat, viewer + " 應看得到 player-b 的座位");
            JsonNode delayScrolls = targetSeat.get("delayScrolls");
            assertEquals(1, delayScrolls.size());
            JsonNode delay = delayScrolls.get(0);
            assertEquals(BD7085.getCardId(), delay.get("cardId").asText(), "cardId 是實際的方塊7 殺");
            assertEquals("CONTENTMENT", delay.get("effect").asText(), viewer + " 要知道它在判定區的效果是樂不思蜀");
            assertEquals("樂不思蜀", delay.get("effectName").asText());
        }
    }
}

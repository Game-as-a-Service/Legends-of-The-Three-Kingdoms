package com.gaas.threeKingdoms.e2e.skill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 使用者回報：國色發動後，輪到目標回合時有時候只有「判定結束」，沒有樂不思蜀的判定。
 * 重現線上陣容：大喬 → 許褚（裸衣詢問）→ 趙雲（棄牌結束回合）→ 曹操（國色目標）。
 */
public class GuoSeContentmentLineupTest extends AbstractBaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Game givenLineup() {
        Player daQiao = createPlayer("player-a", 3, General.大喬, HealthStatus.ALIVE, Role.TRAITOR,
                new Kill(BD7085));
        Player xuChu = createPlayer("player-b", 4, General.許褚, HealthStatus.ALIVE, Role.REBEL);
        Player zhaoYun = createPlayer("player-c", 4, General.趙雲, HealthStatus.ALIVE, Role.MINISTER,
                new Peach(BH3029), new Peach(BH4030), new Peach(BH7033), new Peach(BH8034), new Dodge(BH2028));
        Player caoCao = createPlayer("player-d", 4, General.曹操, HealthStatus.ALIVE, Role.MONARCH);
        Game game = initGame(gameId, Arrays.asList(daQiao, xuChu, zhaoYun, caoCao), daQiao);
        Deck deck = new Deck();
        // Deck 是 Stack：list 最後一張最先被抽。抽牌順序：許褚 2 → 趙雲 2 → 曹操判定 1 → 曹操摸 2 → …
        deck.add(new ArrayList<>(List.of(
                new Kill(BS9009), new Kill(BC0075), new Kill(BS7020), new Kill(BS8008),  // 後面的回合
                new Kill(BS9009),                                                        // 曹操判定：黑桃9（非紅心 → 生效）
                new Dodge(BHK039), new Kill(BC0075),                                     // 趙雲摸牌
                new Kill(BS7020), new Kill(BS8008)                                       // 許褚摸牌
        )));
        game.setDeck(deck);
        return game;
    }

    @DisplayName("大喬國色曹操 → 許褚裸衣 → 趙雲棄牌結束回合 → 曹操回合要判定樂不思蜀")
    @Test
    public void guoSeOnCaoCao_judgedAfterXuChuAndZhaoYunDiscard() throws Exception {
        repository.save(givenLineup());

        mockMvcUtil.useSkillEffect(gameId, "player-a", "國色", "CONTENTMENT", List.of(BD7085.getCardId()), "player-d")
                .andExpect(status().isOk());
        assertEquals(1, repository.findById(gameId).orElseThrow().getPlayer("player-d").getDelayScrollCards().size());

        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
        mockMvcUtil.useSkillEffect(gameId, "player-b", "裸衣", "SKIP", null, null).andExpect(status().isOk());
        mockMvcUtil.finishAction(gameId, "player-b").andExpect(status().isOk());
        assertEquals(1, repository.findById(gameId).orElseThrow().getPlayer("player-d").getDelayScrollCards().size(),
                "輪到曹操前樂不思蜀都要還在");

        mockMvcUtil.finishAction(gameId, "player-c").andExpect(status().isOk());
        Game beforeDiscard = repository.findById(gameId).orElseThrow();
        List<String> zhaoYunCards = beforeDiscard.getPlayer("player-c").getHand().getCards().stream()
                .map(c -> c.getId()).toList();
        int toDiscard = zhaoYunCards.size() - beforeDiscard.getPlayer("player-c").getHP();
        assertTrue(toDiscard > 0, "趙雲要棄牌才能結束回合");
        assertEquals(1, beforeDiscard.getPlayer("player-d").getDelayScrollCards().size(),
                "輪到曹操前樂不思蜀都要還在");

        websocketUtil.popAllPlayerMessage();
        String discardJson = "[" + String.join(",", zhaoYunCards.subList(0, toDiscard).stream()
                .map(id -> "\"" + id + "\"").toList()) + "]";
        mockMvc.perform(post("/api/games/" + gameId + "/player:discardCards")
                .contentType(MediaType.APPLICATION_JSON).content(discardJson)).andExpect(status().isOk());

        // WebSocket 訊息非同步到達：找出「棄牌」這次請求的推播（含 DiscardEvent）
        List<String> eventNames = new ArrayList<>();
        JsonNode contentment = null;
        for (int attempt = 0; attempt < 10 && eventNames.stream().noneMatch("DiscardEvent"::equals); attempt++) {
            String raw = websocketUtil.getValue("player-d");
            if (raw == null) continue;
            JsonNode push = objectMapper.readTree(raw);
            eventNames.clear();
            contentment = null;
            for (JsonNode e : push.get("events")) {
                eventNames.add(e.get("event").asText());
                if ("ContentmentEvent".equals(e.get("event").asText())) {
                    contentment = e;
                }
            }
        }
        assertTrue(eventNames.contains("DiscardEvent"), "應讀到棄牌那次的推播，實際：" + eventNames);
        assertNotNull(contentment, "曹操回合要有樂不思蜀判定，實際事件：" + eventNames);

        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getPlayer("player-d").getDelayScrollCards().isEmpty());
    }

    private Game givenLineupWithTwoDiamonds() {
        return givenLineupWithTwoDiamonds(false, false);
    }

    private Game givenLineupWithTwoDiamonds(boolean zhaoYunContentmentSucceeds, boolean xuChuHasWard) {
        Player daQiao = createPlayer("player-a", 3, General.大喬, HealthStatus.ALIVE, Role.TRAITOR,
                new Kill(BD7085), new Dodge(BD2080));
        Player xuChu = xuChuHasWard
                ? createPlayer("player-b", 4, General.許褚, HealthStatus.ALIVE, Role.REBEL,
                        new com.gaas.threeKingdoms.handcard.scrollcard.Ward(SSJ011))
                : createPlayer("player-b", 4, General.許褚, HealthStatus.ALIVE, Role.REBEL);
        Player zhaoYun = createPlayer("player-c", 4, General.趙雲, HealthStatus.ALIVE, Role.MINISTER,
                new Peach(BH3029), new Peach(BH4030), new Peach(BH7033), new Peach(BH8034), new Dodge(BH2028));
        Player caoCao = createPlayer("player-d", 4, General.曹操, HealthStatus.ALIVE, Role.MONARCH);
        Game game = initGame(gameId, Arrays.asList(daQiao, xuChu, zhaoYun, caoCao), daQiao);
        Deck deck = new Deck();
        // 抽牌順序：許褚 2 → 趙雲判定 1（紅心 → 樂不思蜀失效，趙雲照常出牌）→ 趙雲 2 → 曹操判定 1（黑桃 → 生效）→ 曹操 2 → …
        deck.add(new ArrayList<>(List.of(
                new Kill(BS9009), new Kill(BC0075), new Kill(BS7020), new Kill(BS8008),
                new Kill(BS9009), new Kill(BC0075),                 // 曹操摸牌
                new Kill(BS8008),                                    // 曹操判定：黑桃8
                new Dodge(BHK039), new Kill(BC0075),                 // 趙雲摸牌
                zhaoYunContentmentSucceeds ? new Kill(BS9009) : new Peach(BH3029), // 趙雲判定
                new Kill(BS7020), new Kill(BS8008)                   // 許褚摸牌
        )));
        game.setDeck(deck);
        return game;
    }

    @DisplayName("使用者回報：大喬同回合國色趙雲與曹操 → 趙雲判定後，曹操回合也要判定樂不思蜀")
    @Test
    public void guoSeOnZhaoYunAndCaoCao_bothJudged() throws Exception {
        repository.save(givenLineupWithTwoDiamonds());

        mockMvcUtil.useSkillEffect(gameId, "player-a", "國色", "CONTENTMENT", List.of(BD2080.getCardId()), "player-c")
                .andExpect(status().isOk());
        mockMvcUtil.useSkillEffect(gameId, "player-a", "國色", "CONTENTMENT", List.of(BD7085.getCardId()), "player-d")
                .andExpect(status().isOk());
        Game afterGuoSe = repository.findById(gameId).orElseThrow();
        assertEquals(1, afterGuoSe.getPlayer("player-c").getDelayScrollCards().size());
        assertEquals(1, afterGuoSe.getPlayer("player-d").getDelayScrollCards().size());

        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
        mockMvcUtil.useSkillEffect(gameId, "player-b", "裸衣", "SKIP", null, null).andExpect(status().isOk());
        mockMvcUtil.finishAction(gameId, "player-b").andExpect(status().isOk());

        Game zhaoYunTurn = repository.findById(gameId).orElseThrow();
        assertEquals("player-c", zhaoYunTurn.getCurrentRound().getCurrentRoundPlayer().getId());
        assertTrue(zhaoYunTurn.getPlayer("player-c").getDelayScrollCards().isEmpty(), "趙雲的樂不思蜀已判定");
        assertEquals(1, zhaoYunTurn.getPlayer("player-d").getDelayScrollCards().size(),
                "趙雲判定完，曹操的樂不思蜀要還在");

        mockMvcUtil.finishAction(gameId, "player-c").andExpect(status().isOk());
        Game beforeDiscard = repository.findById(gameId).orElseThrow();
        assertEquals(1, beforeDiscard.getPlayer("player-d").getDelayScrollCards().size(),
                "趙雲結束出牌後，曹操的樂不思蜀要還在");
        List<String> zhaoYunCards = beforeDiscard.getPlayer("player-c").getHand().getCards().stream()
                .map(c -> c.getId()).toList();
        int toDiscard = zhaoYunCards.size() - beforeDiscard.getPlayer("player-c").getHP();
        String discardJson = "[" + String.join(",", zhaoYunCards.subList(0, toDiscard).stream()
                .map(id -> "\"" + id + "\"").toList()) + "]";
        mockMvc.perform(post("/api/games/" + gameId + "/player:discardCards")
                .contentType(MediaType.APPLICATION_JSON).content(discardJson)).andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getPlayer("player-d").getDelayScrollCards().isEmpty(), "曹操的樂不思蜀要被判定掉");
        assertNotEquals("player-d", saved.getCurrentRound().getActivePlayer().getId(),
                "判定黑桃8 生效 → 曹操跳過出牌階段");
    }


    /** 讀 player-d 收到、含指定事件的那則推播，列出事件名稱（ContentmentEvent 附上判定對象）。 */
    private String readEventsOfPushContaining(String eventName) throws Exception {
        for (int attempt = 0; attempt < 10; attempt++) {
            String raw = websocketUtil.getValue("player-d");
            if (raw == null) continue;
            JsonNode push = objectMapper.readTree(raw);
            List<String> names = new ArrayList<>();
            for (JsonNode e : push.get("events")) {
                String n = e.get("event").asText();
                if ("ContentmentEvent".equals(n)) {
                    n += "(" + e.get("data").get("playerId").asText() + ")";
                }
                names.add(n);
            }
            if (eventName == null || names.contains(eventName)) return String.join(",", names);
        }
        return "<not found>";
    }

    /**
     * 自動推進遊戲直到曹操（player-d）判定完；每步記錄曹操判定區，抓出樂不思蜀在哪一步、誰的回合消失。
     */
    private void driveUntilCaoCaoJudged(boolean zhaoYunSucceeds, boolean xuChuHasWard) throws Exception {
        repository.save(givenLineupWithTwoDiamonds(zhaoYunSucceeds, xuChuHasWard));
        mockMvcUtil.useSkillEffect(gameId, "player-a", "國色", "CONTENTMENT", List.of(BD2080.getCardId()), "player-c")
                .andExpect(status().isOk());
        mockMvcUtil.useSkillEffect(gameId, "player-a", "國色", "CONTENTMENT", List.of(BD7085.getCardId()), "player-d")
                .andExpect(status().isOk());

        List<String> trace = new ArrayList<>();
        boolean caoCaoTurnSeen = false;
        for (int step = 0; step < 20; step++) {
            Game g = repository.findById(gameId).orElseThrow();
            String roundPlayer = g.getCurrentRound().getCurrentRoundPlayer().getId();
            String active = g.getCurrentRound().getActivePlayer().getId();
            String phase = String.valueOf(g.getCurrentRound().getRoundPhase());
            String top = g.isTopBehaviorEmpty() ? "-" : g.peekTopBehavior().getClass().getSimpleName();
            int caoCaoDelay = g.getPlayer("player-d").getDelayScrollCards().size();
            trace.add(String.format("step%d round=%s active=%s phase=%s top=%s caoCaoDelay=%d",
                    step, roundPlayer, active, phase, top, caoCaoDelay));
            if (roundPlayer.equals("player-d")) caoCaoTurnSeen = true;
            if (caoCaoDelay == 0 && !top.contains("Ward")) {
                String all = String.join("\n", trace);
                assertTrue(all.contains("ContentmentEvent(player-d)"), "曹操的樂不思蜀消失了，但沒有曹操的判定事件：\n" + all);
                return;
            }
            if (top.contains("Ward")) {
                // 有人有無懈 → 持有者放棄
                websocketUtil.popAllPlayerMessage();
                mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());
                trace.add("  ward-skip push events: " + readEventsOfPushContaining(null));
            } else if (top.equals("WaitingSkillEffectBehavior")) {
                mockMvcUtil.useSkillEffect(gameId, active, "裸衣", "SKIP", null, null).andExpect(status().isOk());
            } else if (phase.equals("Discard")) {
                Player p = g.getPlayer(roundPlayer);
                List<String> cards = p.getHand().getCards().stream().map(c -> c.getId()).toList();
                int n = Math.max(0, cards.size() - p.getHP());
                String json = "[" + String.join(",", cards.subList(0, n).stream().map(id -> "\"" + id + "\"").toList()) + "]";
                websocketUtil.popAllPlayerMessage();
                mockMvc.perform(post("/api/games/" + gameId + "/player:discardCards")
                        .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isOk());
                trace.add("  discard push events: " + readEventsOfPushContaining("DiscardEvent"));
            } else {
                websocketUtil.popAllPlayerMessage();
                mockMvcUtil.finishAction(gameId, roundPlayer).andExpect(status().isOk());
                trace.add("  finishAction push events: " + readEventsOfPushContaining("FinishActionEvent"));
            }
        }
        fail("20 步內沒有判定到曹操的樂不思蜀：\n" + String.join("\n", trace));
    }

    @DisplayName("兩次國色：趙雲判定生效（跳過出牌）")
    @Test
    public void twoGuoSe_zhaoYunSucceeds() throws Exception {
        driveUntilCaoCaoJudged(true, false);
    }

    @DisplayName("兩次國色：許褚有無懈（判定前先問無懈）")
    @Test
    public void twoGuoSe_withWardHolder() throws Exception {
        driveUntilCaoCaoJudged(false, true);
    }

    @DisplayName("兩次國色：趙雲判定生效 + 許褚有無懈")
    @Test
    public void twoGuoSe_zhaoYunSucceedsWithWardHolder() throws Exception {
        driveUntilCaoCaoJudged(true, true);
    }
}

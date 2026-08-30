package com.gaas.threeKingdoms.e2e.skill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
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
 * issue #229（前端回報）：甄姬被殺、被問閃時打出黑色手牌（傾國）後遊戲卡住。
 * 根因：前端以一般 playCard 打出黑牌（非 useSkillEffect），問閃 behavior 的非閃 else
 * 分支默默回空 events / null → 200 但無推播、狀態不變。
 * 修法：Game.playerPlayCard 攔截可轉化牌自動視為發動傾國/龍膽。
 * 本測試驗證 websocket 推播：出黑牌那個 request 後每位玩家都收到結算推播、遊戲可繼續。
 */
public class QingGuoPushTest extends AbstractBaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("A 殺 B（甄姬），B 以 playCard 打出黑牌 → 自動傾國、全員收到推播、activePlayer 回 A")
    @Test
    public void qingGuoViaPlayCardEndpointPushesToAllPlayers() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        Player playerB = createPlayer("player-b", 4, General.甄姬, HealthStatus.ALIVE, Role.MINISTER,
                new Kill(BS9009)); // 黑桃手牌
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        game.setDeck(new Deck());
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        websocketUtil.popAllPlayerMessage();

        // 前端行為：直接把黑牌當一般出牌打出（修復前：200 但無推播 → 卡住）
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BS9009", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        for (String playerId : List.of("player-a", "player-b", "player-c", "player-d")) {
            JsonNode push = objectMapper.readTree(websocketUtil.getValue(playerId));

            boolean hasQingGuoEffect = false;
            for (JsonNode e : push.get("events")) {
                if ("SkillEffectEvent".equals(e.get("event").asText())
                        && "傾國".equals(e.get("data").get("skillName").asText())) {
                    hasQingGuoEffect = true;
                    assertTrue(e.get("data").get("accepted").asBoolean());
                }
            }
            assertTrue(hasQingGuoEffect, playerId + " 應收到 SkillEffectEvent(傾國)");
            assertEquals("player-a", push.get("data").get("round").get("activePlayer").asText(),
                    "結算後 activePlayer 回攻擊者");
            assertEquals(4, push.get("data").get("seats").get(1).get("hp").asInt(), "B 未扣血");
        }

        // 遊戲可繼續：A 結束回合 → 200（修復前這裡永遠等不到）
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals("player-b", saved.getCurrentRound().getCurrentRoundPlayer().getId(), "回合正常輪替");
        assertTrue(saved.getGraveyard().contains(BS9009.getCardId()), "黑牌進墓地");
    }

    @DisplayName("被問閃時打出非閃且不可轉化的牌 → 400 明確錯誤（不再默默吞掉）")
    @Test
    public void nonConvertibleCardAsDodgeReturns400() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        Player playerB = createPlayer("player-b", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER,
                new Kill(BS9009));
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        game.setDeck(new Deck());
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        mockMvcUtil.playCard(gameId, "player-b", "player-a", "BS9009", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isBadRequest());

        // 遊戲狀態未變，B 仍可正常 skip 繼續
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk());
        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(3, saved.getPlayer("player-b").getHP(), "skip 後正常扣血");
    }
}

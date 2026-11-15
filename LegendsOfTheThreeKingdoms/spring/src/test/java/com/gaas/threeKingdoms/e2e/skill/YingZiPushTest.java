package com.gaas.threeKingdoms.e2e.skill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 使用者回報：周瑜英姿是鎖定技，摸牌階段多摸一張，但推播裡沒有任何事件說明英姿發動。
 */
public class YingZiPushTest extends AbstractBaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("上家結束回合 → 輪到周瑜：推播含 SkillEffectEvent(英姿)，且排在摸牌事件之前")
    @Test
    public void zhouYuTurnStartPushesYingZiEvent() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH);
        Player zhouYu = createPlayer("player-b", 3, General.周瑜, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(playerA, zhouYu, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(new ArrayList<>(List.of(new Kill(BS8008), new Kill(BS9009), new Peach(BH3029), new Peach(BH4030))));
        game.setDeck(deck);
        repository.save(game);
        websocketUtil.popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());

        for (String viewer : List.of("player-a", "player-b", "player-c", "player-d")) {
            List<String> names = new ArrayList<>();
            JsonNode yingZi = null;
            for (int attempt = 0; attempt < 10 && !names.contains("DrawCardEvent"); attempt++) {
                String raw = websocketUtil.getValue(viewer);
                if (raw == null) continue;
                names.clear();
                yingZi = null;
                for (JsonNode e : objectMapper.readTree(raw).get("events")) {
                    names.add(e.get("event").asText());
                    if ("SkillEffectEvent".equals(e.get("event").asText())
                            && "英姿".equals(e.get("data").get("skillName").asText())) {
                        yingZi = e;
                    }
                }
            }
            assertNotNull(yingZi, viewer + " 應收到英姿的 SkillEffectEvent，實際：" + names);
            assertEquals("player-b", yingZi.get("data").get("playerId").asText());
            assertEquals("英姿：周瑜 摸牌階段多摸 1 張牌（共 3 張）", yingZi.get("message").asText());
            assertTrue(names.indexOf("SkillEffectEvent") < names.indexOf("DrawCardEvent"),
                    "英姿事件要排在摸牌之前：" + names);
        }
        assertEquals(3, repository.findById(gameId).orElseThrow().getPlayer("player-b").getHandSize());
    }
}

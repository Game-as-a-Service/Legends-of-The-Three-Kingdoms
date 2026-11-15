package com.gaas.threeKingdoms.e2e.skill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
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
 * 使用者回報：呂蒙克己（本回合沒有使用或打出過殺 → 跳過棄牌階段）沒有事件發到前端。
 */
public class KeJiPushTest extends AbstractBaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("呂蒙未出殺、手牌 6 張 4 血 → 結束出牌的推播含 SkillEffectEvent(克己)，手牌全留")
    @Test
    public void luMengFinishActionPushesKeJi() throws Exception {
        Player luMeng = createPlayer("player-a", 4, General.呂蒙, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Kill(BS9009), new Dodge(BH2028), new Dodge(BHK039), new Peach(BH3029), new Peach(BH4030));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(luMeng, playerB, playerC, playerD), luMeng);
        Deck deck = new Deck();
        deck.add(new ArrayList<>(List.of(new Kill(BS7020), new Peach(BH7033), new Peach(BH8034), new Kill(BC0075))));
        game.setDeck(deck);
        repository.save(game);
        websocketUtil.popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());

        for (String viewer : List.of("player-a", "player-b", "player-c", "player-d")) {
            List<String> names = new ArrayList<>();
            JsonNode keJi = null;
            for (int attempt = 0; attempt < 10 && !names.contains("FinishActionEvent"); attempt++) {
                String raw = websocketUtil.getValue(viewer);
                if (raw == null) continue;
                names.clear();
                keJi = null;
                for (JsonNode e : objectMapper.readTree(raw).get("events")) {
                    names.add(e.get("event").asText());
                    if ("SkillEffectEvent".equals(e.get("event").asText())
                            && "克己".equals(e.get("data").get("skillName").asText())) {
                        keJi = e;
                    }
                }
            }
            assertNotNull(keJi, viewer + " 應收到克己的 SkillEffectEvent，實際：" + names);
            assertEquals("player-a", keJi.get("data").get("playerId").asText());
            assertEquals("克己：呂蒙 本回合沒有使用或打出過【殺】，跳過棄牌階段（不用棄 2 張）", keJi.get("message").asText());
        }
        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(6, saved.getPlayer("player-a").getHandSize(), "克己：手牌全部保留");
        assertEquals("player-b", saved.getCurrentRound().getCurrentRoundPlayer().getId(), "直接進下一回合");
    }
}

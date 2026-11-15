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
import com.gaas.threeKingdoms.round.RoundPhase;
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
 * 突襲（摸牌階段詢問）走真實 HTTP + MongoDB：詢問推給張遼，ACCEPT 後拿牌不摸牌。
 */
public class TuXiDrawPhaseTest extends AbstractBaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Game givenZhangLiaoNext() {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH);
        Player zhangLiao = createPlayer("player-b", 4, General.張遼, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL, new Kill(BS8008));
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR, new Dodge(BH2028));
        Game game = initGame(gameId, Arrays.asList(playerA, zhangLiao, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(new ArrayList<>(List.of(new Peach(BH4030), new Peach(BH7033), new Kill(BS9009), new Kill(BC0075))));
        game.setDeck(deck);
        return game;
    }

    @DisplayName("上家結束回合 → 張遼收到突襲詢問（ACCEPT/SKIP）→ ACCEPT 拿兩張、不摸牌、進出牌階段")
    @Test
    public void tuXiAskedAtDrawPhaseThenAccept() throws Exception {
        repository.save(givenZhangLiaoNext());
        websocketUtil.popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());

        JsonNode ask = null;
        for (int attempt = 0; attempt < 10 && ask == null; attempt++) {
            String raw = websocketUtil.getValue("player-b");
            if (raw == null) continue;
            for (JsonNode e : objectMapper.readTree(raw).get("events")) {
                if ("AskSkillEffectEvent".equals(e.get("event").asText())
                        && "突襲".equals(e.get("data").get("skillName").asText())) {
                    ask = e;
                }
            }
        }
        assertNotNull(ask, "張遼應收到突襲詢問");
        assertEquals("player-b", ask.get("data").get("playerId").asText());
        assertEquals("[\"ACCEPT\",\"SKIP\"]", ask.get("data").get("options").toString());
        Game asking = repository.findById(gameId).orElseThrow();
        assertEquals(0, asking.getPlayer("player-b").getHandSize(), "詢問時還沒摸牌");

        mockMvcUtil.useSkillEffect(gameId, "player-b", "突襲", "ACCEPT", List.of("player-c", "player-d"), null)
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(2, saved.getPlayer("player-b").getHandSize());
        assertEquals(0, saved.getPlayer("player-c").getHandSize());
        assertEquals(0, saved.getPlayer("player-d").getHandSize());
        assertEquals(4, saved.getDeck().size(), "沒從牌堆摸牌");
        assertEquals(RoundPhase.Action, saved.getCurrentRound().getRoundPhase());
        assertEquals("player-b", saved.getCurrentRound().getActivePlayer().getId());
        mockMvcUtil.finishAction(gameId, "player-b").andExpect(status().isOk());
    }

    @DisplayName("張遼選 SKIP → 照常摸 2 張")
    @Test
    public void tuXiSkipDrawsNormally() throws Exception {
        repository.save(givenZhangLiaoNext());

        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
        mockMvcUtil.useSkillEffect(gameId, "player-b", "突襲", "SKIP", null, null).andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(2, saved.getPlayer("player-b").getHandSize());
        assertEquals(1, saved.getPlayer("player-c").getHandSize());
    }
}

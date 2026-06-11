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

        // B ACCEPT 反饋
        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-b",
                                  "skillName": "反饋",
                                  "choice": "ACCEPT"
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
}

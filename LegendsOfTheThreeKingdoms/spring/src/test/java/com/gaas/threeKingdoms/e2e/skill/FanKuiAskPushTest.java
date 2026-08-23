package com.gaas.threeKingdoms.e2e.skill;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
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
 * 使用者回報：A 用殺打 B（司馬懿），B 不出閃扣血，前端沒收到發動武將技的 event。
 * 本測試直接驗證 websocket 推播內容：B 不出閃那個 request 後，每位玩家都應收到
 * AskSkillEffectEvent(skillName=反饋, playerId=B)，且 round.activePlayer = B。
 */
public class FanKuiAskPushTest extends AbstractBaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("A 殺 B（司馬懿 3HP），B skip 扣血 → 推播含 AskSkillEffectEvent(反饋)，activePlayer=B")
    @Test
    public void skipAfterKill_pushesFanKuiAskToAllPlayers() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Peach(BH4030));
        playerA.getEquipment().setArmor(new EightDiagramTactic(EC2067)); // 有一件裝備可被反饋指定
        Player playerB = createPlayer("player-b", 3, General.司馬懿, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        game.setDeck(new Deck());
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        websocketUtil.popAllPlayerMessage();

        // B 不出閃
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk());

        for (String playerId : List.of("player-a", "player-b", "player-c", "player-d")) {
            JsonNode push = objectMapper.readTree(websocketUtil.getValue(playerId));

            JsonNode ask = null;
            for (JsonNode e : push.get("events")) {
                if ("AskSkillEffectEvent".equals(e.get("event").asText())) {
                    ask = e;
                }
            }
            assertNotNull(ask, playerId + " 應收到 AskSkillEffectEvent");
            assertEquals("反饋", ask.get("data").get("skillName").asText());
            assertEquals("player-b", ask.get("data").get("playerId").asText(), "被詢問者 = 司馬懿");
            assertEquals("player-a", ask.get("data").get("dataPlayerId").asText(), "傷害來源 = A");
            assertEquals(List.of(EC2067.getCardId()),
                    objectMapper.convertValue(ask.get("data").get("dataCardIds"), List.class),
                    "dataCardIds 只列來源實際存在的裝備 id（無 \"\" 佔位）");

            assertEquals("player-b", push.get("data").get("round").get("activePlayer").asText(),
                    "等待 B 回應反饋");
            assertEquals(2, push.get("data").get("seats").get(1).get("hp").asInt(), "B 已扣血 3→2");
        }

        // B ACCEPT 指定拿 A 的防具 → 結算、activePlayer 回到 A
        mockMvcUtil.useSkillEffect(gameId, "player-b", "反饋", "ACCEPT", List.of(EC2067.getCardId()), null)
                .andExpect(status().isOk());
        JsonNode after = objectMapper.readTree(websocketUtil.getValue("player-b"));
        assertEquals("player-a", after.get("data").get("round").get("activePlayer").asText());
        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getPlayer("player-b").getHand().getCards().stream()
                .anyMatch(c -> c.getId().equals(EC2067.getCardId())));
        assertFalse(saved.getPlayer("player-a").getEquipment().hasAnyEquipment());
    }
}

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
 * 觀星看到的牌堆頂只有諸葛亮能看：AskSkillEffectEvent(觀星).dataCardIds 與
 * ARRANGE 結果 SkillEffectEvent(觀星).dataCardIds 只推給諸葛亮本人，其他玩家收到空 list。
 */
public class GuanXingPeekPrivacyPushTest extends AbstractBaseIntegrationTest {

    private static final List<String> OTHERS = List.of("player-a", "player-c", "player-d");
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** B = 諸葛亮，A 結束回合後輪到 B；堆頂抽序 = BS8008, BS9009, BH3029, BH4030。 */
    private void givenZhugeLiangNext() {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH);
        Player playerB = createPlayer("player-b", 3, General.諸葛亮, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Dodge(BH2028), new Peach(BH4030), new Peach(BH3029), new Kill(BS9009), new Kill(BS8008)));
        game.setDeck(deck);
        repository.save(game);
    }

    @DisplayName("觀星詢問：只有諸葛亮收到觀看的牌 id，其他玩家 dataCardIds 為空")
    @Test
    public void askGuanXingPeekedCardIdsOnlyPushedToZhugeLiang() throws Exception {
        givenZhugeLiangNext();
        websocketUtil.popAllPlayerMessage();

        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());

        JsonNode ownAsk = innerEvent(pushOf("player-b", "AskSkillEffectEvent"), "AskSkillEffectEvent");
        assertEquals("觀星", ownAsk.get("data").get("skillName").asText());
        assertEquals(List.of(BS8008.getCardId(), BS9009.getCardId(), BH3029.getCardId(), BH4030.getCardId()),
                objectMapper.convertValue(ownAsk.get("data").get("dataCardIds"), List.class),
                "諸葛亮本人 payload 不變：看得到觀看的 4 張");
        assertEquals(List.of("ARRANGE", "SKIP"), objectMapper.convertValue(ownAsk.get("data").get("options"), List.class));

        for (String playerId : OTHERS) {
            JsonNode push = pushOf(playerId, "AskSkillEffectEvent");
            JsonNode ask = innerEvent(push, "AskSkillEffectEvent");
            assertEquals("觀星", ask.get("data").get("skillName").asText(), playerId + " 仍知道諸葛亮在觀星");
            assertEquals("player-b", ask.get("data").get("playerId").asText());
            assertEquals(0, ask.get("data").get("dataCardIds").size(), playerId + " 不應看到觀星的牌");
            assertTrue(ask.get("message").asText().contains("4 張"), playerId + " 可得知張數");
            assertNoPeekedIdLeaks(push, playerId);
        }
    }

    @DisplayName("觀星 ARRANGE 結果：只有諸葛亮收到重排順序，其他玩家 dataCardIds 為空")
    @Test
    public void arrangeResultOnlyPushedToZhugeLiang() throws Exception {
        givenZhugeLiangNext();
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
        websocketUtil.popAllPlayerMessage();

        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-b", "skillName": "觀星", "choice": "ARRANGE",
                                  "cardIds": ["BH4030", "BH3029", "BS9009"] }"""))
                .andExpect(status().isOk());

        JsonNode own = innerEvent(pushOf("player-b", "SkillEffectEvent"), "SkillEffectEvent");
        assertEquals("觀星", own.get("data").get("skillName").asText());
        assertEquals(List.of(BH4030.getCardId(), BH3029.getCardId(), BS9009.getCardId()),
                objectMapper.convertValue(own.get("data").get("dataCardIds"), List.class),
                "諸葛亮本人 payload 不變：看得到重排後放回堆頂的順序");

        for (String playerId : OTHERS) {
            JsonNode push = pushOf(playerId, "SkillEffectEvent");
            JsonNode effect = innerEvent(push, "SkillEffectEvent");
            assertEquals("觀星", effect.get("data").get("skillName").asText());
            assertTrue(effect.get("data").get("accepted").asBoolean());
            assertEquals(0, effect.get("data").get("dataCardIds").size(), playerId + " 不應看到觀星重排順序");
            assertNoPeekedIdLeaks(push, playerId);
        }
    }

    /** 整則推播（events[] 內任何 event）都不得出現觀看的牌 id（含之後摸牌的 DrawCardEvent）。 */
    private void assertNoPeekedIdLeaks(JsonNode push, String playerId) {
        String eventsJson = push.get("events").toString();
        for (String id : List.of(BS8008.getCardId(), BS9009.getCardId(), BH3029.getCardId(), BH4030.getCardId())) {
            assertFalse(eventsJson.contains(id), playerId + " 的 events 洩漏了觀星的牌 " + id + "：" + eventsJson);
        }
    }

    /** 逐一 poll 該玩家 queue，直到某則推播的 events[] 含指定 event。 */
    private JsonNode pushOf(String playerId, String eventName) throws Exception {
        for (int attempt = 0; attempt < 10; attempt++) {
            String message = websocketUtil.getValue(playerId);
            if (message == null) {
                break;
            }
            JsonNode node = objectMapper.readTree(message);
            if (node.has("events") && innerEventOrNull(node, eventName) != null) {
                return node;
            }
        }
        fail(playerId + " 未收到 " + eventName);
        return null;
    }

    private JsonNode innerEvent(JsonNode push, String eventName) {
        JsonNode found = innerEventOrNull(push, eventName);
        assertNotNull(found);
        return found;
    }

    private JsonNode innerEventOrNull(JsonNode push, String eventName) {
        for (JsonNode inner : push.get("events")) {
            if (inner.has("event") && eventName.equals(inner.get("event").asText())) {
                return inner;
            }
        }
        return null;
    }
}

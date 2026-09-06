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
 * issue #235（前端回報）：SkillEffectEvent 的 message 固定為「武將技發動結果」，
 * 具體結算文字（如「剛烈判定紅桃，未生效」）只在外層 push message，前端顯示不到。
 * 驗證 websocket 推播中事件層 message 帶 domain event 的具體結算訊息。
 */
public class GangLieMessagePushTest extends AbstractBaseIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("剛烈判定紅桃 → 推播的 SkillEffectEvent.message 為「剛烈判定紅桃，未生效」")
    @Test
    public void gangLieHeartJudgement_pushCarriesSpecificMessage() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        Player playerB = createPlayer("player-b", 4, General.夏侯惇, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.孫權, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH3029))); // 剛烈判定牌：紅心 → 不生效
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        websocketUtil.popAllPlayerMessage(); // popAllPlayerMessage 每人只 pop 一則，逐 request 清
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk());
        websocketUtil.popAllPlayerMessage();

        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerId": "player-b",
                                  "skillName": "剛烈",
                                  "choice": "ACCEPT"
                                }"""))
                .andExpect(status().isOk());

        for (String playerId : List.of("player-a", "player-b", "player-c", "player-d")) {
            JsonNode push = objectMapper.readTree(websocketUtil.getValue(playerId));
            String skillEffectMessage = null;
            for (JsonNode e : push.get("events")) {
                if ("SkillEffectEvent".equals(e.get("event").asText())
                        && "剛烈".equals(e.get("data").get("skillName").asText())) {
                    skillEffectMessage = e.get("message").asText();
                    assertFalse(e.get("data").get("accepted").asBoolean(), "判定紅桃 → accepted=false");
                }
            }
            assertEquals("剛烈判定紅桃，未生效", skillEffectMessage,
                    playerId + " 的事件層 message 應為具體結算文字，而非「武將技發動結果」");
        }
    }
}

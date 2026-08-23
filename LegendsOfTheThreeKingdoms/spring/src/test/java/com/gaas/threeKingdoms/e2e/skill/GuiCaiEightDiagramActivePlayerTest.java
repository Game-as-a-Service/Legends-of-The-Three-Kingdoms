package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
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
 * 使用者回報：A 回合 A 殺 B（八卦陣），C=司馬懿 鬼才換牌結束後，activePlayer 停在 C 而非回到 A。
 */
public class GuiCaiEightDiagramActivePlayerTest extends AbstractBaseIntegrationTest {

    private void givenAKillsBWithEightDiagram_andCIsSimaYi() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Peach(BH4030));
        Player playerB = createPlayer("player-b", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        playerB.getEquipment().setArmor(new EightDiagramTactic(EC2067));
        Player playerC = createPlayer("player-c", 4, General.司馬懿, HealthStatus.ALIVE, Role.REBEL,
                new Peach(BH3029));
        Player playerD = createPlayer("player-d", 4, General.孫權, HealthStatus.ALIVE, Role.MINISTER);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Kill(BS9009))); // 八卦陣判定牌：黑桃 → 原本失敗
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", "BS8008", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.useEquipment(gameId, "player-b", "player-b", "EC2067", EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk());

        Game paused = repository.findById(gameId).orElseThrow();
        assertEquals("player-c", paused.getCurrentRound().getActivePlayer().getId(), "鬼才詢問中 activePlayer = C");
    }

    private void guiCai(String choice, String cardIdsJson) throws Exception {
        mockMvc.perform(post("/api/games/" + gameId + "/player:useSkillEffect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"playerId\": \"player-c\", \"skillName\": \"鬼才\", \"choice\": \"" + choice + "\""
                                + (cardIdsJson == null ? "" : ", \"cardIds\": " + cardIdsJson) + " }"))
                .andExpect(status().isOk());
    }

    @Test
    public void guiCaiAcceptMakesEightDiagramSucceed_activePlayerBackToA() throws Exception {
        givenAKillsBWithEightDiagram_andCIsSimaYi();

        guiCai("ACCEPT", "[\"BH3029\"]"); // 紅心替換 → 八卦陣成功（視為出閃）

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(4, saved.getPlayer("player-b").getHP());
        assertTrue(saved.getTopBehavior().isEmpty(), "殺已結算");
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId(),
                "結算完 activePlayer 應回到回合主 A");

        // A 應能繼續出牌（出桃自療驗證 activePlayer 檢查通過）
        mockMvcUtil.playCard(gameId, "player-a", "player-a", "BH4030", PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
    }

    @Test
    public void guiCaiSkipEightDiagramFails_BAsksDodge_thenActivePlayerBackToA() throws Exception {
        givenAKillsBWithEightDiagram_andCIsSimaYi();

        guiCai("SKIP", null); // 黑桃 → 八卦陣失敗 → 問 B 出閃

        Game afterSkip = repository.findById(gameId).orElseThrow();
        assertFalse(afterSkip.getTopBehavior().isEmpty(), "等 B 回應出閃");
        assertNotEquals("player-c", afterSkip.getCurrentRound().getActivePlayer().getId(),
                "activePlayer 不應停在 C");

        // B 不出閃 → 受傷 → 結算
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip")
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(3, saved.getPlayer("player-b").getHP());
        assertTrue(saved.getTopBehavior().isEmpty());
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId(),
                "結算完 activePlayer 應回到回合主 A");
    }
}

package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.round.Stage;
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
 * 流離先於鐵騎與八卦陣（跨 HTTP 請求，每步經 MongoDB）：
 * 馬超殺裝八卦陣的大喬 → 先問流離 → 大喬放棄 → 判鐵騎（黑色未生效）→ 問八卦陣。
 */
public class LiuLiBeforeTieQiFlowTest extends AbstractBaseIntegrationTest {

    @DisplayName("馬超殺有八卦陣的大喬：流離 → 放棄 → 鐵騎黑色 → 問八卦陣")
    @Test
    public void liuLiThenTieQiThenEightDiagram() throws Exception {
        Player maChao = createPlayer("player-a", 4, General.馬超, HealthStatus.ALIVE, Role.MONARCH, new Kill(BS8008));
        Player daQiao = createPlayer("player-b", 3, General.大喬, HealthStatus.ALIVE, Role.REBEL,
                new Peach(BH3029), new Dodge(BH2028));
        daQiao.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(maChao, daQiao, playerC, playerD), maChao);
        Deck deck = new Deck();
        deck.add(new ArrayList<>(List.of(new Peach(BH7033), new Kill(BS9009))));   // 鐵騎判定：黑桃9
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        Game asking = repository.findById(gameId).orElseThrow();
        WaitingSkillEffectBehavior liuLi = assertInstanceOf(WaitingSkillEffectBehavior.class, asking.peekTopBehavior());
        assertEquals("流離", liuLi.getSkillName());
        assertEquals(2, asking.getDeck().size(), "流離決定前還沒抽鐵騎判定牌");

        mockMvcUtil.useSkillEffect(gameId, "player-b", "流離", "SKIP", null, null).andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(1, saved.getDeck().size(), "放棄流離後才判鐵騎");
        assertEquals(Stage.Wait_Equipment_Effect, saved.getCurrentRound().getStage(), "鐵騎黑色 → 問八卦陣");
        NormalActiveKillBehavior kill = assertInstanceOf(NormalActiveKillBehavior.class, saved.peekTopBehavior());
        assertEquals("player-b", kill.getParam(NormalActiveKillBehavior.PARAM_LIULI_DECIDED_TARGET), "已決定流離（MongoDB 讀回）");
        assertEquals(3, saved.getPlayer("player-b").getHP());
    }
}

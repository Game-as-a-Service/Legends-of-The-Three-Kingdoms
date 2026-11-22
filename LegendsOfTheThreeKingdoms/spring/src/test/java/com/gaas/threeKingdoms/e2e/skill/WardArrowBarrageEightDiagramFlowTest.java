package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
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
 * 線上重現：甄姬萬箭 → 諸葛亮（無懈 + 八卦陣）兩次不出無懈 → 被問八卦陣 → 發動八卦陣原本 400（stage 被蓋回 Normal）。
 * 每步經 HTTP、MongoDB 讀回。
 */
public class WardArrowBarrageEightDiagramFlowTest extends AbstractBaseIntegrationTest {

    @DisplayName("萬箭第二階段無懈放棄後，下一位發動八卦陣 → 200，判定黑 → 問閃")
    @Test
    public void eightDiagramAfterPhaseTwoWardSkip() throws Exception {
        Player zhenJi = createPlayer("player-a", 4, General.甄姬, HealthStatus.ALIVE, Role.MONARCH, new ArrowBarrage(SHA040));
        Player zhuge = createPlayer("player-b", 3, General.諸葛亮, HealthStatus.ALIVE, Role.MINISTER,
                new Ward(SCQ077), new Dodge(BH2028));
        zhuge.getEquipment().setArmor(new EightDiagramTactic(EC2067));
        Player c = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player d = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(zhenJi, zhuge, c, d), zhenJi);
        Deck deck = new Deck();
        deck.add(new ArrayList<>(List.of(new Kill(BS9009)))); // 八卦陣判定：黑桃 → 失敗
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-a", SHA040.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());
        mockMvcUtil.playWardCard(gameId, "player-b", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());

        Game asking = repository.findById(gameId).orElseThrow();
        assertEquals(Stage.Wait_Equipment_Effect, asking.getCurrentRound().getStage(), "問八卦陣時 stage 要對（MongoDB 讀回）");
        assertEquals("player-b", asking.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.useEquipment(gameId, "player-b", "player-b", EC2067.getCardId(), EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk());

        Game after = repository.findById(gameId).orElseThrow();
        assertEquals("player-b", after.getCurrentRound().getActivePlayer().getId(), "八卦陣失敗 → 問諸葛亮出閃");
        assertEquals(3, after.getPlayer("player-b").getHP());
    }
}

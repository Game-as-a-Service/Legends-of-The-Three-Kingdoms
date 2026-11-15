package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.YinYangSwordsCard;
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
 * 鐵騎先於雌雄雙股劍，跨多個 HTTP 請求（每步經 MongoDB 重建 behavior）：
 * 鐵騎結果要記在殺的 behavior 並持久化，雙股劍結算完才讀得到。
 */
public class TieQiYinYangSwordsFlowTest extends AbstractBaseIntegrationTest {

    @DisplayName("鐵騎紅色 → 雙股劍發動 → 黃月英讓馬超摸一張 → 黃月英直接受傷（鐵騎結果從 MongoDB 讀回）")
    @Test
    public void redTieQiPersistsThroughYinYangSwords() throws Exception {
        Player maChao = createPlayer("player-a", 4, General.馬超, HealthStatus.ALIVE, Role.MONARCH, new Kill(BS8008));
        maChao.getEquipment().setWeapon(new YinYangSwordsCard(ES2002));
        Player huangYueYing = createPlayer("player-b", 3, General.黃月英, HealthStatus.ALIVE, Role.REBEL, new Dodge(BH2028));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(maChao, huangYueYing, playerC, playerD), maChao);
        Deck deck = new Deck();
        deck.add(new ArrayList<>(List.of(new Peach(BH7033), new Kill(BS9009), new Peach(BH3029))));   // 鐵騎判定：紅心3
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        Game asking = repository.findById(gameId).orElseThrow();
        NormalActiveKillBehavior kill = NormalActiveKillBehavior.findIn(asking);
        assertEquals("true", kill.getParam(NormalActiveKillBehavior.PARAM_TIEQI_SUCCESS), "鐵騎結果要從 MongoDB 讀回");

        mockMvcUtil.activateYinYangSwords(gameId, "player-a", "ACTIVATE").andExpect(status().isOk());
        mockMvcUtil.useYinYangSwordsEffect(gameId, "player-b", "ATTACKER_DRAWS", "").andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(2, saved.getPlayer("player-b").getHP(), "鐵騎生效：不能出閃，直接受傷");
        assertEquals(1, saved.getPlayer("player-b").getHandSize(), "閃沒被用掉");
        assertEquals(1, saved.getPlayer("player-a").getHandSize(), "雙股劍：馬超摸一張");
        assertTrue(saved.isTopBehaviorEmpty());
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
    }
}

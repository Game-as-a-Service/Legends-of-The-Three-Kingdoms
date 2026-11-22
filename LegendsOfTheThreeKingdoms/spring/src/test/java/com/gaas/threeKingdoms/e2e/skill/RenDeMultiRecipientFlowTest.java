package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
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

/** 仁德一次分給多人：經 HTTP 帶 targetPlayerIds，MongoDB 讀回後各接收者拿到對的牌、劉備回 1 體力。 */
public class RenDeMultiRecipientFlowTest extends AbstractBaseIntegrationTest {

    private void givenLiuBei() {
        Player liuBei = createPlayer("player-a", 4, General.劉備, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028));
        liuBei.getBloodCard().setHp(2); // 最大 4、目前 2，才能回血
        Player b = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        Player c = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player d = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        repository.save(initGame(gameId, Arrays.asList(liuBei, b, c, d), liuBei));
    }

    @DisplayName("仁德一次把殺給 b、桃給 c → 讀回後 b、c 各拿到對的牌，劉備回 1 體力")
    @Test
    public void splitBetweenTwoPlayersInOneRequest() throws Exception {
        givenLiuBei();

        mockMvcUtil.useSkillEffectWithTargets(gameId, "player-a", "仁德", "ACCEPT",
                List.of(BS8008.getCardId(), BH3029.getCardId()), List.of("player-b", "player-c"))
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getPlayer("player-b").getHand().getCard(BS8008.getCardId()).isPresent());
        assertTrue(saved.getPlayer("player-c").getHand().getCard(BH3029.getCardId()).isPresent());
        assertEquals(1, saved.getPlayer("player-a").getHandSize());
        assertEquals(3, saved.getPlayer("player-a").getHP());
        assertTrue(saved.getCurrentRound().isRenDeHealed());
    }

    @DisplayName("targetPlayerIds 數量不對 → 400，沒有任何牌被移動")
    @Test
    public void mismatchedTargetsRejected() throws Exception {
        givenLiuBei();

        mockMvcUtil.useSkillEffectWithTargets(gameId, "player-a", "仁德", "ACCEPT",
                List.of(BS8008.getCardId(), BH3029.getCardId()), List.of("player-b"))
                .andExpect(status().isBadRequest());

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(3, saved.getPlayer("player-a").getHandSize());
        assertEquals(0, saved.getPlayer("player-b").getHandSize());
    }
}

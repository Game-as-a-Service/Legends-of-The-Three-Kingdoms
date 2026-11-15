package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.YinYangSwordsCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 連營（陸遜）：自己在非一般出牌路徑失去最後一張手牌也要摸牌 — 走 HTTP 多請求 + MongoDB reload。
 */
public class LianYingSelfLosePathsTest extends AbstractBaseIntegrationTest {

    @DisplayName("雌雄雙股劍：陸遜棄最後一張手牌（出殺、發動、棄牌三個請求）→ 連營摸一張，接著被問閃")
    @Test
    public void yinYangSwordsTargetDiscardsLast_luXunDrawsOne() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甄姬, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        playerA.getEquipment().setWeapon(new YinYangSwordsCard(ES2002));
        Player playerB = createPlayer("player-b", 4, General.陸遜, HealthStatus.ALIVE, Role.MINISTER,
                new Peach(BH3029));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        repository.save(initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA));

        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.activateYinYangSwords(gameId, "player-a", "ACTIVATE").andExpect(status().isOk());
        mockMvcUtil.useYinYangSwordsEffect(gameId, "player-b", "TARGET_DISCARDS", BH3029.getCardId())
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        Player luXun = saved.getPlayer("player-b");
        assertEquals(1, luXun.getHandSize(), "棄光 → 連營摸 1");
        assertTrue(luXun.getHand().getCard(BH3029.getCardId()).isEmpty(), "棄掉的桃已進墓地");
        assertEquals("player-b", saved.getCurrentRound().getActivePlayer().getId(), "接著問陸遜出閃");
    }

    @DisplayName("無懈可擊：陸遜以最後一張手牌抵銷過河拆橋（出牌、選牌、出無懈三個請求）→ 連營摸一張")
    @Test
    public void wardWithLastHandCard_luXunDrawsOne() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Dismantle(SS4004));
        Player playerB = createPlayer("player-b", 4, General.陸遜, HealthStatus.ALIVE, Role.MINISTER,
                new Ward(SSJ011));
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        repository.save(initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA));

        mockMvcUtil.playCard(gameId, "player-a", "player-b", SS4004.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.useDismantleEffect(gameId, "player-a", "player-b", null, 0)
                .andExpect(status().isOk());
        mockMvcUtil.playWardCard(gameId, "player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        Player luXun = saved.getPlayer("player-b");
        assertEquals(1, luXun.getHandSize(), "無懈用掉最後一張 → 連營摸 1，過河拆橋被抵銷");
        assertTrue(luXun.getHand().getCard(SSJ011.getCardId()).isEmpty());
        assertTrue(saved.isTopBehaviorEmpty());
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId());
    }
}

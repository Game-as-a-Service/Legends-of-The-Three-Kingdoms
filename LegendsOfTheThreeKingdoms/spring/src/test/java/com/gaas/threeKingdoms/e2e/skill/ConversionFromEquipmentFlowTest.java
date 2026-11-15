package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.VioletStallion;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
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
 * 轉化技來源取自裝備區（武聖 / 奇襲 / 國色）：經 HTTP + MongoDB round-trip 驗證
 * 來源牌確實離開裝備區、進棄牌堆（國色則進判定區），且後續請求能接續結算。
 */
public class ConversionFromEquipmentFlowTest extends AbstractBaseIntegrationTest {

    private Game givenGame(Player playerA, Player playerB) {
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        return initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
    }

    @DisplayName("武聖：裝備區麒麟弓當殺 → 讀回後武器已離開、在棄牌堆；下一個請求目標不出閃扣 1 血")
    @Test
    public void wuShengEquippedWeaponAsKillAcrossRequests() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.關羽, HealthStatus.ALIVE, Role.MONARCH);
        playerA.getEquipment().setWeapon(new QilinBowCard(EH5031));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        repository.save(givenGame(playerA, playerB));

        mockMvcUtil.useSkillEffect(gameId, "player-a", "武聖", "KILL", List.of(EH5031.getCardId()), "player-b")
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertNull(saved.getPlayer("player-a").getEquipmentWeaponCard(), "麒麟弓離開裝備區");
        assertEquals(0, saved.getPlayer("player-a").getHandSize());
        assertTrue(saved.getGraveyard().contains(EH5031.getCardId()));

        mockMvcUtil.playCardWithoutCardId(gameId, "player-b", "player-a", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());

        Game afterSkip = repository.findById(gameId).orElseThrow();
        assertEquals(3, afterSkip.getPlayer("player-b").getHP());
        assertNull(afterSkip.getPlayer("player-b").getEquipmentWeaponCard(), "麒麟弓不會被「裝」到目標身上");
        assertTrue(afterSkip.isTopBehaviorEmpty());
    }

    @DisplayName("武聖：紅色手牌（桃）當殺跨請求 → 讀回的殺仍是殺：目標不出閃扣 1 血（不是吃桃）")
    @Test
    public void wuShengHandCardAsKillAcrossRequests() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.關羽, HealthStatus.ALIVE, Role.MONARCH, new Peach(BH3029));
        Player playerB = createPlayer("player-b", 3, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        repository.save(givenGame(playerA, playerB));

        mockMvcUtil.useSkillEffect(gameId, "player-a", "武聖", "KILL", List.of(BH3029.getCardId()), "player-b")
                .andExpect(status().isOk());
        mockMvcUtil.playCardWithoutCardId(gameId, "player-b", "player-a", PlayType.SKIP.getPlayType())
                .andExpect(status().isOk());

        Game afterSkip = repository.findById(gameId).orElseThrow();
        assertEquals(2, afterSkip.getPlayer("player-b").getHP(), "3 血被殺 → 2，不是被桃回到 4");
        assertTrue(afterSkip.isTopBehaviorEmpty());
    }

    @DisplayName("奇襲：裝備區諸葛連弩當過河拆橋 → 下一個請求選拆目標裝備，兩張都進棄牌堆")
    @Test
    public void qiXiEquippedWeaponAsDismantleAcrossRequests() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH);
        playerA.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        playerB.getEquipment().setWeapon(new QilinBowCard(EH5031));
        repository.save(givenGame(playerA, playerB));

        mockMvcUtil.useSkillEffect(gameId, "player-a", "奇襲", "DISMANTLE", List.of(ECA066.getCardId()), "player-b")
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertNull(saved.getPlayer("player-a").getEquipmentWeaponCard(), "連弩離開裝備區");
        assertTrue(saved.getGraveyard().contains(ECA066.getCardId()));

        mockMvcUtil.useDismantleEffect(gameId, "player-a", "player-b", EH5031.getCardId(), null)
                .andExpect(status().isOk());

        Game afterDismantle = repository.findById(gameId).orElseThrow();
        assertNull(afterDismantle.getPlayer("player-b").getEquipmentWeaponCard(), "目標麒麟弓被拆");
        assertTrue(afterDismantle.getGraveyard().contains(EH5031.getCardId()));
        assertTrue(afterDismantle.isTopBehaviorEmpty());
    }

    @DisplayName("國色：裝備區紫騂當樂不思蜀 → 讀回後在目標判定區且還原為樂不思蜀，不在大喬裝備區")
    @Test
    public void guoSeEquippedHorseAsContentmentSurvivesReload() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.大喬, HealthStatus.ALIVE, Role.MONARCH);
        playerA.getEquipment().setMinusOne(new VioletStallion(EDK104));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        repository.save(givenGame(playerA, playerB));

        mockMvcUtil.useSkillEffect(gameId, "player-a", "國色", "CONTENTMENT", List.of(EDK104.getCardId()), "player-b")
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertNull(saved.getPlayer("player-a").getEquipmentMinusOneMountsCard(), "紫騂離開裝備區");
        List<? extends HandCard> delay = saved.getPlayer("player-b").getDelayScrollCards();
        assertEquals(1, delay.size());
        assertInstanceOf(Contentment.class, delay.get(0));
        assertEquals(EDK104.getCardId(), delay.get(0).getId());
        assertFalse(saved.getGraveyard().contains(EDK104.getCardId()));
    }
}

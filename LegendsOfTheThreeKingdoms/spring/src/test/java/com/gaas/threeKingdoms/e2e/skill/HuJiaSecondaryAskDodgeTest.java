package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingHuJiaResponseBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.GreenDragonCrescentBladeCard;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 護駕在二次問閃場景（跨多個 HTTP 請求，每步經 MongoDB reload）：
 * 八卦陣判定失敗、青龍偃月刀追殺。
 */
public class HuJiaSecondaryAskDodgeTest extends AbstractBaseIntegrationTest {

    private static final String ACTIVE = PlayType.ACTIVE.getPlayType();

    @Test
    public void eightDiagramFail_CaoCaoAcceptsHuJia_WeiDodgesForHim() throws Exception {
        // Given A 甘寧出殺；B 曹操主公裝八卦陣；D 夏侯惇（魏）有閃；牌堆頂為黑色判定牌
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL, new Kill(BS8008));
        Player playerB = createPlayer("player-b", 4, General.曹操, HealthStatus.ALIVE, Role.MONARCH);
        playerB.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        Player playerC = createPlayer("player-c", 4, General.趙雲, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.夏侯惇, HealthStatus.ALIVE, Role.MINISTER, new Dodge(BH2028));
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH3029), new Kill(BS9009)));   // 最後一張在頂：黑桃 → 八卦陣失敗
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(), ACTIVE)
                .andExpect(status().isOk());

        // When 曹操發動八卦陣 → 判定失敗
        mockMvcUtil.useEquipment(gameId, "player-b", "player-b", ES2015.getCardId(), EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk());

        // Then 不直接問閃，改詢問曹操是否發動護駕
        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.peekTopBehavior() instanceof WaitingSkillEffectBehavior, "應等待曹操決定護駕");
        assertEquals("player-b", saved.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.useSkillEffect(gameId, "player-b", "護駕", "ACCEPT", null, null)
                .andExpect(status().isOk());
        saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.peekTopBehavior() instanceof WaitingHuJiaResponseBehavior);
        assertEquals("player-d", saved.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.useHuJiaEffect(gameId, "player-d", "ACCEPT", BH2028.getCardId())
                .andExpect(status().isOk());

        saved = repository.findById(gameId).orElseThrow();
        assertEquals(4, saved.getPlayer("player-b").getHP(), "魏將代閃，曹操不扣血");
        assertEquals(0, saved.getPlayer("player-d").getHandSize());
        assertTrue(saved.getTopBehavior().isEmpty(), "殺結算完畢");
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId());
    }

    @Test
    public void eightDiagramFail_CaoCaoSkipsHuJia_TakesDamage() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL, new Kill(BS8008));
        Player playerB = createPlayer("player-b", 4, General.曹操, HealthStatus.ALIVE, Role.MONARCH);
        playerB.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        Player playerC = createPlayer("player-c", 4, General.趙雲, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.夏侯惇, HealthStatus.ALIVE, Role.MINISTER, new Dodge(BH2028));
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH3029), new Kill(BS9009)));
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(), ACTIVE).andExpect(status().isOk());
        mockMvcUtil.useEquipment(gameId, "player-b", "player-b", ES2015.getCardId(), EquipmentPlayType.ACTIVE)
                .andExpect(status().isOk());

        mockMvcUtil.useSkillEffect(gameId, "player-b", "護駕", "SKIP", null, null).andExpect(status().isOk());
        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.peekTopBehavior() instanceof NormalActiveKillBehavior);
        assertEquals("player-b", saved.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());

        saved = repository.findById(gameId).orElseThrow();
        assertEquals(3, saved.getPlayer("player-b").getHP());
        assertEquals(1, saved.getPlayer("player-d").getHandSize(), "放棄護駕不可動用魏將的閃");
    }

    @Test
    public void greenDragonFollowUpKill_AsksHuJiaAgain_WeiDodgesForCaoCao() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL,
                new Kill(BS8008), new Kill(BS9009));
        playerA.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));
        Player playerB = createPlayer("player-b", 4, General.曹操, HealthStatus.ALIVE, Role.MONARCH, new Dodge(BHK039));
        Player playerC = createPlayer("player-c", 4, General.趙雲, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.夏侯惇, HealthStatus.ALIVE, Role.MINISTER, new Dodge(BH2028));
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH3029)));
        game.setDeck(deck);
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(), ACTIVE).andExpect(status().isOk());
        mockMvcUtil.useSkillEffect(gameId, "player-b", "護駕", "SKIP", null, null).andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", BHK039.getCardId(), ACTIVE).andExpect(status().isOk());

        // 青龍追殺 → 再次詢問護駕
        mockMvcUtil.useGreenDragonCrescentBladeEffect(gameId, "player-a", "KILL", BS9009.getCardId())
                .andExpect(status().isOk());
        Game saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.peekTopBehavior() instanceof WaitingSkillEffectBehavior, "追殺應再次詢問護駕");
        assertEquals("player-b", saved.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.useSkillEffect(gameId, "player-b", "護駕", "ACCEPT", null, null).andExpect(status().isOk());
        mockMvcUtil.useHuJiaEffect(gameId, "player-d", "ACCEPT", BH2028.getCardId()).andExpect(status().isOk());

        saved = repository.findById(gameId).orElseThrow();
        assertEquals(4, saved.getPlayer("player-b").getHP());
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId(), "代閃後攻擊者可再發動青龍");

        mockMvcUtil.useGreenDragonCrescentBladeEffect(gameId, "player-a", "SKIP", null).andExpect(status().isOk());
        saved = repository.findById(gameId).orElseThrow();
        assertTrue(saved.getTopBehavior().isEmpty());
    }
}

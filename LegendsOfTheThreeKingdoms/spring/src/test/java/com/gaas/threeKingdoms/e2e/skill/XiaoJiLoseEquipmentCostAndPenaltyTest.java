package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.StonePiercingAxeCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
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
 * 梟姬（issue #195 後續）最後三條「失去裝備」路徑，都跨 request，只有 spring e2e 蓋得到：
 * <ul>
 *   <li>借刀殺人奪武器：B 不出殺（另一個 request）/ B 根本沒殺（useBorrowedSwordEffect 當下直接奪）</li>
 *   <li>貫石斧的代價棄到裝備區的牌（useStonePiercingAxeEffect 是獨立 request）</li>
 *   <li>主公殺死忠臣的罰則棄光裝備（一次失去多張 → per-card 摸 2N）</li>
 * </ul>
 * 純 domain 的斷言（含事件順序）見 {@code Batch2TriggeredSkillsTest}；
 * 制衡棄裝備那條 hook 只有 domain 測試 — 制衡是孫權的技，正常牌局不會有人同時擁有梟姬。
 */
public class XiaoJiLoseEquipmentCostAndPenaltyTest extends AbstractBaseIntegrationTest {

    /** 牌堆放足夠的牌：梟姬最多摸 4 張後仍夠下一回合摸牌階段，finishAction 才不會 400。 */
    private void giveDeckWithEnoughCards(Game game) {
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH8034), new Peach(BH7033), new Peach(BH3029),
                new Peach(BH4030), new Peach(BH6032), new Peach(BH9035), new Peach(BHQ038),
                new Dodge(BH2028), new Dodge(BHK039), new Dodge(BH2041)));
        game.setDeck(deck);
    }

    private Game givenFourPlayerGame(Player playerA, Player playerB) {
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        giveDeckWithEnoughCards(game);
        return game;
    }

    // ===== 借刀殺人：B 的武器被 A 奪走 =====

    /** a = 甘寧（回合主，一張借刀殺人）。 */
    private Player givenBorrowedSwordPlayer() {
        return createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new BorrowedSword(SCK065));
    }

    private void playBorrowedSwordOnPlayerB() throws Exception {
        mockMvcUtil.playCard(gameId, "player-a", "player-b", SCK065.getCardId(),
                PlayType.ACTIVE.getPlayType()).andExpect(status().isOk());
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c")
                .andExpect(status().isOk());
    }

    @DisplayName("孫尚香有殺卻選擇不出 → 武器被借刀殺人奪走，梟姬摸兩張")
    @Test
    public void borrowedSwordUsurpsSunShangXiangWeaponAfterSkip_xiaoJiDrawsTwo() throws Exception {
        Player playerB = createPlayer("player-b", 4, General.孫尚香, HealthStatus.ALIVE, Role.MINISTER,
                new Kill(BS8008));
        playerB.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));
        repository.save(givenFourPlayerGame(givenBorrowedSwordPlayer(), playerB));

        playBorrowedSwordOnPlayerB();
        mockMvcUtil.playCard(gameId, "player-b", "player-c", "", "skip").andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        Player sunShangXiang = saved.getPlayer("player-b");
        assertEquals(3, sunShangXiang.getHandSize(), "留著沒出的殺 + 梟姬摸的兩張");
        assertNull(sunShangXiang.getEquipmentWeaponCard(), "武器已離開孫尚香的裝備區");
        assertTrue(saved.getPlayer("player-a").getHand().getCards().stream()
                        .anyMatch(card -> card.getId().equals(ECA066.getCardId())),
                "被奪的武器進出借刀殺人者手牌，不進棄牌堆");
        assertEquals(4, saved.getPlayer("player-c").getHP(), "B 沒出殺，C 不會受傷");

        // 結算完回合仍是 A，不會卡住
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId());
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
    }

    @DisplayName("孫尚香沒有殺 → 借刀殺人當下直接奪走武器，梟姬摸兩張")
    @Test
    public void borrowedSwordUsurpsSunShangXiangWeaponWithoutKill_xiaoJiDrawsTwo() throws Exception {
        Player playerB = createPlayer("player-b", 4, General.孫尚香, HealthStatus.ALIVE, Role.MINISTER);
        playerB.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));
        repository.save(givenFourPlayerGame(givenBorrowedSwordPlayer(), playerB));

        playBorrowedSwordOnPlayerB();

        Game saved = repository.findById(gameId).orElseThrow();
        Player sunShangXiang = saved.getPlayer("player-b");
        assertEquals(2, sunShangXiang.getHandSize(), "梟姬：武器被直接奪走也要摸兩張");
        assertNull(sunShangXiang.getEquipmentWeaponCard());
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("非孫尚香武器被借刀殺人奪走 → 不摸牌（對照組）")
    @Test
    public void nonSunShangXiangLosesWeaponToBorrowedSword_noDraw() throws Exception {
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        playerB.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));
        repository.save(givenFourPlayerGame(givenBorrowedSwordPlayer(), playerB));

        playBorrowedSwordOnPlayerB();

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(0, saved.getPlayer("player-b").getHandSize(), "沒有梟姬不該摸牌");
        assertNull(saved.getPlayer("player-b").getEquipmentWeaponCard());
    }

    // ===== 貫石斧：代價棄到裝備區的牌 =====

    @DisplayName("孫尚香貫石斧代價棄兩張裝備 → 梟姬 per-card 摸四張，殺強制命中")
    @Test
    public void stonePiercingAxeCostDiscardsTwoEquipment_xiaoJiDrawsFour() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.孫尚香, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        playerA.getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        playerA.getEquipment().setPlusOne(new ShadowHorse(ES5018));
        playerA.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER,
                new Dodge(BH2028));
        repository.save(givenFourPlayerGame(playerA, playerB));

        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(),
                PlayType.ACTIVE.getPlayType()).andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", BH2028.getCardId(),
                PlayType.ACTIVE.getPlayType()).andExpect(status().isOk());
        mockMvcUtil.useStonePiercingAxeEffect(gameId, "player-a", "DISCARD_TWO",
                List.of(EH5044.getCardId(), ES5018.getCardId())).andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        Player sunShangXiang = saved.getPlayer("player-a");
        assertEquals(4, sunShangXiang.getHandSize(), "一次失去 2 張裝備 → 每張各摸 2，共 4 張");
        assertNull(sunShangXiang.getEquipment().getPlusOne());
        assertNull(sunShangXiang.getEquipment().getMinusOne());
        assertNotNull(sunShangXiang.getEquipmentWeaponCard(), "貫石斧本身沒當代價棄掉");
        assertTrue(saved.getGraveyard().contains(EH5044.getCardId()));
        assertTrue(saved.getGraveyard().contains(ES5018.getCardId()));
        assertEquals(3, saved.getPlayer("player-b").getHP(), "貫石斧強制命中，閃無效");

        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
    }

    @DisplayName("非孫尚香貫石斧棄裝備當代價 → 不摸牌（對照組）")
    @Test
    public void nonSunShangXiangStonePiercingAxeCost_noDraw() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        playerA.getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        playerA.getEquipment().setPlusOne(new ShadowHorse(ES5018));
        playerA.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER,
                new Dodge(BH2028));
        repository.save(givenFourPlayerGame(playerA, playerB));

        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(),
                PlayType.ACTIVE.getPlayType()).andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", BH2028.getCardId(),
                PlayType.ACTIVE.getPlayType()).andExpect(status().isOk());
        mockMvcUtil.useStonePiercingAxeEffect(gameId, "player-a", "DISCARD_TWO",
                List.of(EH5044.getCardId(), ES5018.getCardId())).andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(0, saved.getPlayer("player-a").getHandSize(), "沒有梟姬不該摸牌");
        assertEquals(3, saved.getPlayer("player-b").getHP());
    }

    // ===== 主公殺死忠臣：罰則棄光裝備區（一次失去多張）=====

    /** A(主公) 殺死 hp=1 的忠臣 B，b→c→d→a 依序不出桃。 */
    private void monarchKillsMinisterAndNobodyPlaysPeach() throws Exception {
        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(),
                PlayType.ACTIVE.getPlayType()).andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip").andExpect(status().isOk());
        for (String playerId : List.of("player-b", "player-c", "player-d", "player-a")) {
            mockMvcUtil.playCard(gameId, playerId, "player-b", "", "skip").andExpect(status().isOk());
        }
    }

    @DisplayName("孫尚香主公殺死忠臣、罰則棄光兩張裝備 → 梟姬摸四張")
    @Test
    public void monarchDiscardsAllAfterKillingMinister_xiaoJiDrawsPerEquipment() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.孫尚香, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        playerA.getEquipment().setArmor(new EightDiagramTactic(EC2067));
        playerA.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        Player playerB = createPlayer("player-b", 1, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        repository.save(givenFourPlayerGame(playerA, playerB));

        monarchKillsMinisterAndNobodyPlaysPeach();

        Game saved = repository.findById(gameId).orElseThrow();
        Player sunShangXiang = saved.getPlayer("player-a");
        assertFalse(sunShangXiang.getEquipment().hasAnyEquipment(), "主公殺忠臣罰則：手牌與裝備全棄");
        assertEquals(4, sunShangXiang.getHandSize(),
                "梟姬 per-card：罰則棄光 2 張裝備 → 摸 4 張（罰則已先結算完，摸到的留在手上）");
        assertTrue(saved.getSeatingChart().getPlayers().stream()
                .noneMatch(player -> player.getId().equals("player-b")), "忠臣已死亡離場");
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("非孫尚香主公殺忠臣棄光裝備 → 不摸牌（對照組）")
    @Test
    public void nonSunShangXiangMonarchDiscardsAll_noDraw() throws Exception {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        playerA.getEquipment().setArmor(new EightDiagramTactic(EC2067));
        playerA.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        Player playerB = createPlayer("player-b", 1, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        repository.save(givenFourPlayerGame(playerA, playerB));

        monarchKillsMinisterAndNobodyPlaysPeach();

        Game saved = repository.findById(gameId).orElseThrow();
        assertFalse(saved.getPlayer("player-a").getEquipment().hasAnyEquipment());
        assertEquals(0, saved.getPlayer("player-a").getHandSize(), "沒有梟姬不該摸牌");
    }
}

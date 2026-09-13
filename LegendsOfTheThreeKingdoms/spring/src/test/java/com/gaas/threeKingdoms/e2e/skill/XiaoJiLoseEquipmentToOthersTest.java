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
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
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
 * 梟姬（issue #195 後續）：官方「每當你失去一張裝備區裡的牌」沒有限定來源，
 * 被別人取走或棄掉一樣算失去。這裡守兩條被別人動裝備的路徑：
 * <ul>
 *   <li>司馬懿反饋取走孫尚香裝備區的牌（取手牌不算）</li>
 *   <li>麒麟弓棄掉孫尚香的馬（只有一匹直接移除 / 兩匹由攻擊方在另一個 request 選）</li>
 * </ul>
 * 這兩條都跨 request（詢問用的 behavior 會存進 MongoDB 再讀回來），只有 spring e2e 蓋得到；
 * 純 domain 的斷言見 {@code Batch2TriggeredSkillsTest}。
 */
public class XiaoJiLoseEquipmentToOthersTest extends AbstractBaseIntegrationTest {

    /** 牌堆放足夠的桃：梟姬摸 2 張後仍夠下一回合摸牌階段，finishAction 才不會 400。 */
    private void giveDeckWithEnoughCards(Game game) {
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH8034), new Peach(BH7033), new Peach(BH3029),
                new Peach(BH4030), new Peach(BH6032), new Peach(BH9035)));
        game.setDeck(deck);
    }

    private Game givenFourPlayerGame(Player playerA, Player playerB) {
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        giveDeckWithEnoughCards(game);
        return game;
    }

    // ===== 反饋 =====

    /** a = 孫尚香（回合主，一張殺 + 八卦陣）；b = 司馬懿。 */
    private Game givenSunShangXiangAttacksSimaYi() {
        Player playerA = createPlayer("player-a", 4, General.孫尚香, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        playerA.getEquipment().setArmor(new EightDiagramTactic(EC2067));
        Player playerB = createPlayer("player-b", 4, General.司馬懿, HealthStatus.ALIVE, Role.MINISTER);
        return givenFourPlayerGame(playerA, playerB);
    }

    @DisplayName("孫尚香殺司馬懿、反饋取走她的防具 → 梟姬摸兩張，防具進司馬懿手牌")
    @Test
    public void fanKuiTakesSunShangXiangEquipment_xiaoJiDrawsTwo() throws Exception {
        repository.save(givenSunShangXiangAttacksSimaYi());

        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(),
                PlayType.ACTIVE.getPlayType()).andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip").andExpect(status().isOk());
        mockMvcUtil.useSkillEffect(gameId, "player-b", "反饋", "ACCEPT",
                List.of(EC2067.getCardId()), null).andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        Player sunShangXiang = saved.getPlayer("player-a");
        assertEquals(2, sunShangXiang.getHandSize(), "梟姬：裝備被反饋取走也是失去裝備，要摸兩張");
        assertFalse(sunShangXiang.getEquipment().hasAnyEquipment(), "防具已離開孫尚香的裝備區");
        assertTrue(saved.getPlayer("player-b").getHand().getCards().stream()
                        .anyMatch(card -> card.getId().equals(EC2067.getCardId())),
                "反饋取走的裝備進司馬懿手牌，不進棄牌堆");
        assertFalse(saved.getGraveyard().contains(EC2067.getCardId()));

        // 結算完回合仍是孫尚香，不會卡住
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId());
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
    }

    @DisplayName("反饋取的是手牌 → 沒有失去裝備，梟姬不觸發（對照組）")
    @Test
    public void fanKuiTakesHandCardInstead_xiaoJiDoesNotTrigger() throws Exception {
        Game game = givenSunShangXiangAttacksSimaYi();
        game.getPlayer("player-a").getHand().addCardToHand(new Peach(BHQ038));
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(),
                PlayType.ACTIVE.getPlayType()).andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip").andExpect(status().isOk());
        // 不指定 → 取來源第一張手牌
        mockMvcUtil.useSkillEffect(gameId, "player-b", "反饋", "ACCEPT", null, null)
                .andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        Player sunShangXiang = saved.getPlayer("player-a");
        assertEquals(0, sunShangXiang.getHandSize(), "取手牌不是失去裝備，不可摸牌");
        assertEquals(EC2067.getCardId(), sunShangXiang.getEquipment().getArmor().getId(), "裝備沒被動到");
    }

    // ===== 麒麟弓 =====

    /** a = 甘寧（回合主，麒麟弓 + 一張殺）；b = 孫尚香。 */
    private Player givenQilinBowAttacker() {
        Player playerA = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008));
        playerA.getEquipment().setWeapon(new QilinBowCard(EH5031));
        return playerA;
    }

    private void killAndSkipDodge() throws Exception {
        mockMvcUtil.playCard(gameId, "player-a", "player-b", BS8008.getCardId(),
                PlayType.ACTIVE.getPlayType()).andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", "skip").andExpect(status().isOk());
    }

    @DisplayName("麒麟弓棄掉孫尚香唯一的馬 → 梟姬摸兩張，殺的傷害照算")
    @Test
    public void qilinBowRemovesSunShangXiangOnlyMount_xiaoJiDrawsTwo() throws Exception {
        Player playerB = createPlayer("player-b", 4, General.孫尚香, HealthStatus.ALIVE, Role.MINISTER);
        playerB.getEquipment().setPlusOne(new ShadowHorse(ES5018));
        repository.save(givenFourPlayerGame(givenQilinBowAttacker(), playerB));

        killAndSkipDodge();
        mockMvcUtil.useEquipment(gameId, "player-a", "player-b", EH5031.getCardId(),
                EquipmentPlayType.ACTIVE).andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        Player sunShangXiang = saved.getPlayer("player-b");
        assertEquals(2, sunShangXiang.getHandSize(), "梟姬：馬被麒麟弓棄掉也要摸兩張");
        assertNull(sunShangXiang.getEquipment().getPlusOne(), "馬已離開裝備區");
        assertEquals(3, sunShangXiang.getHP(), "麒麟弓棄馬不影響殺的傷害");
        assertTrue(saved.getGraveyard().contains(ES5018.getCardId()), "被棄的馬進棄牌堆");
    }

    @DisplayName("孫尚香有兩匹馬、攻擊方另一個 request 選一匹棄掉 → 梟姬摸兩張")
    @Test
    public void qilinBowAttackerChoosesWhichMountToDiscard_xiaoJiDrawsTwo() throws Exception {
        Player playerB = createPlayer("player-b", 4, General.孫尚香, HealthStatus.ALIVE, Role.MINISTER);
        playerB.getEquipment().setPlusOne(new ShadowHorse(ES5018));
        playerB.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        repository.save(givenFourPlayerGame(givenQilinBowAttacker(), playerB));

        killAndSkipDodge();
        mockMvcUtil.useEquipment(gameId, "player-a", "player-b", EH5031.getCardId(),
                EquipmentPlayType.ACTIVE).andExpect(status().isOk());

        Game askSaved = repository.findById(gameId).orElseThrow();
        assertEquals(0, askSaved.getPlayer("player-b").getHandSize(), "還沒選馬就不算失去裝備");
        assertEquals(4, askSaved.getPlayer("player-b").getHP(), "還沒選馬也還沒結算傷害");

        mockMvcUtil.chooseHorse(gameId, "player-a", ES5018.getCardId()).andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        Player sunShangXiang = saved.getPlayer("player-b");
        assertEquals(2, sunShangXiang.getHandSize(), "梟姬：被選中的馬離開裝備區 → 摸兩張");
        assertNull(sunShangXiang.getEquipment().getPlusOne());
        assertNotNull(sunShangXiang.getEquipment().getMinusOne(), "沒被選中的馬留著，只算失去一張");
        assertEquals(3, sunShangXiang.getHP());
    }

    @DisplayName("非孫尚香被麒麟弓棄馬 → 不摸牌（對照組）")
    @Test
    public void nonSunShangXiangLosesMountToQilinBow_noDraw() throws Exception {
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        playerB.getEquipment().setPlusOne(new ShadowHorse(ES5018));
        repository.save(givenFourPlayerGame(givenQilinBowAttacker(), playerB));

        killAndSkipDodge();
        mockMvcUtil.useEquipment(gameId, "player-a", "player-b", EH5031.getCardId(),
                EquipmentPlayType.ACTIVE).andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        assertEquals(0, saved.getPlayer("player-b").getHandSize(), "沒有梟姬不該摸牌");
        assertNull(saved.getPlayer("player-b").getEquipment().getPlusOne());
        assertEquals(3, saved.getPlayer("player-b").getHP());
    }
}

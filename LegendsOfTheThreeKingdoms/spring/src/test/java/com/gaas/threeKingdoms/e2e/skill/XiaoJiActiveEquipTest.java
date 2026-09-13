package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.StonePiercingAxeCard;
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
 * 使用者回報：「孫尚香 貫石斧替換武器後，[梟姬] 技能未生效」。
 * <p>
 * 主動換裝的舊裝備是被 {@code Equipment.setWeapon()} 直接覆寫掉的，不經任何「移除」流程，
 * 所以既沒觸發梟姬、舊裝備也沒進棄牌堆。官方梟姬只界定「失去裝備區裡的牌」、未限定來源，
 * 自己換裝也算失去 → 要摸兩張。
 * 走真實 HTTP + MongoDB 路徑守住（domain 覆蓋見 Batch2TriggeredSkillsTest）。
 */
public class XiaoJiActiveEquipTest extends AbstractBaseIntegrationTest {

    /** a = 孫尚香（回合主，手上一張貫石斧）；牌堆放數張桃（梟姬摸 2 張後仍夠下一回合摸牌）。 */
    private Game givenSunShangXiangHoldingStonePiercingAxe() {
        Player playerA = createPlayer("player-a", 4, General.孫尚香, HealthStatus.ALIVE, Role.MONARCH,
                new StonePiercingAxeCard(ED5083));
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Game game = initGame(gameId, Arrays.asList(playerA, playerB, playerC, playerD), playerA);
        Deck deck = new Deck();
        deck.add(List.of(new Peach(BH8034), new Peach(BH7033), new Peach(BH3029), new Peach(BH4030)));
        game.setDeck(deck);
        return game;
    }

    @DisplayName("孫尚香用貫石斧換掉諸葛連弩 → 梟姬摸兩張，舊武器進棄牌堆")
    @Test
    public void sunShangXiangReplacesOwnWeapon_xiaoJiDrawsTwo() throws Exception {
        Game game = givenSunShangXiangHoldingStonePiercingAxe();
        game.getPlayer("player-a").getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));
        repository.save(game);

        mockMvcUtil.playCard(gameId, "player-a", "player-a", ED5083.getCardId(),
                PlayType.ACTIVE.getPlayType()).andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        Player sunShangXiang = saved.getPlayer("player-a");
        assertEquals(ED5083.getCardId(), sunShangXiang.getEquipment().getWeapon().getId(),
                "裝備區應換成貫石斧");
        assertEquals(2, sunShangXiang.getHandSize(), "梟姬：失去舊武器要摸兩張");
        assertTrue(saved.getGraveyard().contains(ECA066.getCardId()),
                "被換掉的舊武器要進棄牌堆，不能從牌堆循環中消失");
        assertFalse(saved.getGraveyard().contains(ED5083.getCardId()),
                "新武器在裝備區，不該同時在棄牌堆");

        // 換裝完回合仍為孫尚香，可正常結束行動
        assertEquals("player-a", saved.getCurrentRound().getActivePlayer().getId());
        mockMvcUtil.finishAction(gameId, "player-a").andExpect(status().isOk());
    }

    @DisplayName("孫尚香裝備區原本沒武器 → 沒有失去裝備，梟姬不觸發（對照組）")
    @Test
    public void sunShangXiangEquipsIntoEmptySlot_xiaoJiDoesNotTrigger() throws Exception {
        repository.save(givenSunShangXiangHoldingStonePiercingAxe());

        mockMvcUtil.playCard(gameId, "player-a", "player-a", ED5083.getCardId(),
                PlayType.ACTIVE.getPlayType()).andExpect(status().isOk());

        Game saved = repository.findById(gameId).orElseThrow();
        Player sunShangXiang = saved.getPlayer("player-a");
        assertEquals(ED5083.getCardId(), sunShangXiang.getEquipment().getWeapon().getId());
        assertEquals(0, sunShangXiang.getHandSize(), "空欄位裝新武器沒有「失去」，不可摸牌");
    }
}

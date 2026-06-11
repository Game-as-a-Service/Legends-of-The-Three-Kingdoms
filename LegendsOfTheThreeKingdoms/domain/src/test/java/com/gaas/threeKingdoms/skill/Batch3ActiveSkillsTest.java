package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Batch 3：制衡 / 突襲 / 苦肉 / 反間 / 結姻 / 仁德 / 觀星（proactive useSkillEffect 分支）。
 */
public class Batch3ActiveSkillsTest extends PassiveSkillTestBase {

    // ===== 制衡 =====

    @DisplayName("孫權制衡：棄 2（手牌+裝備）摸 2")
    @Test
    public void zhiHengDiscardsAndDrawsEqual() {
        Game game = createGame(General.孫權, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));
        a.getEquipment().setWeapon(new QilinBowCard(EH5031));

        game.playerUseSkillEffect("player-a", "制衡", "ACCEPT",
                List.of(BS8008.getCardId(), EH5031.getCardId()), null);

        assertEquals(2, a.getHandSize(), "棄 2 摸 2");
        assertFalse(a.getEquipment().hasAnyEquipment());
        assertTrue(game.getGraveyard().contains(BS8008.getCardId()));
    }

    @DisplayName("非孫權發動制衡 → 拋例外")
    @Test
    public void zhiHengOnlyForSunQuan() {
        Game game = createGame(General.劉備, General.劉備, General.劉備, General.劉備);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));

        assertThrows(IllegalStateException.class, () ->
                game.playerUseSkillEffect("player-a", "制衡", "ACCEPT", List.of(BS8008.getCardId()), null));
    }

    // ===== 突襲 =====

    @DisplayName("張遼突襲：取兩名角色各一張手牌；每回合限一次")
    @Test
    public void tuXiTakesOneCardFromEachTarget() {
        Game game = createGame(General.張遼, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        game.getPlayer("player-b").getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-c").getHand().addCardToHand(new Dodge(BH2028));

        game.playerUseSkillEffect("player-a", "突襲", "ACCEPT",
                List.of("player-b", "player-c"), null);

        assertEquals(2, a.getHandSize());
        assertEquals(0, game.getPlayer("player-b").getHandSize());
        assertEquals(0, game.getPlayer("player-c").getHandSize());

        // 每回合限一次
        game.getPlayer("player-b").getHand().addCardToHand(new Kill(BS9009));
        assertThrows(IllegalStateException.class, () ->
                game.playerUseSkillEffect("player-a", "突襲", "ACCEPT", List.of("player-b"), null));
    }

    // ===== 苦肉 =====

    @DisplayName("黃蓋苦肉：-1 HP 摸 2；HP=1 不可發動")
    @Test
    public void kuRouDamagesSelfAndDrawsTwo() {
        Game game = createGame(General.黃蓋, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");

        game.playerUseSkillEffect("player-a", "苦肉", "ACCEPT", null, null);

        assertEquals(3, a.getHP());
        assertEquals(2, a.getHandSize());

        a.getBloodCard().setHp(1);
        assertThrows(IllegalStateException.class, () ->
                game.playerUseSkillEffect("player-a", "苦肉", "ACCEPT", null, null));
    }

    // ===== 反間 =====

    @DisplayName("周瑜反間：目標猜錯花色 → 得牌但受 1 傷")
    @Test
    public void fanJianWrongGuessDamagesTarget() {
        Game game = createGame(General.周瑜, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008)); // 黑桃

        List<DomainEvent> events = game.playerUseSkillEffect("player-a", "反間", "ACCEPT",
                List.of(BS8008.getCardId()), "player-b");
        assertTrue(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent
                && ((AskSkillEffectEvent) e).getPlayerId().equals("player-b")));

        // b 猜紅心（實際黑桃）→ 受 1 傷、仍得牌
        game.playerUseSkillEffect("player-b", "反間", "HEART", null, null);

        assertEquals(3, b.getHP());
        assertTrue(b.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BS8008.getCardId())));
        assertEquals(0, a.getHandSize());
    }

    @DisplayName("周瑜反間：目標猜中花色 → 得牌不受傷；每回合限一次")
    @Test
    public void fanJianCorrectGuessNoDamage() {
        Game game = createGame(General.周瑜, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029)));

        game.playerUseSkillEffect("player-a", "反間", "ACCEPT", List.of(BS8008.getCardId()), "player-b");
        game.playerUseSkillEffect("player-b", "反間", "SPADE", null, null);

        assertEquals(4, b.getHP(), "猜中不受傷");
        assertTrue(b.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BS8008.getCardId())));

        assertThrows(IllegalStateException.class, () ->
                game.playerUseSkillEffect("player-a", "反間", "ACCEPT", List.of(BH3029.getCardId()), "player-b"));
    }

    // ===== 結姻 =====

    @DisplayName("孫尚香結姻：棄 2 手牌，自己與傷最重者各回 1")
    @Test
    public void jieYinHealsSelfAndMostWounded() {
        Game game = createGame(General.孫尚香, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        Player c = game.getPlayer("player-c");
        a.getBloodCard().setHp(2);
        c.getBloodCard().setHp(1); // 傷最重
        game.getPlayer("player-b").getBloodCard().setHp(3);
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029)));

        game.playerUseSkillEffect("player-a", "結姻", "ACCEPT",
                List.of(BS8008.getCardId(), BH3029.getCardId()), null);

        assertEquals(3, a.getHP());
        assertEquals(2, c.getHP(), "傷最重的 c 回 1");
        assertEquals(3, game.getPlayer("player-b").getHP(), "b 不變");
        assertEquals(0, a.getHandSize());

        // 每回合限一次
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS9009), new Peach(BH4030)));
        assertThrows(IllegalStateException.class, () ->
                game.playerUseSkillEffect("player-a", "結姻", "ACCEPT",
                        List.of(BS9009.getCardId(), BH4030.getCardId()), null));
    }

    // ===== 仁德 =====

    @DisplayName("劉備仁德：分兩次共給 2 張 → 回 1 體力（只回一次）")
    @Test
    public void renDeHealsAfterGivingTwoCards() {
        Game game = createGame(General.劉備, General.孫權, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getBloodCard().setHp(2);
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030)));

        game.playerUseSkillEffect("player-a", "仁德", "ACCEPT", List.of(BS8008.getCardId()), "player-b");
        assertEquals(2, a.getHP(), "給 1 張未達門檻");

        game.playerUseSkillEffect("player-a", "仁德", "ACCEPT", List.of(BH3029.getCardId()), "player-c");
        assertEquals(3, a.getHP(), "累積 2 張回 1");

        game.playerUseSkillEffect("player-a", "仁德", "ACCEPT", List.of(BH4030.getCardId()), "player-b");
        assertEquals(3, a.getHP(), "本回合不再回血");

        assertEquals(2, b.getHandSize());
        assertEquals(1, game.getPlayer("player-c").getHandSize());
    }

    // ===== 觀星 =====

    @DisplayName("諸葛亮觀星：觀看 4 張（存活數），重排 2 張回頂、2 張置底")
    @Test
    public void guanXingRearrangesDeckTop() {
        Game game = createGame(General.諸葛亮, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        // 控制堆頂 4 張（後 add 先抽）：抽序 = BS8008, BH2028, BS9009, BH3029
        game.getDeck().add(List.of(new Peach(BH3029), new Kill(BS9009), new Dodge(BH2028), new Kill(BS8008)));

        List<DomainEvent> events = game.playerUseSkillEffect("player-a", "觀星", "ACCEPT", null, null);
        AskSkillEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(e -> (AskSkillEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals(List.of(BS8008.getCardId(), BH2028.getCardId(), BS9009.getCardId(), BH3029.getCardId()),
                ask.getDataCardIds());

        // 重排：BH3029, BS9009 回堆頂；BS8008, BH2028 置底
        game.playerUseSkillEffect("player-a", "觀星", "ARRANGE",
                List.of(BH3029.getCardId(), BS9009.getCardId()), null);

        List<HandCard> next = game.getDeck().deal(2);
        assertEquals(BH3029.getCardId(), next.get(0).getId(), "重排後第一張");
        assertEquals(BS9009.getCardId(), next.get(1).getId(), "重排後第二張");
        assertTrue(game.isTopBehaviorEmpty());
    }
}

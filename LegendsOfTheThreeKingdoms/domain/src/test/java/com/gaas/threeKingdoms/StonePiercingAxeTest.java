package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.StonePiercingAxeCard;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class StonePiercingAxeTest {

    @DisplayName("A 裝備貫石斧 → 攻擊範圍 3")
    @Test
    public void testEquipStonePiercingAxe_AttackRangeIs3() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        playerA.getHand().addCardToHand(new StonePiercingAxeCard(ED5083));

        game.playerPlayCard(playerA.getId(), ED5083.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        assertInstanceOf(StonePiercingAxeCard.class, playerA.getEquipmentWeaponCard());
        assertEquals(3, ((StonePiercingAxeCard) playerA.getEquipmentWeaponCard()).getWeaponDistance());
    }

    @DisplayName("A 出殺 → B 出閃 → A 收到 AskStonePiercingAxeEffectEvent")
    @Test
    public void testDodgeTriggersAskEvent() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        AskStonePiercingAxeEffectEvent askEvent = events.stream()
                .filter(e -> e instanceof AskStonePiercingAxeEffectEvent)
                .map(e -> (AskStonePiercingAxeEffectEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("AskStonePiercingAxeEffectEvent should be emitted"));
        assertEquals("player-a", askEvent.getAttackerPlayerId());
        assertEquals("player-b", askEvent.getTargetPlayerId());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("A 選 SKIP 不發動 → 殺被抵銷，behavior pop")
    @Test
    public void testSkipStonePiercingAxeEffect_KillIsCancelled() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));
        int playerBHpBefore = playerB.getHP();

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        game.playerUseStonePiercingAxeEffect(playerA.getId(),
                AskStonePiercingAxeEffectEvent.Choice.SKIP, List.of());

        assertEquals(playerBHpBefore, playerB.getHP());
        assertEquals(0, game.getTopBehavior().size());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("A 選 DISCARD_TWO (兩張手牌) → B 強制扣血 + StonePiercingAxeTriggerEvent")
    @Test
    public void testDiscardTwoHandCards_ForceHit() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));
        int playerBHpBefore = playerB.getHP();

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        List<DomainEvent> events = game.playerUseStonePiercingAxeEffect(playerA.getId(),
                AskStonePiercingAxeEffectEvent.Choice.DISCARD_TWO,
                List.of(BH3029.getCardId(), BH4030.getCardId()));

        // B 強制扣血
        assertEquals(playerBHpBefore - 1, playerB.getHP());
        // A 手牌剩 0 張（原 3 張 - 1 殺 - 2 棄牌）
        assertEquals(0, playerA.getHandSize());
        // 有 StonePiercingAxeTriggerEvent
        StonePiercingAxeTriggerEvent triggerEvent = events.stream()
                .filter(e -> e instanceof StonePiercingAxeTriggerEvent)
                .map(e -> (StonePiercingAxeTriggerEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("StonePiercingAxeTriggerEvent should be emitted"));
        assertEquals("player-a", triggerEvent.getAttackerPlayerId());
        assertEquals("player-b", triggerEvent.getTargetPlayerId());
        assertEquals(List.of(BH3029.getCardId(), BH4030.getCardId()), triggerEvent.getDiscardedCardIds());
        // behavior stack 清空
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("A 選 DISCARD_TWO (一手牌 + 一裝備) → B 強制扣血，A 裝備被移除")
    @Test
    public void testDiscardOneHandOneEquipment_ForceHit() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        playerA.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));
        int playerBHpBefore = playerB.getHP();

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        game.playerUseStonePiercingAxeEffect(playerA.getId(),
                AskStonePiercingAxeEffectEvent.Choice.DISCARD_TWO,
                List.of(BH3029.getCardId(), EH5044.getCardId()));

        assertEquals(playerBHpBefore - 1, playerB.getHP());
        assertEquals(0, playerA.getHandSize());
        // 赤兔馬被移除
        assertNull(playerA.getEquipment().getMinusOne());
        // 貫石斧還在
        assertNotNull(playerA.getEquipmentWeaponCard());
    }

    @DisplayName("A 選 DISCARD_TWO 但 cardIds 數量不是 2 → 拋例外")
    @Test
    public void testDiscardTwoWithWrongNumberOfCards_ThrowsException() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // 只傳 1 張
        assertThrows(RuntimeException.class, () ->
                game.playerUseStonePiercingAxeEffect(playerA.getId(),
                        AskStonePiercingAxeEffectEvent.Choice.DISCARD_TWO,
                        List.of(BH3029.getCardId())));
    }

    @DisplayName("A 選 DISCARD_TWO 但 cardId 不屬於 A → 拋例外")
    @Test
    public void testDiscardTwoWithCardNotOwned_ThrowsException() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // BS9009 不存在於 A 手牌或裝備
        assertThrows(RuntimeException.class, () ->
                game.playerUseStonePiercingAxeEffect(playerA.getId(),
                        AskStonePiercingAxeEffectEvent.Choice.DISCARD_TWO,
                        List.of(BH3029.getCardId(), BS9009.getCardId())));
    }

    @DisplayName("沒裝備貫石斧 → 不觸發（對照組）")
    @Test
    public void testNoStonePiercingAxe_NormalFlow() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));
        int playerBHpBefore = playerB.getHP();

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        assertFalse(events.stream().anyMatch(e -> e instanceof AskStonePiercingAxeEffectEvent));
        assertEquals(playerBHpBefore, playerB.getHP());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("A 可棄牌數不足 2 → 殺被閃直接結束，不觸發")
    @Test
    public void testNotEnoughCardsToDiscard_NotTriggered() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        // A 只有 1 張殺（除了裝備），沒其他牌
        playerA.getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        playerA.getHand().addCardToHand(new Kill(BS8008));
        playerB.getHand().addCardToHand(new Dodge(BH2028));
        int playerBHpBefore = playerB.getHP();

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        // 出完殺後 A 手牌 0 張，裝備 1 張（貫石斧），可棄牌總數 1，不足 2
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // 不應觸發貫石斧
        assertFalse(events.stream().anyMatch(e -> e instanceof AskStonePiercingAxeEffectEvent));
        assertEquals(playerBHpBefore, playerB.getHP());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("錯誤玩家呼叫 API → 拋 IllegalStateException")
    @Test
    public void testWrongPlayerCallsAPI_ThrowsException() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        assertThrows(IllegalStateException.class, () ->
                game.playerUseStonePiercingAxeEffect(playerB.getId(),
                        AskStonePiercingAxeEffectEvent.Choice.SKIP, List.of()));
    }

    @DisplayName("沒有 WaitingStonePiercingAxeResponseBehavior 時呼叫 → 拋例外")
    @Test
    public void testNoWaitingBehavior_ThrowsException() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");

        assertThrows(IllegalStateException.class, () ->
                game.playerUseStonePiercingAxeEffect(playerA.getId(),
                        AskStonePiercingAxeEffectEvent.Choice.SKIP, List.of()));
    }

    @DisplayName("B HP=1，貫石斧強制命中 → B 瀕死（AskPeachEvent + DyingAskPeachBehavior on stack）")
    @Test
    public void testStonePiercingAxe_KillsTargetToDying() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        // B HP = 1
        playerB.setBloodCard(new BloodCard(1));

        playerA.getEquipment().setWeapon(new StonePiercingAxeCard(ED5083));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        List<DomainEvent> events = game.playerUseStonePiercingAxeEffect(playerA.getId(),
                AskStonePiercingAxeEffectEvent.Choice.DISCARD_TWO,
                List.of(BH3029.getCardId(), BH4030.getCardId()));

        assertTrue(events.stream().anyMatch(e -> e instanceof AskPeachEvent));
        assertEquals(0, playerB.getHP());
        // 瀕死 behavior 在 stack 上
        assertFalse(game.getTopBehavior().isEmpty(), "DyingAskPeachBehavior should be on the stack");
    }

    // Helper
    private Game createGameWithPlayerAB() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .build();
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withEquipment(new Equipment())
                .build();
        game.setPlayers(asList(playerA, playerB, playerC, playerD));
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));
        return game;
    }
}

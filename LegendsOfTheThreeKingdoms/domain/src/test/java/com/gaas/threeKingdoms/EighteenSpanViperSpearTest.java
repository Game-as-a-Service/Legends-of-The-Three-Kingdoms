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
import com.gaas.threeKingdoms.handcard.basiccard.VirtualKill;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.EighteenSpanViperSpearCard;
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

public class EighteenSpanViperSpearTest {

    @DisplayName("A 裝備丈八蛇矛 → 攻擊範圍 3")
    @Test
    public void testEquipViperSpear_AttackRangeIs3() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        playerA.getHand().addCardToHand(new EighteenSpanViperSpearCard(ESQ025));

        game.playerPlayCard(playerA.getId(), ESQ025.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        assertInstanceOf(EighteenSpanViperSpearCard.class, playerA.getEquipmentWeaponCard());
        assertEquals(3, ((EighteenSpanViperSpearCard) playerA.getEquipmentWeaponCard()).getWeaponDistance());
    }

    @DisplayName("A 呼叫 useViperSpearKill 棄兩張牌 → ViperSpearKillTriggerEvent + B 收到 AskDodgeEvent")
    @Test
    public void testUseViperSpearKill_TwoHandCards_TriggersAskDodge() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Dodge(BH2028)));
        int playerAHandSizeBefore = playerA.getHandSize();

        List<DomainEvent> events = game.playerUseViperSpearKill(
                playerA.getId(), playerB.getId(),
                List.of(BH3029.getCardId(), BH2028.getCardId()));

        ViperSpearKillTriggerEvent triggerEvent = events.stream()
                .filter(e -> e instanceof ViperSpearKillTriggerEvent)
                .map(e -> (ViperSpearKillTriggerEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("ViperSpearKillTriggerEvent should be emitted"));
        assertEquals("player-a", triggerEvent.getAttackerPlayerId());
        assertEquals("player-b", triggerEvent.getTargetPlayerId());
        assertEquals(List.of(BH3029.getCardId(), BH2028.getCardId()), triggerEvent.getDiscardedCardIds());

        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
        // A 手牌 -2
        assertEquals(playerAHandSizeBefore - 2, playerA.getHandSize());
    }

    @DisplayName("B 不出閃 → B 扣血")
    @Test
    public void testViperSpearKill_TargetSkipsDodge_TakesDamage() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Dodge(BH2028)));
        int playerBHpBefore = playerB.getHP();

        game.playerUseViperSpearKill(playerA.getId(), playerB.getId(),
                List.of(BH3029.getCardId(), BH2028.getCardId()));

        // B 不出閃
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        assertEquals(playerBHpBefore - 1, playerB.getHP());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("B 出閃 → 殺被抵銷")
    @Test
    public void testViperSpearKill_TargetPlaysDodge_NoDamage() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Kill(BS8008)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));
        int playerBHpBefore = playerB.getHP();

        game.playerUseViperSpearKill(playerA.getId(), playerB.getId(),
                List.of(BH3029.getCardId(), BS8008.getCardId()));

        // B 出閃
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        assertEquals(playerBHpBefore, playerB.getHP());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("B 有八卦陣 → 先問八卦陣")
    @Test
    public void testViperSpearKill_TargetHasEightDiagram_AskEquipmentEffectFirst() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Dodge(BH2028)));
        playerB.getEquipment().setArmor(new EightDiagramTactic(ES2015));

        List<DomainEvent> events = game.playerUseViperSpearKill(
                playerA.getId(), playerB.getId(),
                List.of(BH3029.getCardId(), BH2028.getCardId()));

        assertTrue(events.stream().anyMatch(e -> e instanceof AskPlayEquipmentEffectEvent));
        assertFalse(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
    }

    @DisplayName("A 沒裝備丈八蛇矛 → 拋例外")
    @Test
    public void testNoViperSpear_ThrowsException() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Dodge(BH2028)));

        assertThrows(IllegalStateException.class, () ->
                game.playerUseViperSpearKill(playerA.getId(), playerB.getId(),
                        List.of(BH3029.getCardId(), BH2028.getCardId())));
    }

    @DisplayName("discardCardIds 數量不是 2 → 拋例外")
    @Test
    public void testDiscardNotTwoCards_ThrowsException() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Dodge(BH2028), new Kill(BS8008)));

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseViperSpearKill(playerA.getId(), playerB.getId(),
                        List.of(BH3029.getCardId())));

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseViperSpearKill(playerA.getId(), playerB.getId(),
                        List.of(BH3029.getCardId(), BH2028.getCardId(), BS8008.getCardId())));
    }

    @DisplayName("discardCardIds 不在 A 手牌 → 拋例外")
    @Test
    public void testDiscardCardNotInHand_ThrowsException() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Dodge(BH2028)));

        // BS8008 不在 A 手牌
        assertThrows(RuntimeException.class, () ->
                game.playerUseViperSpearKill(playerA.getId(), playerB.getId(),
                        List.of(BH3029.getCardId(), BS8008.getCardId())));
    }

    @DisplayName("重複傳同一張 cardId → 拋例外")
    @Test
    public void testDuplicateCardIds_ThrowsException() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Dodge(BH2028)));

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseViperSpearKill(playerA.getId(), playerB.getId(),
                        List.of(BH3029.getCardId(), BH3029.getCardId())));
    }

    @DisplayName("A 本回合已出過殺（無諸葛連弩）→ 拋例外")
    @Test
    public void testAlreadyPlayedKillThisRound_ThrowsException() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)));

        // 先出一張普通殺
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // 再嘗試用丈八蛇矛出殺 → 應拋例外
        assertThrows(IllegalStateException.class, () ->
                game.playerUseViperSpearKill(playerA.getId(), playerB.getId(),
                        List.of(BH3029.getCardId(), BH2028.getCardId())));
    }

    @DisplayName("B HP=1 → 瀕死流程")
    @Test
    public void testViperSpearKill_KillsTargetToDying() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        playerB.setBloodCard(new BloodCard(1));

        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Dodge(BH2028)));

        game.playerUseViperSpearKill(playerA.getId(), playerB.getId(),
                List.of(BH3029.getCardId(), BH2028.getCardId()));

        // B 不出閃
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        assertTrue(events.stream().anyMatch(e -> e instanceof AskPeachEvent));
        assertEquals(0, playerB.getHP());
        // DyingAskPeachBehavior on stack
        assertFalse(game.getTopBehavior().isEmpty());
    }

    @DisplayName("VirtualKill 有正確的 id 和 effect")
    @Test
    public void testVirtualKill_HasCorrectIdAndEffect() {
        VirtualKill virtualKill = new VirtualKill();
        assertEquals(VirtualKill.VIRTUAL_CARD_ID, virtualKill.getId());
        assertEquals(VirtualKill.VIRTUAL_CARD_NAME, virtualKill.getName());

        // effect 應扣 1 滴血
        Player player = PlayerBuilder.construct()
                .withId("p")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();
        virtualKill.effect(player);
        assertEquals(3, player.getHP());
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

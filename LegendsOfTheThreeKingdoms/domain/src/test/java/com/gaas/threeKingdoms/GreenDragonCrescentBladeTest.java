package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.behavior.behavior.WaitingGreenDragonCrescentBladeResponseBehavior;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.GreenDragonCrescentBladeCard;
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

public class GreenDragonCrescentBladeTest {

    @DisplayName("A 裝備青龍偃月刀 → 攻擊範圍 3")
    @Test
    public void testEquipGreenDragonCrescentBlade_AttackRangeIs3() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        playerA.getHand().addCardToHand(new GreenDragonCrescentBladeCard(ES5005));

        game.playerPlayCard(playerA.getId(), ES5005.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        assertInstanceOf(GreenDragonCrescentBladeCard.class, playerA.getEquipmentWeaponCard());
        assertEquals(3, ((GreenDragonCrescentBladeCard) playerA.getEquipmentWeaponCard()).getWeaponDistance());
    }

    @DisplayName("A 出殺 → B 出閃 → A 收到 AskGreenDragonCrescentBladeEffectEvent")
    @Test
    public void testDodgeTriggersAskEvent() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));

        // A 出殺
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // B 出閃
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // A 應收到 AskGreenDragonCrescentBladeEffectEvent
        AskGreenDragonCrescentBladeEffectEvent askEvent = events.stream()
                .filter(e -> e instanceof AskGreenDragonCrescentBladeEffectEvent)
                .map(e -> (AskGreenDragonCrescentBladeEffectEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("AskGreenDragonCrescentBladeEffectEvent should be emitted"));
        assertEquals("player-a", askEvent.getAttackerPlayerId());
        assertEquals("player-b", askEvent.getTargetPlayerId());

        // activePlayer 應為 A（等待選擇）
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("A 選 SKIP 不發動 → 殺被抵銷，behavior pop")
    @Test
    public void testSkipGreenDragonCrescentBladeEffect_KillIsCancelled() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));
        int playerBHpBefore = playerB.getHP();

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // A 選 SKIP
        game.playerUseGreenDragonCrescentBladeEffect(playerA.getId(),
                AskGreenDragonCrescentBladeEffectEvent.Choice.SKIP, "");

        // B HP 不變，behavior stack 為空
        assertEquals(playerBHpBefore, playerB.getHP());
        assertEquals(0, game.getTopBehavior().size());
        // activePlayer 回 A（currentRoundPlayer）
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("A 選 KILL 再出殺 → GreenDragonCrescentBladeTriggerEvent + B 收到 AskDodgeEvent")
    @Test
    public void testKillGreenDragonCrescentBladeEffect_TriggerAnotherKill() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // A 選 KILL，再出 BS9009
        List<DomainEvent> events = game.playerUseGreenDragonCrescentBladeEffect(
                playerA.getId(),
                AskGreenDragonCrescentBladeEffectEvent.Choice.KILL,
                BS9009.getCardId());

        // 應有 GreenDragonCrescentBladeTriggerEvent
        GreenDragonCrescentBladeTriggerEvent triggerEvent = events.stream()
                .filter(e -> e instanceof GreenDragonCrescentBladeTriggerEvent)
                .map(e -> (GreenDragonCrescentBladeTriggerEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("GreenDragonCrescentBladeTriggerEvent should be emitted"));
        assertEquals("player-a", triggerEvent.getAttackerPlayerId());
        assertEquals("player-b", triggerEvent.getTargetPlayerId());
        assertEquals(BS9009.getCardId(), triggerEvent.getKillCardId());

        // B 應收到 AskDodgeEvent（被第二張殺瞄準）
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));

        // A 的第二張殺應進入墓地
        assertEquals(0, playerA.getHandSize());
        // activePlayer 應為 B（等待出閃）
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("A 選 KILL 再出殺 → B 不出閃 → B 扣血")
    @Test
    public void testGreenDragonCrescentBladeTrigger_TargetSkipsDodge_TakesDamage() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));
        int playerBHpBefore = playerB.getHP();

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());
        game.playerUseGreenDragonCrescentBladeEffect(playerA.getId(),
                AskGreenDragonCrescentBladeEffectEvent.Choice.KILL, BS9009.getCardId());

        // B 不出閃
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        assertEquals(playerBHpBefore - 1, playerB.getHP());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("多次循環：A 殺 → B 閃 → 發動 → A 殺 → B 閃 → 再發動 → A 殺 → 命中")
    @Test
    public void testGreenDragonCrescentBladeMultipleLoops() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009), new Kill(BS0010)));
        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BH2028), new Dodge(BH2041)));
        int playerBHpBefore = playerB.getHP();

        // 第一次殺 → 閃
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // 發動 → 第二次殺 → 閃
        game.playerUseGreenDragonCrescentBladeEffect(playerA.getId(),
                AskGreenDragonCrescentBladeEffectEvent.Choice.KILL, BS9009.getCardId());
        game.playerPlayCard(playerB.getId(), BH2041.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // 再發動 → 第三次殺 → B 不出閃
        game.playerUseGreenDragonCrescentBladeEffect(playerA.getId(),
                AskGreenDragonCrescentBladeEffectEvent.Choice.KILL, BS0010.getCardId());
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // 最終 B 扣一滴血
        assertEquals(playerBHpBefore - 1, playerB.getHP());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("A 選 KILL 但沒殺可出 → 拋例外")
    @Test
    public void testKillChoiceWithoutKillCard_ThrowsException() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));
        playerA.getHand().addCardToHand(new Kill(BS8008));
        playerB.getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // A 手牌已空，選 KILL 但 cardId 不存在
        assertThrows(RuntimeException.class, () ->
                game.playerUseGreenDragonCrescentBladeEffect(playerA.getId(),
                        AskGreenDragonCrescentBladeEffectEvent.Choice.KILL, BS9009.getCardId()));
    }

    @DisplayName("A 選 KILL 但提供非殺 cardId → 拋例外")
    @Test
    public void testKillChoiceWithNonKillCard_ThrowsException() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        assertThrows(RuntimeException.class, () ->
                game.playerUseGreenDragonCrescentBladeEffect(playerA.getId(),
                        AskGreenDragonCrescentBladeEffectEvent.Choice.KILL, BH3029.getCardId()));
    }

    @DisplayName("沒有裝備青龍偃月刀 → B 出閃後流程正常結束（對照組）")
    @Test
    public void testNoGreenDragonCrescentBlade_NormalFlow() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));
        int playerBHpBefore = playerB.getHP();

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // 不應有 AskGreenDragonCrescentBladeEffectEvent
        assertFalse(events.stream().anyMatch(e -> e instanceof AskGreenDragonCrescentBladeEffectEvent));
        assertEquals(playerBHpBefore, playerB.getHP());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("錯誤玩家呼叫 API → 拋 IllegalStateException")
    @Test
    public void testWrongPlayerCallsAPI_ThrowsException() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        playerA.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // B 錯誤呼叫 API
        assertThrows(IllegalStateException.class, () ->
                game.playerUseGreenDragonCrescentBladeEffect(playerB.getId(),
                        AskGreenDragonCrescentBladeEffectEvent.Choice.SKIP, ""));
    }

    @DisplayName("沒有 WaitingGreenDragonCrescentBladeResponseBehavior 時呼叫 → 拋例外")
    @Test
    public void testNoWaitingBehavior_ThrowsException() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");

        assertThrows(IllegalStateException.class, () ->
                game.playerUseGreenDragonCrescentBladeEffect(playerA.getId(),
                        AskGreenDragonCrescentBladeEffectEvent.Choice.SKIP, ""));
    }

    @DisplayName("B HP=1，青龍偃月刀追殺命中 → B 瀕死")
    @Test
    public void testGreenDragonCrescentBlade_KillsTargetToDying() {
        Game game = createGameWithPlayerAB();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        // B HP = 1
        playerB.setBloodCard(new BloodCard(1));

        playerA.getEquipment().setWeapon(new GreenDragonCrescentBladeCard(ES5005));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009)));
        playerB.getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), BH2028.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());
        game.playerUseGreenDragonCrescentBladeEffect(playerA.getId(),
                AskGreenDragonCrescentBladeEffectEvent.Choice.KILL, BS9009.getCardId());
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // B 進入瀕死（AskPeachEvent）
        assertTrue(events.stream().anyMatch(e -> e instanceof AskPeachEvent));
        assertEquals(0, playerB.getHP());
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

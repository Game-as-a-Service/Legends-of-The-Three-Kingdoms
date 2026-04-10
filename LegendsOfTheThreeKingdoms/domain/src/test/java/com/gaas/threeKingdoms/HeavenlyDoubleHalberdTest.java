package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.behavior.behavior.HeavenlyDoubleHalberdKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.HeavenlyDoubleHalberdCard;
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

public class HeavenlyDoubleHalberdTest {

    @DisplayName("A 裝備方天畫戟 → 攻擊範圍 4")
    @Test
    public void testEquipHalberd_AttackRangeIs4() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        playerA.getHand().addCardToHand(new HeavenlyDoubleHalberdCard(EDQ103));

        game.playerPlayCard(playerA.getId(), EDQ103.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        assertInstanceOf(HeavenlyDoubleHalberdCard.class, playerA.getEquipmentWeaponCard());
        assertEquals(4, ((HeavenlyDoubleHalberdCard) playerA.getEquipmentWeaponCard()).getWeaponDistance());
    }

    @DisplayName("A 用方天畫戟出殺 + 2 個額外目標 → 廣播 trigger event 並問 B 出閃")
    @Test
    public void testUseHalberdKill_TwoAdditionalTargets_EmitsTriggerEventAndAsksFirstTarget() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        equipHalberdWithSingleKill(playerA);

        List<DomainEvent> events = game.playerUseHeavenlyDoubleHalberdKill(
                "player-a", BS8008.getCardId(), "player-b", List.of("player-c", "player-d"));

        HeavenlyDoubleHalberdKillTriggerEvent trigger = events.stream()
                .filter(e -> e instanceof HeavenlyDoubleHalberdKillTriggerEvent)
                .map(e -> (HeavenlyDoubleHalberdKillTriggerEvent) e)
                .findFirst().orElseThrow(() -> new AssertionError("trigger event missing"));
        assertEquals("player-a", trigger.getAttackerPlayerId());
        assertEquals(BS8008.getCardId(), trigger.getCardId());
        assertEquals(List.of("player-b", "player-c", "player-d"), trigger.getTargetPlayerIds());

        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent
                && ((AskDodgeEvent) e).getPlayerId().equals("player-b")));
        assertEquals(0, playerA.getHandSize());
    }

    @DisplayName("A 用方天畫戟出殺 + 1 個額外目標 → 問 B 出閃（只有 2 個目標）")
    @Test
    public void testUseHalberdKill_OneAdditionalTarget_EmitsAsksFirstTarget() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        equipHalberdWithSingleKill(playerA);

        List<DomainEvent> events = game.playerUseHeavenlyDoubleHalberdKill(
                "player-a", BS8008.getCardId(), "player-b", List.of("player-c"));

        HeavenlyDoubleHalberdKillTriggerEvent trigger = events.stream()
                .filter(e -> e instanceof HeavenlyDoubleHalberdKillTriggerEvent)
                .map(e -> (HeavenlyDoubleHalberdKillTriggerEvent) e)
                .findFirst().orElseThrow();
        assertEquals(List.of("player-b", "player-c"), trigger.getTargetPlayerIds());
    }

    @DisplayName("A 用方天畫戟出殺但 0 個額外目標 → 短路走一般 playCard 流程，不產生 halberd 事件")
    @Test
    public void testUseHalberdKill_ZeroAdditionalTargets_ShortCircuits() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        equipHalberdWithSingleKill(playerA);

        List<DomainEvent> events = game.playerUseHeavenlyDoubleHalberdKill(
                "player-a", BS8008.getCardId(), "player-b", List.of());

        // 走 NormalActiveKillBehavior 路徑
        assertFalse(game.getTopBehavior().isEmpty());
        assertInstanceOf(NormalActiveKillBehavior.class, game.getTopBehavior().peek());
        assertFalse(game.getTopBehavior().peek() instanceof HeavenlyDoubleHalberdKillBehavior);
        // 不會發 halberd trigger event
        assertFalse(events.stream().anyMatch(e -> e instanceof HeavenlyDoubleHalberdKillTriggerEvent));
        // 會有一般出牌流程的 AskDodgeEvent
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
    }

    @DisplayName("全部目標都不出閃 → 每個都扣 1 血")
    @Test
    public void testUseHalberdKill_AllTargetsSkipDodge_AllTakeDamage() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        Player playerC = game.getPlayer("player-c");
        Player playerD = game.getPlayer("player-d");
        equipHalberdWithSingleKill(playerA);

        game.playerUseHeavenlyDoubleHalberdKill(
                "player-a", BS8008.getCardId(), "player-b", List.of("player-c", "player-d"));

        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());

        assertEquals(3, playerB.getHP());
        assertEquals(3, playerC.getHP());
        assertEquals(3, playerD.getHP());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("全部目標都出閃 → 沒有人扣血")
    @Test
    public void testUseHalberdKill_AllTargetsPlayDodge_NoDamage() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        Player playerC = game.getPlayer("player-c");
        Player playerD = game.getPlayer("player-d");
        equipHalberdWithSingleKill(playerA);
        playerB.getHand().addCardToHand(new Dodge(BH2028));
        playerC.getHand().addCardToHand(new Dodge(BH2041));
        playerD.getHand().addCardToHand(new Dodge(BD2080));

        game.playerUseHeavenlyDoubleHalberdKill(
                "player-a", BS8008.getCardId(), "player-b", List.of("player-c", "player-d"));

        game.playerPlayCard("player-b", BH2028.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-c", BH2041.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-d", BD2080.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());

        assertEquals(4, playerB.getHP());
        assertEquals(4, playerC.getHP());
        assertEquals(4, playerD.getHP());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("B 出閃、C 不出閃、D 出閃 → 只有 C 扣血")
    @Test
    public void testUseHalberdKill_MixedResponses_OnlySkipperTakesDamage() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        Player playerC = game.getPlayer("player-c");
        Player playerD = game.getPlayer("player-d");
        equipHalberdWithSingleKill(playerA);
        playerB.getHand().addCardToHand(new Dodge(BH2028));
        playerD.getHand().addCardToHand(new Dodge(BD2080));

        game.playerUseHeavenlyDoubleHalberdKill(
                "player-a", BS8008.getCardId(), "player-b", List.of("player-c", "player-d"));

        game.playerPlayCard("player-b", BH2028.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-d", BD2080.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());

        assertEquals(4, playerB.getHP());
        assertEquals(3, playerC.getHP());
        assertEquals(4, playerD.getHP());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("第一個目標有八卦陣 → 先發 AskPlayEquipmentEffectEvent")
    @Test
    public void testUseHalberdKill_FirstTargetHasEightDiagram_AsksEquipmentEffectFirst() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        equipHalberdWithSingleKill(playerA);
        playerB.getEquipment().setArmor(new EightDiagramTactic(ES2015));

        List<DomainEvent> events = game.playerUseHeavenlyDoubleHalberdKill(
                "player-a", BS8008.getCardId(), "player-b", List.of("player-c"));

        assertTrue(events.stream().anyMatch(e -> e instanceof AskPlayEquipmentEffectEvent));
        assertFalse(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
    }

    @DisplayName("C HP=1 且不出閃 → C 進入瀕死流程，D 仍被詢問")
    @Test
    public void testUseHalberdKill_MiddleTargetDying_DFlowStillResumes() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        Player playerC = game.getPlayer("player-c");
        playerC.setBloodCard(new BloodCard(1));
        equipHalberdWithSingleKill(playerA);
        Player playerB = game.getPlayer("player-b");
        playerB.getHand().addCardToHand(new Dodge(BH2028));

        game.playerUseHeavenlyDoubleHalberdKill(
                "player-a", BS8008.getCardId(), "player-b", List.of("player-c", "player-d"));

        // B 出閃
        game.playerPlayCard("player-b", BH2028.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        // C 不出閃 → 扣血至 0 → 進入瀕死
        List<DomainEvent> dyingEvents = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());

        assertTrue(dyingEvents.stream().anyMatch(e -> e instanceof AskPeachEvent));
        assertEquals(0, playerC.getHP());

        // 所有人跳過救桃 → C 死亡 (peach-ask 順序從 dying player 開始: C → D → E → A → B)
        game.playerPlayCard("player-c", "", "player-c", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-d", "", "player-c", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-e", "", "player-c", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-a", "", "player-c", PlayType.SKIP.getPlayType());
        List<DomainEvent> resumeEvents =
                game.playerPlayCard("player-b", "", "player-c", PlayType.SKIP.getPlayType());

        // 應該恢復詢問 D 出閃
        assertTrue(resumeEvents.stream().anyMatch(e -> e instanceof AskDodgeEvent
                && ((AskDodgeEvent) e).getPlayerId().equals("player-d")));
    }

    @DisplayName("最後一個目標 D HP=1 且死亡 → behavior 正確結束，不發 spurious AskDodge")
    @Test
    public void testUseHalberdKill_LastTargetDying_BehaviorTerminatesCleanly() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        Player playerD = game.getPlayer("player-d");
        playerD.setBloodCard(new BloodCard(1));
        equipHalberdWithSingleKill(playerA);

        game.playerUseHeavenlyDoubleHalberdKill(
                "player-a", BS8008.getCardId(), "player-b", List.of("player-d"));

        // B 不出閃
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        // D 不出閃 → HP=0 → 瀕死（D 是最後一個目標）
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());

        assertEquals(0, playerD.getHP());

        // 所有人跳過桃 → D 永久死亡 (peach-ask: D → E → A → B → C)
        game.playerPlayCard("player-d", "", "player-d", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-e", "", "player-d", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-a", "", "player-d", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-b", "", "player-d", PlayType.SKIP.getPlayType());
        List<DomainEvent> finalEvents =
                game.playerPlayCard("player-c", "", "player-d", PlayType.SKIP.getPlayType());

        // behavior stack 應為空（halberd 已結束）
        assertEquals(0, game.getTopBehavior().size());
        // 不應有 spurious AskDodgeEvent
        assertFalse(finalEvents.stream().anyMatch(e -> e instanceof AskDodgeEvent),
                "Should not emit AskDodgeEvent after last target dies");
    }

    @DisplayName("A 沒裝備方天畫戟 → 拋例外")
    @Test
    public void testNoHalberd_ThrowsException() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        playerA.getHand().addCardToHand(new Kill(BS8008));

        assertThrows(IllegalStateException.class, () ->
                game.playerUseHeavenlyDoubleHalberdKill(
                        "player-a", BS8008.getCardId(), "player-b", List.of("player-c")));
    }

    @DisplayName("A 的殺不是最後一張手牌 → 拋例外")
    @Test
    public void testNotLastHandCard_ThrowsException() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        playerA.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029)));

        assertThrows(IllegalStateException.class, () ->
                game.playerUseHeavenlyDoubleHalberdKill(
                        "player-a", BS8008.getCardId(), "player-b", List.of("player-c")));
    }

    @DisplayName("additionalTargets 超過 2 → 拋例外")
    @Test
    public void testMoreThanTwoAdditionalTargets_ThrowsException() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        equipHalberdWithSingleKill(playerA);

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseHeavenlyDoubleHalberdKill(
                        "player-a", BS8008.getCardId(), "player-b",
                        List.of("player-c", "player-d", "player-e")));
    }

    @DisplayName("additionalTargets 包含重複玩家 → 拋例外")
    @Test
    public void testDuplicateAdditionalTarget_ThrowsException() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        equipHalberdWithSingleKill(playerA);

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseHeavenlyDoubleHalberdKill(
                        "player-a", BS8008.getCardId(), "player-b",
                        List.of("player-b", "player-c")));
    }

    @DisplayName("additionalTargets 包含自己 → 拋例外")
    @Test
    public void testSelfAsAdditionalTarget_ThrowsException() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        equipHalberdWithSingleKill(playerA);

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseHeavenlyDoubleHalberdKill(
                        "player-a", BS8008.getCardId(), "player-b",
                        List.of("player-c", "player-a")));
    }

    @DisplayName("primaryTarget 是自己 → 拋例外")
    @Test
    public void testSelfAsPrimaryTarget_ThrowsException() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        equipHalberdWithSingleKill(playerA);

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseHeavenlyDoubleHalberdKill(
                        "player-a", BS8008.getCardId(), "player-a",
                        List.of("player-b", "player-c")));
    }

    @DisplayName("cardId 不在手牌 → 拋例外")
    @Test
    public void testCardNotInHand_ThrowsException() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        playerA.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        playerA.getHand().addCardToHand(new Kill(BS8008));

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseHeavenlyDoubleHalberdKill(
                        "player-a", BS9009.getCardId(), "player-b", List.of("player-c")));
    }

    @DisplayName("cardId 指向非殺 → 拋例外")
    @Test
    public void testCardNotKill_ThrowsException() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        playerA.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        playerA.getHand().addCardToHand(new Peach(BH3029));

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseHeavenlyDoubleHalberdKill(
                        "player-a", BH3029.getCardId(), "player-b", List.of("player-c")));
    }

    @DisplayName("本回合已出過殺 → 拋例外")
    @Test
    public void testAlreadyPlayedKillThisRound_ThrowsException() {
        Game game = createGame();
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        playerA.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009)));

        // 先出一張普通殺
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());

        // 再嘗試用方天畫戟出殺 → 應拋例外（此時只剩 BS9009 一張，滿足 last hand card）
        assertThrows(IllegalStateException.class, () ->
                game.playerUseHeavenlyDoubleHalberdKill(
                        "player-a", BS9009.getCardId(), "player-c", List.of("player-d")));
    }

    // -------- Helpers --------

    /** 給 A 裝備方天畫戟並只放一張殺在手中 */
    private void equipHalberdWithSingleKill(Player playerA) {
        playerA.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        playerA.getHand().addCardToHand(new Kill(BS8008));
    }

    /** 建立 5 人場 game，玩家 A (monarch)、B、C、D、E */
    private Game createGame() {
        Game game = new Game();
        game.initDeck();
        Player playerA = playerBuilder("player-a", Role.MONARCH);
        Player playerB = playerBuilder("player-b", Role.TRAITOR);
        Player playerC = playerBuilder("player-c", Role.MINISTER);
        Player playerD = playerBuilder("player-d", Role.REBEL);
        Player playerE = playerBuilder("player-e", Role.REBEL);
        game.setPlayers(asList(playerA, playerB, playerC, playerD, playerE));
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));
        return game;
    }

    private Player playerBuilder(String id, Role role) {
        return PlayerBuilder.construct()
                .withId(id)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(role))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();
    }
}

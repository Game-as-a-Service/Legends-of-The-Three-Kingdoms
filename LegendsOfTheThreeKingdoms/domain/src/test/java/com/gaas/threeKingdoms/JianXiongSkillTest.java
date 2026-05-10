package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.DyingAskPeachBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingJianXiongResponseBehavior;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskJianXiongEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.JianXiongEffectEvent;
import com.gaas.threeKingdoms.skill.context.DamageContext;
import com.gaas.threeKingdoms.skill.wei.JianXiongSkill;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.EighteenSpanViperSpearCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.behavior.behavior.LightningJudgementBehavior;
import com.gaas.threeKingdoms.behavior.behavior.BarbarianInvasionBehavior;
import com.gaas.threeKingdoms.behavior.behavior.ArrowBarrageBehavior;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
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

public class JianXiongSkillTest {

    private Game setupGameCaoCaoB(General playerBGeneral) {
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(playerBGeneral))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));
        return game;
    }

    @DisplayName("""
            Given
            B 玩家為曹操（WEI001），無閃，HP 4
            A 對 B 出殺

            When
            B 不出閃 → 受到 1 點傷害

            Then
            事件中含 AskJianXiongEffectEvent
            stack 頂端為 WaitingJianXiongResponseBehavior
            B 仍為 3 HP，殺仍在棄牌堆
            """)
    @Test
    public void givenCaoCaoTakeDamageFromKill_AskJianXiongEffectEmitted() {
        Game game = setupGameCaoCaoB(General.曹操);

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.playerPlayCard("player-b", "", "player-a", "skip");

        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
        assertTrue(events.stream().anyMatch(e -> e instanceof AskJianXiongEffectEvent),
                "expected AskJianXiongEffectEvent in events");
        AskJianXiongEffectEvent ask = (AskJianXiongEffectEvent) events.stream()
                .filter(e -> e instanceof AskJianXiongEffectEvent).findFirst().orElseThrow();
        assertEquals("player-b", ask.getPlayerId());
        assertEquals(List.of(BS8008.getCardId()), ask.getSourceCardIds());

        assertFalse(game.getTopBehavior().isEmpty());
        assertTrue(game.getTopBehavior().peek() instanceof WaitingJianXiongResponseBehavior);
        assertTrue(game.getGraveyard().contains(BS8008.getCardId()));
    }

    @DisplayName("""
            Given
            B 為曹操，受到殺造成的傷害 → AskJianXiongEffectEvent

            When
            B 選 ACCEPT

            Then
            殺從棄牌堆進入 B 手牌
            事件中含 JianXiongEffectEvent(taken=true)
            stack 為空
            """)
    @Test
    public void givenJianXiongAsk_AcceptChoice_KillReturnsToHand() {
        Game game = setupGameCaoCaoB(General.曹操);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");

        List<DomainEvent> events = game.playerUseJianXiongEffect("player-b", AskJianXiongEffectEvent.Choice.ACCEPT);

        assertFalse(game.getGraveyard().contains(BS8008.getCardId()));
        assertTrue(game.getPlayer("player-b").getHand().getCards().stream()
                .anyMatch(c -> c.getId().equals(BS8008.getCardId())));
        assertTrue(events.stream().anyMatch(e -> e instanceof JianXiongEffectEvent
                && ((JianXiongEffectEvent) e).isTaken()));
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("""
            Given
            B 為曹操，受到殺造成的傷害 → AskJianXiongEffectEvent

            When
            B 選 SKIP

            Then
            殺仍在棄牌堆，B 手牌不變
            事件中含 JianXiongEffectEvent(taken=false)
            stack 為空
            """)
    @Test
    public void givenJianXiongAsk_SkipChoice_NoChange() {
        Game game = setupGameCaoCaoB(General.曹操);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");

        int handSizeBefore = game.getPlayer("player-b").getHand().size();
        List<DomainEvent> events = game.playerUseJianXiongEffect("player-b", AskJianXiongEffectEvent.Choice.SKIP);

        assertTrue(game.getGraveyard().contains(BS8008.getCardId()));
        assertEquals(handSizeBefore, game.getPlayer("player-b").getHand().size());
        assertTrue(events.stream().anyMatch(e -> e instanceof JianXiongEffectEvent
                && !((JianXiongEffectEvent) e).isTaken()));
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("""
            Given
            B 為劉備（非曹操），受到殺造成的傷害

            Then
            不觸發奸雄 — 沒有 AskJianXiongEffectEvent
            stack 為空
            """)
    @Test
    public void givenNonCaoCao_NoJianXiongTrigger() {
        Game game = setupGameCaoCaoB(General.劉備);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.playerPlayCard("player-b", "", "player-a", "skip");

        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
        assertFalse(events.stream().anyMatch(e -> e instanceof AskJianXiongEffectEvent));
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("""
            Given
            B 為曹操、HP=1
            A 對 B 出殺

            When
            B 不出閃 → HP→0 進入 dying

            Then
            不觸發奸雄 ask（dying flow 跟奸雄不互鎖）
            stack 頂為 DyingAskPeachBehavior（dying flow 正常進）
            """)
    @Test
    public void givenCaoCaoDyingFromKill_NoJianXiongTrigger() {
        Game game = setupGameCaoCaoB(General.曹操);
        game.getPlayer("player-b").getBloodCard().setHp(1);

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.playerPlayCard("player-b", "", "player-a", "skip");

        assertEquals(0, game.getPlayer("player-b").getBloodCard().getHp());
        assertFalse(events.stream().anyMatch(e -> e instanceof AskJianXiongEffectEvent),
                "JianXiong should not trigger during dying");
        assertFalse(game.getTopBehavior().isEmpty(), "dying flow should keep behavior stack non-empty");
        assertTrue(game.getTopBehavior().peek() instanceof DyingAskPeachBehavior,
                "expected DyingAskPeachBehavior on top, JianXiong should not have inserted itself");
    }

    @DisplayName("""
            Given
            B 為曹操，受到殺造成的傷害 → AskJianXiongEffectEvent
            sourceCard 在 ACCEPT 之前被其他效果從 graveyard 移走

            When
            B 選 ACCEPT

            Then
            throws IllegalStateException（防禦既有 race condition）
            """)
    @Test
    public void givenSourceCardRemovedFromGraveyard_AcceptThrows() {
        Game game = setupGameCaoCaoB(General.曹操);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");

        // Setup precondition: source card 應該在 graveyard
        assertTrue(game.getGraveyard().removeCard(BS8008.getCardId()).isPresent(),
                "setup precondition: source card should be in graveyard before manual removal");

        assertThrows(IllegalStateException.class,
                () -> game.playerUseJianXiongEffect("player-b", AskJianXiongEffectEvent.Choice.ACCEPT));
    }

    @DisplayName("""
            Given
            B 為曹操，受到殺造成的傷害 → 觸發奸雄

            Then
            事件中只有 1 個 AskJianXiongEffectEvent（鎖定 v1 spec：一次 DamageEvent 只觸發 1 次）
            """)
    @Test
    public void givenSingleDamageEvent_OnlyOneAskJianXiongEffectEventEmitted() {
        Game game = setupGameCaoCaoB(General.曹操);
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.playerPlayCard("player-b", "", "player-a", "skip");

        long count = events.stream().filter(e -> e instanceof AskJianXiongEffectEvent).count();
        assertEquals(1, count, "JianXiong should trigger exactly once per DamageEvent");
    }

    @DisplayName("""
            Given
            B 為曹操，受到丈八蛇矛攻擊（A 棄兩張當虛擬殺）
            B 不出閃 → 受傷

            Then
            事件中含 AskJianXiongEffectEvent，sourceCardIds 為 A 棄的兩張牌
            （標準版 FAQ：丈八蛇矛攻擊曹操，奸雄獲得攻擊者打出的兩張手牌）
            """)
    @Test
    public void givenViperSpearAttack_AskJianXiongEffectEmittedWithTwoDiscards() {
        Game game = setupGameCaoCaoB(General.曹操);
        Player playerA = game.getPlayer("player-a");
        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));

        // A 用丈八蛇矛棄兩張殺攻擊 B
        game.playerUseViperSpearKill(playerA.getId(), "player-b",
                List.of(BS8008.getCardId(), BH3029.getCardId()));
        // B 不出閃
        List<DomainEvent> events = game.playerPlayCard("player-b", "", playerA.getId(), "skip");

        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
        AskJianXiongEffectEvent ask = (AskJianXiongEffectEvent) events.stream()
                .filter(e -> e instanceof AskJianXiongEffectEvent).findFirst()
                .orElseThrow(() -> new AssertionError("expected AskJianXiongEffectEvent"));
        assertEquals("player-b", ask.getPlayerId());
        assertEquals(List.of(BS8008.getCardId(), BH3029.getCardId()), ask.getSourceCardIds());
        assertTrue(game.getGraveyard().contains(BS8008.getCardId()));
        assertTrue(game.getGraveyard().contains(BH3029.getCardId()));
        assertTrue(game.getTopBehavior().peek() instanceof WaitingJianXiongResponseBehavior);
    }

    @DisplayName("""
            Given
            B 為曹操，受到丈八蛇矛攻擊 → AskJianXiongEffectEvent

            When
            B 選 ACCEPT

            Then
            兩張棄牌都從棄牌堆進入 B 手牌
            JianXiongEffectEvent(taken=true)
            """)
    @Test
    public void givenViperSpearAsk_AcceptChoice_TwoDiscardsReturnToHand() {
        Game game = setupGameCaoCaoB(General.曹操);
        Player playerA = game.getPlayer("player-a");
        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));

        game.playerUseViperSpearKill(playerA.getId(), "player-b",
                List.of(BS8008.getCardId(), BH3029.getCardId()));
        game.playerPlayCard("player-b", "", playerA.getId(), "skip");

        int handSizeBefore = game.getPlayer("player-b").getHand().size();
        List<DomainEvent> events = game.playerUseJianXiongEffect("player-b", AskJianXiongEffectEvent.Choice.ACCEPT);

        assertFalse(game.getGraveyard().contains(BS8008.getCardId()));
        assertFalse(game.getGraveyard().contains(BH3029.getCardId()));
        assertEquals(handSizeBefore + 2, game.getPlayer("player-b").getHand().size());
        assertTrue(game.getPlayer("player-b").getHand().getCards().stream()
                .anyMatch(c -> c.getId().equals(BS8008.getCardId())));
        assertTrue(game.getPlayer("player-b").getHand().getCards().stream()
                .anyMatch(c -> c.getId().equals(BH3029.getCardId())));
        assertTrue(events.stream().anyMatch(e -> e instanceof JianXiongEffectEvent
                && ((JianXiongEffectEvent) e).isTaken()
                && ((JianXiongEffectEvent) e).getSourceCardIds().equals(List.of(BS8008.getCardId(), BH3029.getCardId()))));
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("""
            Given
            B 為曹操，受到丈八蛇矛攻擊 → AskJianXiongEffectEvent

            When
            B 選 SKIP

            Then
            兩張棄牌都留在棄牌堆，B 手牌不變
            JianXiongEffectEvent(taken=false)
            """)
    @Test
    public void givenViperSpearAsk_SkipChoice_NoChange() {
        Game game = setupGameCaoCaoB(General.曹操);
        Player playerA = game.getPlayer("player-a");
        playerA.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));

        game.playerUseViperSpearKill(playerA.getId(), "player-b",
                List.of(BS8008.getCardId(), BH3029.getCardId()));
        game.playerPlayCard("player-b", "", playerA.getId(), "skip");

        int handSizeBefore = game.getPlayer("player-b").getHand().size();
        List<DomainEvent> events = game.playerUseJianXiongEffect("player-b", AskJianXiongEffectEvent.Choice.SKIP);

        assertTrue(game.getGraveyard().contains(BS8008.getCardId()));
        assertTrue(game.getGraveyard().contains(BH3029.getCardId()));
        assertEquals(handSizeBefore, game.getPlayer("player-b").getHand().size());
        assertTrue(events.stream().anyMatch(e -> e instanceof JianXiongEffectEvent
                && !((JianXiongEffectEvent) e).isTaken()));
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("""
            Given
            A 對 B (曹操) 出決鬥；B 無殺
            DuelBehavior on top；A 的決鬥牌進入棄牌堆

            When
            DuelBehavior.doBehaviorAction 觸發 → B 受到 1 點傷害

            Then
            事件中含 AskJianXiongEffectEvent，sourceCardIds = [Duel]
            （FAQ：錦囊牌對曹操造成傷害也可獲得錦囊本身）
            """)
    @Test
    public void givenCaoCaoLosesDuel_AskJianXiongEffectEmitted() {
        Game game = setupGameCaoCaoB(General.曹操);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        // A 加一張決鬥；B 維持沒殺（setupGameCaoCaoB 預設 B 手牌空）
        playerA.getHand().addCardToHand(new Duel(SSA001));

        // A 對 B 出決鬥（B 沒有殺 → DuelBehavior.doBehaviorAction 直接扣血給 B）
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SSA001.getCardId(),
                playerB.getId(), "active");

        assertEquals(3, playerB.getBloodCard().getHp());
        AskJianXiongEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskJianXiongEffectEvent)
                .map(e -> (AskJianXiongEffectEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected AskJianXiongEffectEvent"));
        assertEquals("player-b", ask.getPlayerId());
        assertEquals(List.of(SSA001.getCardId()), ask.getSourceCardIds());
        assertTrue(game.getGraveyard().contains(SSA001.getCardId()));
        assertTrue(game.getTopBehavior().peek() instanceof WaitingJianXiongResponseBehavior);
    }

    @DisplayName("""
            Given
            A 對 B (曹操) 出決鬥；B 有 1 張殺；A 之後也有 1 張殺
            B 出殺 → A 反殺 → B 沒殺 → 受傷

            Then
            事件中含 AskJianXiongEffectEvent，sourceCardIds = [Duel]
            """)
    @Test
    public void givenCaoCaoLosesDuelAfterSwap_AskJianXiongEffectEmitted() {
        Game game = setupGameCaoCaoB(General.曹操);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        // A 已有 BS8008 殺 (預設 setup)；加決鬥
        playerA.getHand().addCardToHand(new Duel(SSA001));
        // B 加一張殺 BHJ037（B 第一個出，剩下沒殺）
        playerB.getHand().addCardToHand(new Kill(BHJ037));

        // A 對 B 出決鬥
        game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), "active");
        // B 先出殺 → 換 A 出殺
        game.playerPlayCard(playerB.getId(), BHJ037.getCardId(), playerA.getId(), "active");
        // A 再出殺 → B 沒殺 → 受傷
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), BS8008.getCardId(),
                playerB.getId(), "active");

        assertEquals(3, playerB.getBloodCard().getHp());
        AskJianXiongEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskJianXiongEffectEvent)
                .map(e -> (AskJianXiongEffectEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected AskJianXiongEffectEvent"));
        assertEquals(List.of(SSA001.getCardId()), ask.getSourceCardIds(),
                "曹操決鬥輸應拿到決鬥本身（不是反殺的殺）");
    }

    @DisplayName("""
            Given
            B (曹操) 為發起方對 A 出決鬥；A 出殺 → B 沒殺反擊 → B 受傷

            Then
            事件中含 AskJianXiongEffectEvent，sourceCardIds = [Duel]
            """)
    @Test
    public void givenCaoCaoIsDuelInitiator_LosesDuel_AskJianXiongEffectEmitted() {
        Game game = setupGameCaoCaoB(General.曹操);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        // 改成 B 為當前回合
        game.setCurrentRound(new com.gaas.threeKingdoms.round.Round(playerB));
        // B (曹操) 加決鬥
        playerB.getHand().addCardToHand(new Duel(SSA001));
        // A 已有 BS8008 殺；B 沒殺

        // B 對 A 出決鬥
        game.playerPlayCard(playerB.getId(), SSA001.getCardId(), playerA.getId(), "active");
        // A 出殺 → B 沒殺 → B 受傷
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), BS8008.getCardId(),
                playerB.getId(), "active");

        assertEquals(3, playerB.getBloodCard().getHp());
        AskJianXiongEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskJianXiongEffectEvent)
                .map(e -> (AskJianXiongEffectEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected AskJianXiongEffectEvent for Duel initiator"));
        assertEquals(List.of(SSA001.getCardId()), ask.getSourceCardIds());
    }

    @DisplayName("""
            Given
            B 為曹操（WEI001）
            stack 頂為 LightningJudgementBehavior（直接模擬 Lightning 判定情境）
            graveyard 含 Lightning scroll（card.effect 結束後 scroll 已進墓地）

            When
            直接呼叫 JianXiongSkill.onDamaged with Lightning sourceCard

            Then
            事件中含 AskJianXiongEffectEvent，sourceCardIds = [Lightning]
            stack 頂被換成 WaitingJianXiongResponseBehavior
            （FAQ：曹操判定閃電受到傷害，可以將閃電收入手牌）
            """)
    @Test
    public void givenLightningJudgementOnStack_OnDamaged_AskJianXiongEffectEmitted() {
        Game game = setupGameCaoCaoB(General.曹操);
        Player caoCao = game.getPlayer("player-b");
        Player attacker = game.getPlayer("player-a");

        // 模擬 Lightning Ward 路徑：LJB 在 stack 頂
        Lightning lightning = new Lightning(SSA014);
        LightningJudgementBehavior ljb = new LightningJudgementBehavior(
                game, caoCao, List.of(caoCao.getId()), null,
                lightning.getId(), "inactive", lightning);
        ljb.setIsOneRound(true); // 模擬 line 645 已標 待 pop
        game.updateTopBehavior(ljb);
        game.getGraveyard().add(lightning);

        DamageContext ctx = new DamageContext(caoCao, attacker, lightning, 1);
        List<DomainEvent> events = new JianXiongSkill().onDamaged(game, ctx);

        AskJianXiongEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskJianXiongEffectEvent)
                .map(e -> (AskJianXiongEffectEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected AskJianXiongEffectEvent for Lightning"));
        assertEquals("player-b", ask.getPlayerId());
        assertEquals(List.of(lightning.getId()), ask.getSourceCardIds());
        assertTrue(game.getTopBehavior().peek() instanceof WaitingJianXiongResponseBehavior);
    }

    @DisplayName("""
            Given
            B 為曹操（WEI001）
            stack 為空（Lightning 無 Ward 路徑）
            graveyard 含 Lightning scroll

            When
            直接呼叫 JianXiongSkill.onDamaged with Lightning sourceCard

            Then
            事件中含 AskJianXiongEffectEvent，sourceCardIds = [Lightning]
            （白名單允許空 stack 觸發奸雄）
            """)
    @Test
    public void givenEmptyStack_OnDamagedByLightning_AskJianXiongEffectEmitted() {
        Game game = setupGameCaoCaoB(General.曹操);
        Player caoCao = game.getPlayer("player-b");
        Player attacker = game.getPlayer("player-a");

        Lightning lightning = new Lightning(SSA014);
        game.getGraveyard().add(lightning);

        // stack 是空的
        assertTrue(game.getTopBehavior().isEmpty());

        DamageContext ctx = new DamageContext(caoCao, attacker, lightning, 1);
        List<DomainEvent> events = new JianXiongSkill().onDamaged(game, ctx);

        AskJianXiongEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskJianXiongEffectEvent)
                .map(e -> (AskJianXiongEffectEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected AskJianXiongEffectEvent for empty stack"));
        assertEquals(List.of(lightning.getId()), ask.getSourceCardIds());
        assertTrue(game.getTopBehavior().peek() instanceof WaitingJianXiongResponseBehavior);
    }

    @DisplayName("""
            Given
            A 出南蠻入侵；B (曹操) 是第一個被詢問的 reactor、無殺
            B 不出殺 → 受傷

            Then
            事件中含 AskJianXiongEffectEvent，sourceCardIds = [BarbarianInvasion]
            stack 頂為 WaitingJianXiongResponseBehavior（BI 還在底）
            （AOE polling 整合：JianXiong 介入時先 defer polling-advance）
            """)
    @Test
    public void givenCaoCaoLosesBarbarianInvasion_MidPolling_AskJianXiongEffectEmitted() {
        Game game = setupGameCaoCaoB(General.曹操);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");

        // A 加南蠻入侵；B 維持沒殺
        playerA.getHand().addCardToHand(new BarbarianInvasion(SS7007));

        // A 出南蠻，B 第一個被詢問
        game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", "active");
        // B 不出殺 → 受傷觸發奸雄
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        assertEquals(3, playerB.getBloodCard().getHp());
        AskJianXiongEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskJianXiongEffectEvent)
                .map(e -> (AskJianXiongEffectEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected AskJianXiongEffectEvent for BI mid-polling"));
        assertEquals(List.of(SS7007.getCardId()), ask.getSourceCardIds());
        assertTrue(game.getTopBehavior().peek() instanceof WaitingJianXiongResponseBehavior);
        // BI 還在 stack 底（polling 未結束）
        assertEquals(2, game.getTopBehavior().size());
        assertTrue(game.getTopBehavior().get(0) instanceof BarbarianInvasionBehavior);
    }

    @DisplayName("""
            Given
            BI 中段觸發奸雄（接續上面情境）

            When
            B 選 ACCEPT

            Then
            南蠻牌進入 B 手牌
            BI polling resume：C 收到 AskKillEvent、activePlayer = C
            stack 只剩 BI（WaitingJX 已 pop）
            """)
    @Test
    public void givenBIMidPolling_AcceptJianXiong_PollingResumesToNextReactor() {
        Game game = setupGameCaoCaoB(General.曹操);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        playerA.getHand().addCardToHand(new BarbarianInvasion(SS7007));

        game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", "active");
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        List<DomainEvent> events = game.playerUseJianXiongEffect(playerB.getId(), AskJianXiongEffectEvent.Choice.ACCEPT);

        // 南蠻進手牌、graveyard 移除
        assertFalse(game.getGraveyard().contains(SS7007.getCardId()));
        assertTrue(playerB.getHand().getCards().stream().anyMatch(c -> c.getId().equals(SS7007.getCardId())));
        // C 被詢問出殺
        AskKillEvent ask = events.stream()
                .filter(e -> e instanceof AskKillEvent)
                .map(e -> (AskKillEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected AskKillEvent for next reactor (C)"));
        assertEquals("player-c", ask.getPlayerId());
        assertEquals("player-c", game.getActivePlayer().getId());
        // stack 只剩 BI（WaitingJX 已 pop，BI mid-poll isOneRound=false）
        assertEquals(1, game.getTopBehavior().size());
        assertTrue(game.getTopBehavior().peek() instanceof BarbarianInvasionBehavior);
    }

    @DisplayName("""
            Given
            A 出南蠻；B/C 都出殺、D (曹操) 為最後 reactor、無殺
            D 不出殺 → 受傷觸發奸雄

            When
            D 選 ACCEPT

            Then
            南蠻進入 D 手牌、stack 空、activePlayer 回到 A（roundPlayer）
            """)
    @Test
    public void givenCaoCaoIsLastReactor_AcceptJianXiong_PollingEnds() {
        Game game = setupGameCaoCaoB(General.曹操);
        // 改成 D 為曹操；B/C/D 順序倒過來才方便讓曹操是 last
        // 簡化：直接讓 player-d 是曹操即可
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        Player playerC = game.getPlayer("player-c");
        Player playerD = game.getPlayer("player-d");
        // 把 D 設成曹操，B 設回非曹操
        playerD.setGeneralCard(new GeneralCard(General.曹操));
        playerB.setGeneralCard(new GeneralCard(General.劉備));

        playerA.getHand().addCardToHand(new BarbarianInvasion(SS7007));
        playerB.getHand().addCardToHand(new Kill(BHJ037));
        playerC.getHand().addCardToHand(new Kill(BS9009));

        // A 出南蠻 → B 出殺 → C 出殺 → D 不出殺
        game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", "active");
        game.playerPlayCard(playerB.getId(), BHJ037.getCardId(), playerA.getId(), "active");
        game.playerPlayCard(playerC.getId(), BS9009.getCardId(), playerA.getId(), "active");
        game.playerPlayCard(playerD.getId(), "", playerA.getId(), "skip");

        // D 為曹操，受傷觸發奸雄；ACCEPT
        List<DomainEvent> events = game.playerUseJianXiongEffect(playerD.getId(), AskJianXiongEffectEvent.Choice.ACCEPT);

        assertFalse(game.getGraveyard().contains(SS7007.getCardId()));
        assertTrue(playerD.getHand().getCards().stream().anyMatch(c -> c.getId().equals(SS7007.getCardId())));
        assertTrue(game.getTopBehavior().isEmpty(), "stack should be empty after last reactor + JianXiong resolved");
        assertEquals("player-a", game.getActivePlayer().getId(), "activePlayer back to round player");
    }

    @DisplayName("""
            Given
            A 出萬箭齊發；B (曹操) 第一個被詢問、無閃
            B 不出閃 → 受傷觸發奸雄

            When
            B 選 ACCEPT

            Then
            萬箭進入 B 手牌、polling resume 到 C（C 被詢問出閃）
            """)
    @Test
    public void givenCaoCaoLosesArrowBarrage_MidPolling_AcceptJianXiong_PollingResumes() {
        Game game = setupGameCaoCaoB(General.曹操);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        playerA.getHand().addCardToHand(new ArrowBarrage(SHA040));

        // A 出萬箭，B 第一個被詢問
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", "active");
        // B 不出閃 → 受傷觸發奸雄
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        // 受傷後 stack 頂應為 WaitingJX（AB 仍在底等 callback resume）
        assertTrue(game.getTopBehavior().peek() instanceof WaitingJianXiongResponseBehavior);

        List<DomainEvent> events = game.playerUseJianXiongEffect(playerB.getId(), AskJianXiongEffectEvent.Choice.ACCEPT);

        assertFalse(game.getGraveyard().contains(SHA040.getCardId()));
        assertTrue(playerB.getHand().getCards().stream().anyMatch(c -> c.getId().equals(SHA040.getCardId())));
        // C 被詢問出閃
        AskDodgeEvent askDodge = events.stream()
                .filter(e -> e instanceof AskDodgeEvent)
                .map(e -> (AskDodgeEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected AskDodgeEvent for C"));
        assertEquals("player-c", askDodge.getPlayerId());
        assertEquals("player-c", game.getActivePlayer().getId());
        assertEquals(1, game.getTopBehavior().size());
        assertTrue(game.getTopBehavior().peek() instanceof ArrowBarrageBehavior);
    }

    @DisplayName("""
            Given
            B (曹操) HP=1，A 出殺打 B → B 不出閃 → HP=0 進瀕死
            C 出桃救 B → B HP=1

            Then
            事件中含 AskJianXiongEffectEvent，sourceCardIds=[殺.id]
            （FAQ：致命傷被救回後仍可發動奸雄）
            """)
    @Test
    public void givenCaoCaoLethalDamageThenRevived_AskJianXiongEffectEmitted() {
        Game game = setupGameCaoCaoB(General.曹操);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        Player playerC = game.getPlayer("player-c");
        playerB.setBloodCard(new BloodCard(1));
        playerC.getHand().addCardToHand(new Peach(BH3029));

        // A 出殺打 B（HP=1）→ B 不出閃 → 進瀕死
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), "active");
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        // 確認進入瀕死流程
        assertEquals(0, playerB.getBloodCard().getHp());
        assertTrue(game.getTopBehavior().peek() instanceof DyingAskPeachBehavior);

        // B 自己沒桃，跳過 → C 被詢問
        game.playerPlayCard(playerB.getId(), "", playerB.getId(), "skip");

        // C 出桃救 B
        List<DomainEvent> events = game.playerPlayCard(playerC.getId(), BH3029.getCardId(), playerB.getId(), "active");

        // B 已救回（HP=1）
        assertEquals(1, playerB.getBloodCard().getHp());

        // 預期：events 含 AskJianXiongEffectEvent
        AskJianXiongEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskJianXiongEffectEvent)
                .map(e -> (AskJianXiongEffectEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected AskJianXiongEffectEvent after revival"));
        assertEquals("player-b", ask.getPlayerId());
        assertEquals(List.of(BS8008.getCardId()), ask.getSourceCardIds());
        // stack 頂為 WaitingJX
        assertTrue(game.getTopBehavior().peek() instanceof WaitingJianXiongResponseBehavior);

        // ACCEPT → 殺進手牌
        game.playerUseJianXiongEffect(playerB.getId(), AskJianXiongEffectEvent.Choice.ACCEPT);
        assertFalse(game.getGraveyard().contains(BS8008.getCardId()));
        assertTrue(playerB.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BS8008.getCardId())));
    }

    @DisplayName("""
            Given
            B (曹操) HP=1，A 出殺打 B → B 不出閃 → HP=0 進瀕死
            其他玩家都不出桃 → B 死亡

            Then
            events 不含 AskJianXiongEffectEvent（FAQ：除非曹操被救回）
            """)
    @Test
    public void givenCaoCaoLethalDamageNoRevival_NoJianXiongTrigger() {
        Game game = setupGameCaoCaoB(General.曹操);
        Player playerA = game.getPlayer("player-a");
        Player playerB = game.getPlayer("player-b");
        Player playerC = game.getPlayer("player-c");
        Player playerD = game.getPlayer("player-d");
        playerB.setBloodCard(new BloodCard(1));

        // A 出殺打 B → B 不出閃 → 進瀕死
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), "active");
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        // B / C / D 依序不出桃
        game.playerPlayCard(playerB.getId(), "", playerB.getId(), "skip");
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), "skip");
        List<DomainEvent> events = game.playerPlayCard(playerD.getId(), "", playerB.getId(), "skip");

        // B 死亡 — 不觸發奸雄
        assertEquals(0, playerB.getBloodCard().getHp());
        assertFalse(events.stream().anyMatch(e -> e instanceof AskJianXiongEffectEvent),
                "JianXiong should not trigger when player actually dies");
    }

    @DisplayName("""
            Given
            B 為曹操（WEI001）
            stack 頂為非 NormalActiveKillBehavior（用一個 anonymous Behavior 模擬）
            graveyard 含一張 Kill

            When
            直接呼叫 JianXiongSkill.onDamaged with Kill source

            Then
            守門路徑生效：返回空 events，stack 不被改動
            （這個情境在 v1 不會自然發生 — sourceCard 是 Kill 而 top 不是 NormalActiveKill —
            但守門是針對未來新加的 OnDamagedSkill 防禦；此 test 鎖定守門行為）
            """)
    @Test
    public void givenJianXiongSkill_NonNormalActiveKillBehaviorOnStack_NoTrigger() {
        Game game = setupGameCaoCaoB(General.曹操);
        Player caoCao = game.getPlayer("player-b");
        Player attacker = game.getPlayer("player-a");

        // Push a non-NormalActiveKillBehavior to stack
        Behavior fakeBehavior = new Behavior(game, caoCao, List.of("player-b"), caoCao,
                "", "active", null, true, false, false);
        game.updateTopBehavior(fakeBehavior);

        // Add a Kill to graveyard so contains() check passes
        Kill kill = new Kill(BS8008);
        game.getGraveyard().add(kill);

        // Direct invoke: stack 頂為 fakeBehavior（非 NormalActiveKillBehavior），sourceCard 是 Kill
        DamageContext ctx = new DamageContext(caoCao, attacker, kill, 1);
        List<DomainEvent> events = new JianXiongSkill().onDamaged(game, ctx);

        assertTrue(events.isEmpty(), "JianXiong should not trigger when top behavior is not NormalActiveKillBehavior");
        assertSame(fakeBehavior, game.getTopBehavior().peek(),
                "fake behavior should remain on stack untouched (not popped, no WaitingJX pushed)");
    }
}

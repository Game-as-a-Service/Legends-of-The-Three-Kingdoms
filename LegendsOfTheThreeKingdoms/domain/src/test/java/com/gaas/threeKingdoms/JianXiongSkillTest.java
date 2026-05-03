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
        assertEquals(BS8008.getCardId(), ask.getSourceCardId());

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

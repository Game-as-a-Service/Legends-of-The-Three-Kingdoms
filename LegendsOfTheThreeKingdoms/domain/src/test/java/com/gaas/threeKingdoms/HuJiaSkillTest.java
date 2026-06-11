package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.behavior.behavior.ArrowBarrageBehavior;
import com.gaas.threeKingdoms.behavior.behavior.HeavenlyDoubleHalberdKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingHuJiaResponseBehavior;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskHuJiaEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.HuJiaEffectEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.HeavenlyDoubleHalberdCard;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

public class HuJiaSkillTest {

    /**
     * 4 人場：
     *   player-a = 馬超（蜀）反賊，攻擊者
     *   player-b = 曹操 主公（MONARCH），HP 4，無閃
     *   player-c = caoCaoFollower1General（決定 c 是否為 Wei）忠臣
     *   player-d = caoCaoFollower2General（決定 d 是否為 Wei）忠臣
     * 座位順序：a → b → c → d → a
     * 護駕 polling 順序：以 b 為起點 next 開始 → c → d → a（過濾 alive Wei + 排除曹操）
     */
    private Game setupGame(General caoCaoRole, General caoCaoFollower1General, General caoCaoFollower2General) {
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.甘寧))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.曹操))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(caoCaoRole == General.曹操 ? Role.MONARCH : Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(caoCaoFollower1General))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(caoCaoFollower2General))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        game.setPlayers(Arrays.asList(playerA, playerB, playerC, playerD));
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));
        return game;
    }

    /** 曹操主公 + C 是 Wei，D 是非 Wei → Wei polling 只有 [C] */
    private Game setupMonarchCaoCaoWithOneWei() {
        return setupGame(General.曹操, General.夏侯惇, General.諸葛亮);
    }

    /** 曹操主公 + C/D 都是 Wei → Wei polling = [C, D] */
    private Game setupMonarchCaoCaoWithTwoWei() {
        return setupGame(General.曹操, General.夏侯惇, General.張遼);
    }

    private void giveDodge(Player p, com.gaas.threeKingdoms.handcard.PlayCard card) {
        p.getHand().addCardToHand(new Dodge(card));
    }

    @DisplayName("""
            Given 曹操為主公，C 為 Wei（夏侯惇）、D 為非 Wei
            When  A 對曹操 (B) 出殺
            Then  emit AskHuJiaEffectEvent 給 C；不 emit AskDodgeEvent；stack 頂為 WaitingHuJiaResponseBehavior；activePlayer = C
            """)
    @Test
    public void givenMonarchCaoCaoAndOneWei_WhenKilled_ThenAskHuJiaToWei() {
        Game game = setupMonarchCaoCaoWithOneWei();
        Player playerC = game.getPlayer("player-c");
        giveDodge(playerC, BH2028);

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        AskHuJiaEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskHuJiaEffectEvent)
                .map(e -> (AskHuJiaEffectEvent) e)
                .findFirst()
                .orElseThrow(() -> new AssertionError("expected AskHuJiaEffectEvent"));
        assertEquals("player-c", ask.getPlayerId());
        assertEquals("player-b", ask.getCaoCaoPlayerId());
        assertEquals(List.of(BH2028.getCardId()), ask.getDodgeCardIdsInHand());
        assertFalse(events.stream().anyMatch(e -> e instanceof AskDodgeEvent),
                "AskDodgeEvent should be replaced by HuJia ask");
        assertTrue(game.peekTopBehavior() instanceof WaitingHuJiaResponseBehavior);
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("""
            Given 曹操為主公、C/D 皆 Wei，C 有閃 D 無
            When  A 對曹操出殺、C ACCEPT 出閃
            Then  C 失去該閃、曹操 HP 不變、stack 回到 Normal、HuJiaEffectEvent(accepted=true) 出現
            """)
    @Test
    public void givenMonarchCaoCao_WhenWeiAcceptDodge_ThenCaoCaoNotDamaged() {
        Game game = setupMonarchCaoCaoWithTwoWei();
        Player playerB = game.getPlayer("player-b");
        Player playerC = game.getPlayer("player-c");
        giveDodge(playerC, BH2028);

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.playerUseHuJiaEffect(
                "player-c", AskHuJiaEffectEvent.Choice.ACCEPT, BH2028.getCardId());

        assertEquals(4, playerB.getHP());
        assertTrue(playerC.getHand().getCards().stream().noneMatch(c -> c.getId().equals(BH2028.getCardId())),
                "Wei should lose the dodge");
        HuJiaEffectEvent huJia = events.stream()
                .filter(e -> e instanceof HuJiaEffectEvent)
                .map(e -> (HuJiaEffectEvent) e)
                .findFirst().orElseThrow();
        assertTrue(huJia.isAccepted());
        assertEquals(BH2028.getCardId(), huJia.getDodgeCardId());
        assertTrue(game.isTopBehaviorEmpty(), "behavior stack should be empty after dodge resolves");
    }

    @DisplayName("""
            Given 曹操主公、C/D 皆 Wei
            When  C DECLINE
            Then  emit 下一個 AskHuJiaEffectEvent(D)、activePlayer = D、stack 仍是 WaitingHuJiaResponseBehavior
            """)
    @Test
    public void givenFirstWeiDecline_ThenAskNextWei() {
        Game game = setupMonarchCaoCaoWithTwoWei();
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        List<DomainEvent> events = game.playerUseHuJiaEffect(
                "player-c", AskHuJiaEffectEvent.Choice.DECLINE, null);

        AskHuJiaEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskHuJiaEffectEvent)
                .map(e -> (AskHuJiaEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals("player-d", ask.getPlayerId());
        assertTrue(game.peekTopBehavior() instanceof WaitingHuJiaResponseBehavior);
        assertEquals("player-d", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("""
            Given 曹操主公、僅 C 為 Wei，C 無閃
            When  C DECLINE（最後一個 Wei）
            Then  pop WaitingHuJia、emit AskDodgeEvent(曹操)、activePlayer = 曹操
            """)
    @Test
    public void givenAllWeiDecline_ThenFallbackToAskCaoCaoDodge() {
        Game game = setupMonarchCaoCaoWithOneWei();
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        List<DomainEvent> events = game.playerUseHuJiaEffect(
                "player-c", AskHuJiaEffectEvent.Choice.DECLINE, null);

        AskDodgeEvent ask = events.stream()
                .filter(e -> e instanceof AskDodgeEvent)
                .map(e -> (AskDodgeEvent) e)
                .findFirst().orElseThrow();
        assertEquals("player-b", ask.getPlayerId());
        assertFalse(game.peekTopBehavior() instanceof WaitingHuJiaResponseBehavior);
        assertTrue(game.peekTopBehavior() instanceof NormalActiveKillBehavior);
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("""
            Given 曹操非主公（MINISTER）、其他 Wei 存活
            When  曹操被殺
            Then  不觸發護駕，emit AskDodgeEvent(曹操)
            """)
    @Test
    public void givenCaoCaoIsNotMonarch_NoHuJiaTrigger() {
        Game game = setupGame(General.劉備 /* not 曹操 → makes B = MINISTER */, General.夏侯惇, General.張遼);
        // override: B 仍是曹操 but as MINISTER（setupGame 已根據 caoCaoRole != 曹操 設成 MINISTER）

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertFalse(events.stream().anyMatch(e -> e instanceof AskHuJiaEffectEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
    }

    @DisplayName("""
            Given 曹操主公、無其他存活 Wei（C / D 皆非 Wei）
            When  曹操被殺
            Then  不觸發護駕，emit AskDodgeEvent(曹操)
            """)
    @Test
    public void givenMonarchCaoCao_NoOtherAliveWei_NoHuJiaTrigger() {
        Game game = setupGame(General.曹操, General.諸葛亮, General.孫權);

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertFalse(events.stream().anyMatch(e -> e instanceof AskHuJiaEffectEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
    }

    @DisplayName("""
            Given 曹操主公、唯一 Wei 武將為死亡狀態
            Then  不觸發護駕
            """)
    @Test
    public void givenOnlyWeiHelperIsDead_NoHuJiaTrigger() {
        Game game = setupMonarchCaoCaoWithOneWei();
        game.getPlayer("player-c").setHealthStatus(HealthStatus.DEATH);

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertFalse(events.stream().anyMatch(e -> e instanceof AskHuJiaEffectEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
    }

    @DisplayName("""
            Given 曹操主公、僅 C 為 Wei；C 嘗試 ACCEPT 但傳的 cardId 不在手中
            Then  throws IllegalArgumentException
            """)
    @Test
    public void givenAcceptWithCardNotInHand_Throws() {
        Game game = setupMonarchCaoCaoWithOneWei();
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseHuJiaEffect("player-c", AskHuJiaEffectEvent.Choice.ACCEPT, BH2028.getCardId()));
    }

    @DisplayName("""
            Given 曹操主公、C 為 Wei、C 嘗試 ACCEPT 非閃 cardId（殺）
            Then  throws IllegalArgumentException
            """)
    @Test
    public void givenAcceptWithNonDodgeCard_Throws() {
        Game game = setupMonarchCaoCaoWithOneWei();
        game.getPlayer("player-c").getHand().addCardToHand(new Kill(BS9009));
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseHuJiaEffect("player-c", AskHuJiaEffectEvent.Choice.ACCEPT, BS9009.getCardId()));
    }

    @DisplayName("""
            Given 萬箭齊發瞄到曹操主公 + 其他 Wei 存活
            When  輪詢到曹操這 reactor
            Then  emit AskHuJiaEffectEvent（不 emit AskDodge），stack 頂為 WaitingHuJia 在 ArrowBarrage 上
            """)
    @Test
    public void givenArrowBarrageWithMonarchCaoCao_HuJiaFiresOnCaoCaoTurn() {
        Game game = setupMonarchCaoCaoWithTwoWei();
        Player playerA = game.getPlayer("player-a");
        playerA.getHand().addCardToHand(new ArrowBarrage(SS7007));

        // A 出萬箭，假設 B 第一個被詢問（座位順序）
        game.playerPlayCard("player-a", SS7007.getCardId(), "player-b", "active");

        // ArrowBarrage 應已 push；驗證 stack 頂是 WaitingHuJia，next-second 是 ArrowBarrage
        assertTrue(game.peekTopBehavior() instanceof WaitingHuJiaResponseBehavior,
                "WaitingHuJia should be top while polling Cao Cao");
        assertTrue(game.peekTopBehaviorSecondElement()
                .map(b -> b instanceof ArrowBarrageBehavior).orElse(false),
                "ArrowBarrage should be the parent behavior");
    }

    @DisplayName("""
            Given 方天畫戟瞄到曹操主公（多目標）+ 其他 Wei 存活
            When  曹操是當前 reactor
            Then  emit AskHuJiaEffectEvent；stack：HeavenlyDoubleHalberd / WaitingHuJia
            """)
    @Test
    public void givenHeavenlyDoubleHalberdWithMonarchCaoCao_HuJiaFiresOnCaoCaoTurn() {
        Game game = setupMonarchCaoCaoWithTwoWei();
        Player playerA = game.getPlayer("player-a");
        playerA.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        // 把 A 手牌弄成「只剩這張殺」(setupGame 已加了 BS8008)
        // 方天畫戟需要 A 手中只剩一張 Kill

        game.playerUseHeavenlyDoubleHalberdKill(
                "player-a", BS8008.getCardId(), List.of("player-b", "player-c"));

        assertTrue(game.peekTopBehavior() instanceof WaitingHuJiaResponseBehavior);
        assertTrue(game.peekTopBehaviorSecondElement()
                .map(b -> b instanceof HeavenlyDoubleHalberdKillBehavior).orElse(false),
                "HeavenlyDoubleHalberd should be parent");
    }

    @DisplayName("""
            Given 萬箭齊發 reactionPlayers=[B(曹操), C(Wei), D(Wei)]，C 手中有閃
            When  B 被詢問 → HuJia 觸發 → C ACCEPT 代閃
            Then  WaitingHuJia pop、stack 頂回到 ArrowBarrage
                  currentReactionPlayer 推進到 C
                  emit AskDodgeEvent(C)（C 非主公，不再觸發 HuJia）
                  C 失去那張閃
            """)
    @Test
    public void givenArrowBarrageOnMonarchCaoCao_WeiAccept_PollingResumesToNextReactor() {
        Game game = setupMonarchCaoCaoWithTwoWei();
        Player playerA = game.getPlayer("player-a");
        Player playerC = game.getPlayer("player-c");
        playerA.getHand().addCardToHand(new ArrowBarrage(SS7007));
        giveDodge(playerC, BH2028);

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-b", "active");
        // ACCEPT 代替曹操出閃
        List<DomainEvent> events = game.playerUseHuJiaEffect(
                "player-c", AskHuJiaEffectEvent.Choice.ACCEPT, BH2028.getCardId());

        // 推進到 polling 下一位 C，由 C 自己被問 dodge（C 非主公不觸發 HuJia 再次）
        assertTrue(game.peekTopBehavior() instanceof ArrowBarrageBehavior,
                "WaitingHuJia should be popped; ArrowBarrage back to top");
        assertEquals("player-c", ((ArrowBarrageBehavior) game.peekTopBehavior())
                .getCurrentReactionPlayer().getId());
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent
                        && ((AskDodgeEvent) e).getPlayerId().equals("player-c")),
                "should ask dodge to next reactor C");
        assertTrue(playerC.getHand().getCards().stream().noneMatch(c -> c.getId().equals(BH2028.getCardId())),
                "C should have lost the dodge used for HuJia");
        // 曹操 HP 未受影響
        assertEquals(4, game.getPlayer("player-b").getHP());
    }

    @DisplayName("""
            Given 方天畫戟多目標 [B(曹操), C(Wei)]，C 與 D 皆 Wei，D 手中有閃
            When  B 被詢問 → HuJia 觸發 → 因 B 下家 C 也是 target reactor，
                  但 HuJia 順序按座位（C 為 caoCao 下家），C ACCEPT 出閃代替
            Then  WaitingHuJia pop、stack 頂回到 HDH
                  currentReactionPlayer 推進到下一 target（reactionPlayers 順序）= C
                  emit AskDodgeEvent(C)
            """)
    @Test
    public void givenHDHOnMonarchCaoCao_WeiAccept_PollingAdvancesToNextHDHTarget() {
        Game game = setupMonarchCaoCaoWithTwoWei();
        Player playerA = game.getPlayer("player-a");
        Player playerC = game.getPlayer("player-c");
        playerA.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        // C 額外多兩張閃（一張用於 HuJia 代閃曹操，一張為 C 自己 dodge）
        giveDodge(playerC, BH2028);
        giveDodge(playerC, BHK039);

        game.playerUseHeavenlyDoubleHalberdKill(
                "player-a", BS8008.getCardId(), List.of("player-b", "player-c"));

        List<DomainEvent> events = game.playerUseHuJiaEffect(
                "player-c", AskHuJiaEffectEvent.Choice.ACCEPT, BH2028.getCardId());

        assertTrue(game.peekTopBehavior() instanceof HeavenlyDoubleHalberdKillBehavior,
                "WaitingHuJia should be popped; HDH back to top");
        HeavenlyDoubleHalberdKillBehavior hdh = (HeavenlyDoubleHalberdKillBehavior) game.peekTopBehavior();
        assertEquals("player-c", hdh.getCurrentReactionPlayer().getId(),
                "HDH polling should advance from B to next target C");
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent
                        && ((AskDodgeEvent) e).getPlayerId().equals("player-c")));
        assertEquals(4, game.getPlayer("player-b").getHP(), "曹操 not damaged");
    }
}

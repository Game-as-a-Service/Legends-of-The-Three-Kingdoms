package com.gaas.threeKingdoms.Ward;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.Utils.getEvent;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class WardWithBarbarianInvasionTest {

    // === Helper methods ===

    private Game createGame() {
        Game game = new Game();
        game.initDeck();
        game.setDeck(new Deck(List.of(new BarbarianInvasion(SSK013), new Dismantle(SS3003))));
        return game;
    }

    private Player createPlayer(String id, General general, Role role) {
        return PlayerBuilder.construct()
                .withId(id)
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(general))
                .withRoleCard(new RoleCard(role))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();
    }

    private void setupGame(Game game, List<Player> players, Player activePlayer) {
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(activePlayer));
    }

    // =============================================
    // Phase 1 Tests (全體取消)
    // =============================================

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有南蠻入侵
            B 有無懈可擊

            When
            A 出南蠻入侵

            Then
            收到 WaitForWardEvent，eligible players 包含 B
            不會收到 AskKillEvent（Ward 攔截）
            """)
    @Test
    public void test1_givenPlayerBHasWard_WhenPlayerAPlaysBarbarianInvasion_ThenWaitForWardEvent() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BarbarianInvasion(SS7007)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Then
        WaitForWardEvent waitForWardEvent = getEvent(events, WaitForWardEvent.class).orElseThrow();
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-b"));
        assertEquals(1, waitForWardEvent.getPlayerIds().size());
        assertEquals(List.of("player-b", "player-c", "player-d"), waitForWardEvent.getTargetPlayerIds());
        // 不應該有 AskKillEvent（Ward 攔截中）
        assertFalse(events.stream().anyMatch(e -> e instanceof AskKillEvent));
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有南蠻入侵 + 無懈可擊
            B 有無懈可擊
            C 有無懈可擊

            When
            A 出南蠻入侵

            Then
            WaitForWardEvent 包含 B, C 但不包含 A（出牌者排除）
            """)
    @Test
    public void test2_givenABCHaveWard_WhenAPlaysBarbarianInvasion_ThenWaitForWardExcludesA() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BarbarianInvasion(SS7007), new Ward(SCQ077)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Then
        WaitForWardEvent waitForWardEvent = getEvent(events, WaitForWardEvent.class).orElseThrow();
        assertFalse(waitForWardEvent.getPlayerIds().contains("player-a"));
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-b"));
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-c"));
        assertEquals(2, waitForWardEvent.getPlayerIds().size());
        assertEquals(List.of("player-b", "player-c", "player-d"), waitForWardEvent.getTargetPlayerIds());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有南蠻入侵
            B 有無懈可擊

            A 出南蠻入侵

            When
            B 出無懈可擊（1 張，奇數 → 抵銷）

            Then
            南蠻入侵被抵銷（WardEvent）
            不會收到 AskKillEvent
            active player 回到 A
            """)
    @Test
    public void test3_givenBHasWard_WhenBPlaysWard_ThenBarbarianInvasionCancelled() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BarbarianInvasion(SS7007)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BarbarianInvasion
        game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // When: B plays Ward
        List<DomainEvent> events = game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow();
        assertNotNull(wardEvent);
        assertFalse(events.stream().anyMatch(e -> e instanceof AskKillEvent));
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有南蠻入侵
            B 有無懈可擊
            C 有無懈可擊

            A 出南蠻入侵

            When
            B 出無懈可擊

            Then
            收到 PlayCardEvent（B 出無懈可擊）
            收到 WaitForWardEvent 包含 C
            """)
    @Test
    public void test4_givenBCHaveWard_WhenBPlaysWard_ThenWaitForCWard() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BarbarianInvasion(SS7007)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BarbarianInvasion
        game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // When: B plays Ward
        List<DomainEvent> events = game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        PlayCardEvent playCardEvent = getEvent(events, PlayCardEvent.class).orElseThrow();
        assertEquals("player-b", playCardEvent.getPlayerId());
        WaitForWardEvent waitForWardEvent = getEvent(events, WaitForWardEvent.class).orElseThrow();
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-c"));
        assertEquals(List.of("player-b", "player-c", "player-d"), waitForWardEvent.getTargetPlayerIds());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有南蠻入侵
            B 有無懈可擊
            C 有無懈可擊

            A 出南蠻入侵
            B 出無懈可擊

            When
            C 出無懈可擊（2 張，偶數 → 效果生效）

            Then
            南蠻入侵生效（有 AskKillEvent for B）
            WardEvent 出現（C 抵銷 B 的無懈可擊）
            """)
    @Test
    public void test5_givenBCPlayWard_WhenEvenWards_ThenBarbarianInvasionProceeds() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BarbarianInvasion(SS7007)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BarbarianInvasion
        game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());
        // B plays Ward
        game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // When: C plays Ward (2 wards, even → effect proceeds)
        List<DomainEvent> events = game.playWardCard("player-c", SCK078.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        assertTrue(events.stream().anyMatch(e -> e instanceof WardEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof AskKillEvent));
        AskKillEvent askKillEvent = getEvent(events, AskKillEvent.class).orElseThrow();
        assertEquals("player-b", askKillEvent.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有南蠻入侵
            B 有無懈可擊
            C 有無懈可擊

            A 出南蠻入侵
            Phase 1: B skip → C skip（全部 skip）→ 效果生效
            Phase 2: B, C 仍有 Ward → WaitForWardEvent
            B skip → C skip Phase 2

            Then
            B 收到 AskKillEvent
            """)
    @Test
    public void test6_givenAllSkipWard_ThenBarbarianInvasionProceeds() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BarbarianInvasion(SS7007)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BarbarianInvasion → Phase 1 WaitForWardEvent
        game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());
        // Phase 1: B skips
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());
        // Phase 1: C skips → 0 wards → proceeds → Phase 2 WaitForWardEvent (B, C still have Ward)
        game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Phase 2: B skips
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());
        // When: Phase 2: C skips → 0 wards → B gets AskKillEvent
        List<DomainEvent> events = game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Then
        assertTrue(events.stream().anyMatch(e -> e instanceof AskKillEvent));
        AskKillEvent askKillEvent = getEvent(events, AskKillEvent.class).orElseThrow();
        assertEquals("player-b", askKillEvent.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有南蠻入侵 + 無懈可擊
            B 有無懈可擊
            C 有無懈可擊

            A 出南蠻入侵
            Phase 1: B 出無懈可擊 → A 出無懈可擊（反制 B）→ C skip
            2 張無懈可擊（偶數）→ 生效
            Phase 2: C 仍有 Ward → WaitForWardEvent → C skip

            Then
            收到 WardEvent（A 的無懈可擊抵銷了 B 的無懈可擊）
            Phase 2 skip 後收到 AskKillEvent for B
            """)
    @Test
    public void test7_givenBPlaysWardAPlaysWardCSkips_ThenBarbarianInvasionProceeds() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BarbarianInvasion(SS7007), new Ward(SCQ077)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BarbarianInvasion → Phase 1 WaitForWardEvent
        game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());
        // Phase 1: B plays Ward
        game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // Phase 1: A plays Ward (counter B)
        game.playWardCard("player-a", SCQ077.getCardId(), PlayType.ACTIVE.getPlayType());
        // Phase 1: C skips → 2 wards (even) → proceeds → Phase 2 WaitForWardEvent (C still has Ward)
        List<DomainEvent> phase1Events = game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Verify Phase 1 WardEvent is present
        assertTrue(phase1Events.stream().anyMatch(e -> e instanceof WardEvent));

        // When: Phase 2: C skips → 0 wards → B gets AskKillEvent
        List<DomainEvent> events = game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Then
        assertTrue(events.stream().anyMatch(e -> e instanceof AskKillEvent));
        AskKillEvent askKillEvent = getEvent(events, AskKillEvent.class).orElseThrow();
        assertEquals("player-b", askKillEvent.getPlayerId());
    }

    // =============================================
    // Phase 2 Tests (逐人發動)
    // =============================================

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有南蠻入侵
            沒有人有無懈可擊

            When
            A 出南蠻入侵

            Then
            南蠻入侵正常開始輪詢
            B 收到 AskKillEvent
            """)
    @Test
    public void test8_givenNoOneHasWard_WhenAPlaysBarbarianInvasion_ThenNormalPolling() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BarbarianInvasion(SS7007)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Then
        assertTrue(events.stream().anyMatch(e -> e instanceof AskKillEvent));
        AskKillEvent askKillEvent = getEvent(events, AskKillEvent.class).orElseThrow();
        assertEquals("player-b", askKillEvent.getPlayerId());
        assertEquals("player-b", game.getActivePlayer().getId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有南蠻入侵
            C 有無懈可擊

            A 出南蠻入侵
            Phase 1: C skip → proceeds
            Phase 2 for B: C has Ward → WaitForWardEvent → C skip → B AskKillEvent
            B skip 殺 → B 扣血
            Phase 2 for C: C has Ward → WaitForWardEvent → C plays Ward → C protected

            Then
            C 被 Ward 保護後，跳到 D 的 AskKillEvent
            """)
    @Test
    public void test9_givenPhase2Ward_WhenWardProtectsC_ThenSkipToD() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BarbarianInvasion(SS7007)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BarbarianInvasion → Phase 1 WaitForWardEvent (C has Ward)
        game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Phase 1: C skips → 0 wards → proceeds
        // → Phase 2 for B: C has Ward → WaitForWardEvent
        game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Phase 2 for B: C skips → 0 wards → B gets AskKillEvent
        List<DomainEvent> phase2Events = game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());
        AskKillEvent bAskKill = getEvent(phase2Events, AskKillEvent.class).orElseThrow();
        assertEquals("player-b", bAskKill.getPlayerId());

        // B skips kill → B takes damage → Phase 2 for C: C has Ward → WaitForWardEvent
        List<DomainEvent> bSkipEvents = game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());

        // Check that Phase 2 Ward is triggered for C
        WaitForWardEvent waitForWard = getEvent(bSkipEvents, WaitForWardEvent.class).orElseThrow();
        assertTrue(waitForWard.getPlayerIds().contains("player-c"));
        assertEquals(List.of("player-c"), waitForWard.getTargetPlayerIds());

        // C plays Ward to protect themselves (Phase 2) → 1 ward (odd) → C is protected
        List<DomainEvent> wardEvents = game.playWardCard("player-c", SCK078.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: C is protected, D gets AskKillEvent (no Ward left)
        AskKillEvent dAskKill = getEvent(wardEvents, AskKillEvent.class).orElseThrow();
        assertEquals("player-d", dAskKill.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有南蠻入侵
            B 有無懈可擊 (用於 Phase 2 保護 B 自己)
            沒人有 Phase 1 Ward (B 的 Ward 也會觸發 Phase 1)

            When
            A 出南蠻入侵
            B skip Phase 1 Ward
            Phase 2: B 出 Ward 保護自己
            C skip 殺 → C 扣血

            Then
            C hp = 3
            D 收到 AskKillEvent
            """)
    @Test
    public void test10_givenBProtectedByWard_WhenCSkipsKill_ThenCTakesDamageAndDAskKill() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BarbarianInvasion(SS7007)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BarbarianInvasion → Phase 1 Ward (B has Ward)
        game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // B skips Phase 1 Ward → 0 wards → proceeds
        // Phase 2: askNextPlayerOrWard for B → B has Ward → WaitForWardEvent
        List<DomainEvent> skipEvents = game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());

        // B plays Ward to protect themselves (Phase 2)
        List<DomainEvent> wardEvents = game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: B is protected, C gets AskKillEvent
        AskKillEvent cAskKill = getEvent(wardEvents, AskKillEvent.class).orElseThrow();
        assertEquals("player-c", cAskKill.getPlayerId());

        // C skips kill → takes damage
        List<DomainEvent> cSkipEvents = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());

        // Then
        assertEquals(3, game.getPlayer("player-c").getBloodCard().getHp());
        AskKillEvent dAskKill = getEvent(cSkipEvents, AskKillEvent.class).orElseThrow();
        assertEquals("player-d", dAskKill.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有南蠻入侵
            D 有無懈可擊 (用於 Phase 2 保護 D 自己)
            D 是最後一個 reaction player

            A 出南蠻入侵
            Phase 1: D skip → proceeds
            Phase 2 for B: D has Ward → D skip → B AskKillEvent
            B skip 殺 → B 扣血
            Phase 2 for C: D has Ward → D skip → C AskKillEvent
            C skip 殺 → C 扣血
            Phase 2 for D: D has Ward → D plays Ward → D protected → last player

            Then
            南蠻入侵結束
            Active player 回到 A
            """)
    @Test
    public void test11_givenLastPlayerProtectedByWard_ThenBarbarianInvasionEnds() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BarbarianInvasion(SS7007)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);
        playerD.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BarbarianInvasion → Phase 1 Ward (D has Ward) → WaitForWardEvent
        game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Phase 1: D skips → 0 wards → proceeds → Phase 2 for B (D has Ward) → WaitForWardEvent
        game.playWardCard("player-d", "", PlayType.SKIP.getPlayType());

        // Phase 2 for B: D skips → 0 wards → B gets AskKillEvent
        game.playWardCard("player-d", "", PlayType.SKIP.getPlayType());

        // B skips kill → B takes damage → Phase 2 for C (D has Ward) → WaitForWardEvent
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());

        // Phase 2 for C: D skips → 0 wards → C gets AskKillEvent
        game.playWardCard("player-d", "", PlayType.SKIP.getPlayType());

        // C skips kill → C takes damage → Phase 2 for D (D has Ward) → WaitForWardEvent
        game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());

        // Phase 2 for D: D plays Ward → 1 ward (odd) → D protected → last player → ends
        List<DomainEvent> wardEvents = game.playWardCard("player-d", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: D is last player, protected → BarbarianInvasion ends
        assertFalse(wardEvents.stream().anyMatch(e -> e instanceof AskKillEvent));
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有南蠻入侵
            B 有 2 張無懈可擊 (Phase 2)
            C 有無懈可擊

            A 出南蠻入侵
            B skip Phase 1 Ward
            C skip Phase 1 Ward

            Phase 2: B 出 Ward 保護自己
            C 出 Ward 反制 B 的 Ward (偶數)

            Then
            B 仍需出殺（Ward 互相抵銷，2 張偶數 → 效果生效 → B 需出殺）
            """)
    @Test
    public void test12_givenPhase2EvenWards_ThenPlayerStillNeedsKill() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BarbarianInvasion(SS7007)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BarbarianInvasion → Phase 1 Ward (B, C have Ward)
        game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // B skips Phase 1
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());
        // C skips Phase 1 → 0 wards → proceeds
        List<DomainEvent> phase1Done = game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Phase 2: askNextPlayerOrWard for B → B has Ward → WaitForWardEvent
        // B plays Ward to protect themselves
        game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // C plays Ward to counter B's Ward → 2 wards (even) → effect NOT cancelled → B must play kill
        List<DomainEvent> counterEvents = game.playWardCard("player-c", SCK078.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: B still needs to play kill (even number of wards → Ward failed to protect)
        AskKillEvent askKillEvent = getEvent(counterEvents, AskKillEvent.class).orElseThrow();
        assertEquals("player-b", askKillEvent.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有南蠻入侵
            B C 有無懈可擊

            Phase 1: B 出 Ward，C 出 Ward（偶數 → 效果生效）
            Phase 2: 沒人有 Ward → 正常輪詢 B

            When
            B skip 殺 → B 扣血

            Then
            B hp = 3
            C 收到 AskKillEvent
            """)
    @Test
    public void test13_givenPhase1EvenThenPhase2NoWard_ThenNormalPolling() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BarbarianInvasion(SS7007)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BarbarianInvasion → Phase 1 Ward (B, C have Ward)
        game.playerPlayCard(playerA.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // B plays Ward (Phase 1)
        game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // C plays Ward (Phase 1 counter) → 2 wards even → proceeds
        List<DomainEvent> phase1Done = game.playWardCard("player-c", SCK078.getCardId(), PlayType.ACTIVE.getPlayType());

        // Phase 2: No one has Ward anymore → B gets AskKillEvent directly
        AskKillEvent bAskKill = getEvent(phase1Done, AskKillEvent.class).orElseThrow();
        assertEquals("player-b", bAskKill.getPlayerId());

        // When: B skips kill → takes damage
        List<DomainEvent> bSkipEvents = game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());

        // Then
        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
        AskKillEvent cAskKill = getEvent(bSkipEvents, AskKillEvent.class).orElseThrow();
        assertEquals("player-c", cAskKill.getPlayerId());
    }
}

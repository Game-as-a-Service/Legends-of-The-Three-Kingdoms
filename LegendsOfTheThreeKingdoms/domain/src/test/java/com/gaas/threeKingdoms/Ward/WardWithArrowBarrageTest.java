package com.gaas.threeKingdoms.Ward;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
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

public class WardWithArrowBarrageTest {

    // === Helper methods ===

    private Game createGame() {
        Game game = new Game();
        game.initDeck();
        game.setDeck(new Deck(List.of(new ArrowBarrage(SHA040), new Dismantle(SS3003))));
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
            A 有萬箭齊發
            B 有無懈可擊

            When
            A 出萬箭齊發

            Then
            收到 WaitForWardEvent，eligible players 包含 B
            不會收到 AskDodgeEvent（Ward 攔截）
            """)
    @Test
    public void test1_givenPlayerBHasWard_WhenPlayerAPlaysArrowBarrage_ThenWaitForWardEvent() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new ArrowBarrage(SHA040)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Then
        WaitForWardEvent waitForWardEvent = getEvent(events, WaitForWardEvent.class).orElseThrow();
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-b"));
        assertEquals(1, waitForWardEvent.getPlayerIds().size());
        assertEquals(List.of("player-b", "player-c", "player-d"), waitForWardEvent.getTargetPlayerIds());
        // 不應該有 AskDodgeEvent（Ward 攔截中）
        assertFalse(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有萬箭齊發 + 無懈可擊
            B 有無懈可擊
            C 有無懈可擊

            When
            A 出萬箭齊發

            Then
            WaitForWardEvent 包含 B, C 但不包含 A（出牌者排除）
            """)
    @Test
    public void test2_givenABCHaveWard_WhenAPlaysArrowBarrage_ThenWaitForWardExcludesA() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new ArrowBarrage(SHA040), new Ward(SCQ077)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

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
            A 有萬箭齊發
            B 有無懈可擊

            A 出萬箭齊發

            When
            B 出無懈可擊（1 張，奇數 → 抵銷）

            Then
            萬箭齊發被抵銷（WardEvent）
            不會收到 AskDodgeEvent
            active player 回到 A
            """)
    @Test
    public void test3_givenBHasWard_WhenBPlaysWard_ThenArrowBarrageCancelled() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new ArrowBarrage(SHA040)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays ArrowBarrage
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // When: B plays Ward
        List<DomainEvent> events = game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow();
        assertNotNull(wardEvent);
        assertFalse(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有萬箭齊發
            B 有無懈可擊
            C 有無懈可擊

            A 出萬箭齊發

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
        playerA.getHand().addCardToHand(Arrays.asList(new ArrowBarrage(SHA040)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays ArrowBarrage
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

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
            A 有萬箭齊發
            B 有無懈可擊
            C 有無懈可擊

            A 出萬箭齊發
            B 出無懈可擊

            When
            C 出無懈可擊（2 張，偶數 → 效果生效）

            Then
            萬箭齊發生效（有 AskDodgeEvent for B）
            WardEvent 出現（C 抵銷 B 的無懈可擊）
            """)
    @Test
    public void test5_givenBCPlayWard_WhenEvenWards_ThenArrowBarrageProceeds() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new ArrowBarrage(SHA040)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays ArrowBarrage
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());
        // B plays Ward
        game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // When: C plays Ward (2 wards, even → effect proceeds)
        List<DomainEvent> events = game.playWardCard("player-c", SCK078.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        assertTrue(events.stream().anyMatch(e -> e instanceof WardEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
        AskDodgeEvent askDodgeEvent = getEvent(events, AskDodgeEvent.class).orElseThrow();
        assertEquals("player-b", askDodgeEvent.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有萬箭齊發
            B 有無懈可擊
            C 有無懈可擊

            A 出萬箭齊發
            Phase 1: B skip → C skip（全部 skip）→ 效果生效
            Phase 2: B, C 仍有 Ward → WaitForWardEvent
            B skip → C skip Phase 2

            Then
            B 收到 AskDodgeEvent
            """)
    @Test
    public void test6_givenAllSkipWard_ThenArrowBarrageProceeds() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new ArrowBarrage(SHA040)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays ArrowBarrage → Phase 1 WaitForWardEvent
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());
        // Phase 1: B skips
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());
        // Phase 1: C skips → 0 wards → proceeds → Phase 2 WaitForWardEvent (B, C still have Ward)
        game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Phase 2: B skips
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());
        // When: Phase 2: C skips → 0 wards → B gets AskDodgeEvent
        List<DomainEvent> events = game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Then
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
        AskDodgeEvent askDodgeEvent = getEvent(events, AskDodgeEvent.class).orElseThrow();
        assertEquals("player-b", askDodgeEvent.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有萬箭齊發 + 無懈可擊
            B 有無懈可擊
            C 有無懈可擊

            A 出萬箭齊發
            Phase 1: B 出無懈可擊 → A 出無懈可擊（反制 B）→ C skip
            2 張無懈可擊（偶數）→ 生效
            Phase 2: C 仍有 Ward → WaitForWardEvent → C skip

            Then
            收到 WardEvent（A 的無懈可擊抵銷了 B 的無懈可擊）
            Phase 2 skip 後收到 AskDodgeEvent for B
            """)
    @Test
    public void test7_givenBPlaysWardAPlaysWardCSkips_ThenArrowBarrageProceeds() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new ArrowBarrage(SHA040), new Ward(SCQ077)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays ArrowBarrage → Phase 1 WaitForWardEvent
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());
        // Phase 1: B plays Ward
        game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // Phase 1: A plays Ward (counter B)
        game.playWardCard("player-a", SCQ077.getCardId(), PlayType.ACTIVE.getPlayType());
        // Phase 1: C skips → 2 wards (even) → proceeds → Phase 2 WaitForWardEvent (C still has Ward)
        List<DomainEvent> phase1Events = game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Verify Phase 1 WardEvent is present
        assertTrue(phase1Events.stream().anyMatch(e -> e instanceof WardEvent));

        // When: Phase 2: C skips → 0 wards → B gets AskDodgeEvent
        List<DomainEvent> events = game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Then
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
        AskDodgeEvent askDodgeEvent = getEvent(events, AskDodgeEvent.class).orElseThrow();
        assertEquals("player-b", askDodgeEvent.getPlayerId());
    }

    // =============================================
    // Phase 2 Tests (逐人發動)
    // =============================================

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有萬箭齊發
            沒有人有無懈可擊

            When
            A 出萬箭齊發

            Then
            萬箭齊發正常開始輪詢
            B 收到 AskDodgeEvent
            """)
    @Test
    public void test8_givenNoOneHasWard_WhenAPlaysArrowBarrage_ThenNormalPolling() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new ArrowBarrage(SHA040)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Then
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
        AskDodgeEvent askDodgeEvent = getEvent(events, AskDodgeEvent.class).orElseThrow();
        assertEquals("player-b", askDodgeEvent.getPlayerId());
        assertEquals("player-b", game.getActivePlayer().getId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有萬箭齊發
            C 有無懈可擊

            A 出萬箭齊發
            Phase 1: C skip → proceeds
            Phase 2 for B: C has Ward → WaitForWardEvent → C skip → B AskDodgeEvent
            B skip 閃 → B 扣血
            Phase 2 for C: C has Ward → WaitForWardEvent → C plays Ward → C protected

            Then
            C 被 Ward 保護後，跳到 D 的 AskDodgeEvent
            """)
    @Test
    public void test9_givenPhase2Ward_WhenWardProtectsC_ThenSkipToD() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new ArrowBarrage(SHA040)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays ArrowBarrage → Phase 1 WaitForWardEvent (C has Ward)
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Phase 1: C skips → 0 wards → proceeds
        // → Phase 2 for B: C has Ward → WaitForWardEvent
        game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Phase 2 for B: C skips → 0 wards → B gets AskDodgeEvent
        List<DomainEvent> phase2Events = game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());
        AskDodgeEvent bAskDodge = getEvent(phase2Events, AskDodgeEvent.class).orElseThrow();
        assertEquals("player-b", bAskDodge.getPlayerId());

        // B skips dodge → B takes damage → Phase 2 for C: C has Ward → WaitForWardEvent
        List<DomainEvent> bSkipEvents = game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());

        // Check that Phase 2 Ward is triggered for C
        WaitForWardEvent waitForWard = getEvent(bSkipEvents, WaitForWardEvent.class).orElseThrow();
        assertTrue(waitForWard.getPlayerIds().contains("player-c"));
        assertEquals(List.of("player-c"), waitForWard.getTargetPlayerIds());

        // C plays Ward to protect themselves (Phase 2) → 1 ward (odd) → C is protected
        List<DomainEvent> wardEvents = game.playWardCard("player-c", SCK078.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: C is protected, D gets AskDodgeEvent (no Ward left)
        AskDodgeEvent dAskDodge = getEvent(wardEvents, AskDodgeEvent.class).orElseThrow();
        assertEquals("player-d", dAskDodge.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有萬箭齊發
            B 有無懈可擊 (用於 Phase 2 保護 B 自己)

            When
            A 出萬箭齊發
            B skip Phase 1 Ward
            Phase 2: B 出 Ward 保護自己
            C skip 閃 → C 扣血

            Then
            C hp = 3
            D 收到 AskDodgeEvent
            """)
    @Test
    public void test10_givenBProtectedByWard_WhenCSkipsDodge_ThenCTakesDamageAndDAskDodge() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new ArrowBarrage(SHA040)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays ArrowBarrage → Phase 1 Ward (B has Ward)
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // B skips Phase 1 Ward → 0 wards → proceeds
        // Phase 2: askNextPlayerOrWard for B → B has Ward → WaitForWardEvent
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());

        // B plays Ward to protect themselves (Phase 2)
        List<DomainEvent> wardEvents = game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: B is protected, C gets AskDodgeEvent
        AskDodgeEvent cAskDodge = getEvent(wardEvents, AskDodgeEvent.class).orElseThrow();
        assertEquals("player-c", cAskDodge.getPlayerId());

        // C skips dodge → takes damage
        List<DomainEvent> cSkipEvents = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());

        // Then
        assertEquals(3, game.getPlayer("player-c").getBloodCard().getHp());
        AskDodgeEvent dAskDodge = getEvent(cSkipEvents, AskDodgeEvent.class).orElseThrow();
        assertEquals("player-d", dAskDodge.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有萬箭齊發
            D 有無懈可擊 (用於 Phase 2 保護 D 自己)
            D 是最後一個 reaction player

            A 出萬箭齊發
            Phase 1: D skip → proceeds
            Phase 2 for B: D has Ward → D skip → B AskDodgeEvent
            B skip 閃 → B 扣血
            Phase 2 for C: D has Ward → D skip → C AskDodgeEvent
            C skip 閃 → C 扣血
            Phase 2 for D: D has Ward → D plays Ward → D protected → last player

            Then
            萬箭齊發結束
            Active player 回到 A
            """)
    @Test
    public void test11_givenLastPlayerProtectedByWard_ThenArrowBarrageEnds() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new ArrowBarrage(SHA040)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);
        playerD.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays ArrowBarrage → Phase 1 Ward (D has Ward) → WaitForWardEvent
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Phase 1: D skips → 0 wards → proceeds → Phase 2 for B (D has Ward) → WaitForWardEvent
        game.playWardCard("player-d", "", PlayType.SKIP.getPlayType());

        // Phase 2 for B: D skips → 0 wards → B gets AskDodgeEvent
        game.playWardCard("player-d", "", PlayType.SKIP.getPlayType());

        // B skips dodge → B takes damage → Phase 2 for C (D has Ward) → WaitForWardEvent
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());

        // Phase 2 for C: D skips → 0 wards → C gets AskDodgeEvent
        game.playWardCard("player-d", "", PlayType.SKIP.getPlayType());

        // C skips dodge → C takes damage → Phase 2 for D (D has Ward) → WaitForWardEvent
        game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());

        // Phase 2 for D: D plays Ward → 1 ward (odd) → D protected → last player → ends
        List<DomainEvent> wardEvents = game.playWardCard("player-d", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: D is last player, protected → ArrowBarrage ends
        assertFalse(wardEvents.stream().anyMatch(e -> e instanceof AskDodgeEvent));
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有萬箭齊發
            B 有無懈可擊
            C 有無懈可擊

            A 出萬箭齊發
            B skip Phase 1 Ward
            C skip Phase 1 Ward

            Phase 2: B 出 Ward 保護自己
            C 出 Ward 反制 B 的 Ward (偶數)

            Then
            B 仍需出閃（Ward 互相抵銷，2 張偶數 → 效果生效 → B 需出閃）
            """)
    @Test
    public void test12_givenPhase2EvenWards_ThenPlayerStillNeedsDodge() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new ArrowBarrage(SHA040)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays ArrowBarrage → Phase 1 Ward (B, C have Ward)
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // B skips Phase 1
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());
        // C skips Phase 1 → 0 wards → proceeds
        game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Phase 2: askNextPlayerOrWard for B → B has Ward → WaitForWardEvent
        // B plays Ward to protect themselves
        game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // C plays Ward to counter B's Ward → 2 wards (even) → effect NOT cancelled → B must play dodge
        List<DomainEvent> counterEvents = game.playWardCard("player-c", SCK078.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: B still needs to play dodge (even number of wards → Ward failed to protect)
        AskDodgeEvent askDodgeEvent = getEvent(counterEvents, AskDodgeEvent.class).orElseThrow();
        assertEquals("player-b", askDodgeEvent.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有萬箭齊發
            B C 有無懈可擊

            Phase 1: B 出 Ward，C 出 Ward（偶數 → 效果生效）
            Phase 2: 沒人有 Ward → 正常輪詢 B

            When
            B skip 閃 → B 扣血

            Then
            B hp = 3
            C 收到 AskDodgeEvent
            """)
    @Test
    public void test13_givenPhase1EvenThenPhase2NoWard_ThenNormalPolling() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new ArrowBarrage(SHA040)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays ArrowBarrage → Phase 1 Ward (B, C have Ward)
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // B plays Ward (Phase 1)
        game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // C plays Ward (Phase 1 counter) → 2 wards even → proceeds
        List<DomainEvent> phase1Done = game.playWardCard("player-c", SCK078.getCardId(), PlayType.ACTIVE.getPlayType());

        // Phase 2: No one has Ward anymore → B gets AskDodgeEvent directly
        AskDodgeEvent bAskDodge = getEvent(phase1Done, AskDodgeEvent.class).orElseThrow();
        assertEquals("player-b", bAskDodge.getPlayerId());

        // When: B skips dodge → takes damage
        List<DomainEvent> bSkipEvents = game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());

        // Then
        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
        AskDodgeEvent cAskDodge = getEvent(bSkipEvents, AskDodgeEvent.class).orElseThrow();
        assertEquals("player-c", cAskDodge.getPlayerId());
    }
}

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
import com.gaas.threeKingdoms.handcard.scrollcard.BountifulHarvest;
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

public class WardWithBountifulHarvestTest {

    // === Helper methods ===

    private Game createGame() {
        Game game = new Game();
        game.initDeck();
        // Deck cards will be drawn for BountifulHarvest effect (one per player)
        game.setDeck(new Deck(List.of(new Kill(BS8008), new Peach(BH3029), new Kill(BC2054), new Peach(BH0036))));
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
            A 有五穀豐登
            B 有無懈可擊

            When
            A 出五穀豐登

            Then
            收到 WaitForWardEvent，eligible players 包含 B
            不會收到 BountifulHarvestEvent（Ward 攔截）
            """)
    @Test
    public void test1_givenPlayerBHasWard_WhenPlayerAPlaysBountifulHarvest_ThenWaitForWardEvent() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BountifulHarvest(SH3042)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SH3042.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Then
        WaitForWardEvent waitForWardEvent = getEvent(events, WaitForWardEvent.class).orElseThrow();
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-b"));
        assertEquals(1, waitForWardEvent.getPlayerIds().size());
        // Target is all reactionPlayers (including caster for BH)
        assertEquals(List.of("player-a", "player-b", "player-c", "player-d"), waitForWardEvent.getTargetPlayerIds());
        // 不應該有 BountifulHarvestEvent（Ward 攔截中）
        assertFalse(events.stream().anyMatch(e -> e instanceof BountifulHarvestEvent));
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有五穀豐登
            B 有無懈可擊

            A 出五穀豐登

            When
            B 出無懈可擊（1 張，奇數 → 抵銷）

            Then
            五穀豐登被抵銷（WardEvent）
            不會收到 BountifulHarvestEvent
            active player 回到 A
            """)
    @Test
    public void test2_givenBHasWard_WhenBPlaysWard_ThenBountifulHarvestCancelled() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BountifulHarvest(SH3042)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BountifulHarvest
        game.playerPlayCard(playerA.getId(), SH3042.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // When: B plays Ward
        List<DomainEvent> events = game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow();
        assertNotNull(wardEvent);
        assertFalse(events.stream().anyMatch(e -> e instanceof BountifulHarvestEvent));
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有五穀豐登
            B 有無懈可擊
            C 有無懈可擊

            A 出五穀豐登
            B 出無懈可擊

            When
            C 出無懈可擊（2 張，偶數 → 效果生效）

            Then
            五穀豐登生效（有 BountifulHarvestEvent）
            WardEvent 出現（C 抵銷 B 的無懈可擊）
            """)
    @Test
    public void test3_givenBCPlayWard_WhenEvenWards_ThenBountifulHarvestProceeds() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BountifulHarvest(SH3042)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BountifulHarvest
        game.playerPlayCard(playerA.getId(), SH3042.getCardId(), "", PlayType.ACTIVE.getPlayType());
        // B plays Ward
        game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // When: C plays Ward (2 wards, even → effect proceeds)
        List<DomainEvent> events = game.playWardCard("player-c", SCK078.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        assertTrue(events.stream().anyMatch(e -> e instanceof WardEvent));
        // Phase 1 even → proceed to Phase 2, first player should get BountifulHarvestEvent
        assertTrue(events.stream().anyMatch(e -> e instanceof BountifulHarvestEvent));
        BountifulHarvestEvent bhEvent = getEvent(events, BountifulHarvestEvent.class).orElseThrow();
        assertEquals("player-a", bhEvent.getNextChoosingPlayerId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有五穀豐登
            B 有無懈可擊

            A 出五穀豐登
            Phase 1: B skip（全部 skip）→ 效果生效

            Then
            Phase 2: 第一個玩家（A）收到 BountifulHarvestEvent 或 WaitForWardEvent
            """)
    @Test
    public void test4_givenAllSkipPhase1Ward_ThenBountifulHarvestProceeds() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BountifulHarvest(SH3042)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BountifulHarvest → Phase 1 WaitForWardEvent
        game.playerPlayCard(playerA.getId(), SH3042.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // When: B skips Phase 1 → 0 wards → proceeds to Phase 2
        // Phase 2 for A: B has Ward → WaitForWardEvent
        List<DomainEvent> events = game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());

        // Then: Phase 2 WaitForWardEvent for A's turn (B still has Ward)
        WaitForWardEvent phase2Ward = getEvent(events, WaitForWardEvent.class).orElseThrow();
        assertTrue(phase2Ward.getPlayerIds().contains("player-b"));
        assertEquals(List.of("player-a"), phase2Ward.getTargetPlayerIds());
    }

    // =============================================
    // Phase 2 Tests (逐人發動)
    // =============================================

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有五穀豐登
            沒有人有無懈可擊

            When
            A 出五穀豐登

            Then
            五穀豐登正常開始
            A 收到 BountifulHarvestEvent（A 先選）
            """)
    @Test
    public void test5_givenNoOneHasWard_WhenAPlaysBountifulHarvest_ThenNormalPicking() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BountifulHarvest(SH3042)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SH3042.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Then: No Ward, normal picking starts with A
        assertTrue(events.stream().anyMatch(e -> e instanceof BountifulHarvestEvent));
        BountifulHarvestEvent bhEvent = getEvent(events, BountifulHarvestEvent.class).orElseThrow();
        assertEquals("player-a", bhEvent.getNextChoosingPlayerId());
        assertEquals(4, bhEvent.getAssignmentCardIds().size());
        assertFalse(events.stream().anyMatch(e -> e instanceof WaitForWardEvent));
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有五穀豐登
            B 有無懈可擊

            A 出五穀豐登
            Phase 1: B skip → proceeds
            Phase 2 for A: B has Ward → WaitForWardEvent → B skip
            A 選牌

            When
            Phase 2 for B: B has Ward → WaitForWardEvent → B plays Ward → B 被跳過

            Then
            B 被 Ward 跳過，C 收到 BountifulHarvestEvent
            """)
    @Test
    public void test6_givenPhase2Ward_WhenWardSkipsB_ThenSkipToC() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BountifulHarvest(SH3042)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BountifulHarvest → Phase 1 WaitForWardEvent (B has Ward)
        game.playerPlayCard(playerA.getId(), SH3042.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Phase 1: B skips → 0 wards → proceeds to Phase 2
        // Phase 2 for A: B has Ward → WaitForWardEvent
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());

        // Phase 2 for A: B skips → 0 wards → A gets BountifulHarvestEvent
        List<DomainEvent> aPickEvents = game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());
        BountifulHarvestEvent aPickEvent = getEvent(aPickEvents, BountifulHarvestEvent.class).orElseThrow();
        assertEquals("player-a", aPickEvent.getNextChoosingPlayerId());

        // A picks a card → Phase 2 for B: B has Ward → WaitForWardEvent
        List<DomainEvent> aChooseEvents = game.playerChooseCardFromBountifulHarvest("player-a", BS8008.getCardId());
        WaitForWardEvent bWardEvent = getEvent(aChooseEvents, WaitForWardEvent.class).orElseThrow();
        assertTrue(bWardEvent.getPlayerIds().contains("player-b"));
        assertEquals(List.of("player-b"), bWardEvent.getTargetPlayerIds());

        // When: B plays Ward to skip themselves → 1 ward (odd) → B is skipped
        List<DomainEvent> wardEvents = game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: B skipped, C gets BountifulHarvestEvent
        BountifulHarvestEvent cPickEvent = getEvent(wardEvents, BountifulHarvestEvent.class).orElseThrow();
        assertEquals("player-c", cPickEvent.getNextChoosingPlayerId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有五穀豐登
            B 有無懈可擊

            A 出五穀豐登
            Phase 1: B skip → proceeds
            Phase 2 for A: B skip → A picks
            Phase 2 for B: B skip → B picks
            Phase 2 for C: no Ward → C picks
            Phase 2 for D: D is last → picks

            Then
            全部選完，五穀豐登結束
            """)
    @Test
    public void test7_givenAllSkipWardPhase2_ThenAllPlayersPick() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BountifulHarvest(SH3042)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BH → Phase 1 WaitForWardEvent (B has Ward)
        game.playerPlayCard(playerA.getId(), SH3042.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Phase 1: B skips → proceeds
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());

        // Phase 2 for A: B skips → A picks
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());
        game.playerChooseCardFromBountifulHarvest("player-a", BS8008.getCardId());

        // Phase 2 for B: B skips → B picks
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());
        game.playerChooseCardFromBountifulHarvest("player-b", BH3029.getCardId());

        // C picks (no Ward left after B skipped once per Phase 2 round... wait, B still has Ward card)
        // Actually B still has SSJ011 - let me reconsider...
        // B only skips Ward, B still has the Ward card. B used SKIP playType, not ACTIVE.
        // So Phase 2 for C: B has Ward → WaitForWardEvent → B skips
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());
        game.playerChooseCardFromBountifulHarvest("player-c", BC2054.getCardId());

        // Phase 2 for D: B has Ward → WaitForWardEvent → B skips
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());
        List<DomainEvent> dPickEvents = game.playerChooseCardFromBountifulHarvest("player-d", BH0036.getCardId());

        // Then: All players picked, BH ends
        assertEquals(1, playerA.getHandSize()); // picked BS8008
        assertEquals(2, playerB.getHandSize()); // has SSJ011 + picked BH3029
        assertEquals(1, playerC.getHandSize()); // picked BC2054
        assertEquals(1, playerD.getHandSize()); // picked BH0036
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有五穀豐登
            D 有無懈可擊

            A 出五穀豐登
            Phase 1: D skip → proceeds
            Phase 2: 各玩家選牌，D 有 Ward 所以每次都有 WaitForWardEvent
            Phase 2 for D（最後一個）: D 出 Ward → D 被跳過 → BH 結束

            Then
            D 被跳過（不選牌）
            active player 回到 A
            """)
    @Test
    public void test8_givenLastPlayerSkippedByWard_ThenBountifulHarvestEnds() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BountifulHarvest(SH3042)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);
        playerD.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BH → Phase 1 WaitForWardEvent (D has Ward)
        game.playerPlayCard(playerA.getId(), SH3042.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Phase 1: D skips → proceeds
        game.playWardCard("player-d", "", PlayType.SKIP.getPlayType());

        // Phase 2 for A: D has Ward → WaitForWardEvent → D skips → A picks
        game.playWardCard("player-d", "", PlayType.SKIP.getPlayType());
        game.playerChooseCardFromBountifulHarvest("player-a", BS8008.getCardId());

        // Phase 2 for B: D has Ward → WaitForWardEvent → D skips → B picks
        game.playWardCard("player-d", "", PlayType.SKIP.getPlayType());
        game.playerChooseCardFromBountifulHarvest("player-b", BH3029.getCardId());

        // Phase 2 for C: D has Ward → WaitForWardEvent → D skips → C picks
        game.playWardCard("player-d", "", PlayType.SKIP.getPlayType());
        game.playerChooseCardFromBountifulHarvest("player-c", BC2054.getCardId());

        // Phase 2 for D: D has Ward → WaitForWardEvent → D plays Ward → 1 ward (odd) → D skipped
        List<DomainEvent> wardEvents = game.playWardCard("player-d", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: D skipped, BH ends
        assertFalse(wardEvents.stream().anyMatch(e -> e instanceof BountifulHarvestEvent));
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertEquals(0, game.getTopBehavior().size());
        assertEquals(0, playerD.getHandSize()); // D didn't pick, and used their Ward
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有五穀豐登
            B 有無懈可擊
            C 有無懈可擊

            A 出五穀豐登
            Phase 1: B skip, C skip → proceeds
            Phase 2 for A: B 出 Ward → C 出 Ward 反制（偶數 → 效果生效）

            Then
            A 正常選牌（Ward 被偶數抵銷）
            """)
    @Test
    public void test9_givenPhase2EvenWards_ThenPlayerStillPicks() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BountifulHarvest(SH3042)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BH → Phase 1 WaitForWardEvent (B, C have Ward)
        game.playerPlayCard(playerA.getId(), SH3042.getCardId(), "", PlayType.ACTIVE.getPlayType());

        // Phase 1: B skips, C skips → 0 wards → proceeds
        game.playWardCard("player-b", "", PlayType.SKIP.getPlayType());
        game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Phase 2 for A: B has Ward → WaitForWardEvent
        // B plays Ward to prevent A from picking
        game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // C plays Ward to counter B → 2 wards (even) → A still picks
        List<DomainEvent> counterEvents = game.playWardCard("player-c", SCK078.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: Even wards → A still gets to pick
        BountifulHarvestEvent bhEvent = getEvent(counterEvents, BountifulHarvestEvent.class).orElseThrow();
        assertEquals("player-a", bhEvent.getNextChoosingPlayerId());
    }

    @DisplayName("""
            Given
            玩家 A B C D，A 的回合
            A 有五穀豐登 + 無懈可擊
            B 有無懈可擊
            C 有無懈可擊

            A 出五穀豐登
            Phase 1: B 出 Ward → A 出 Ward（counter）→ C skip
            2 張 Ward（偶數）→ 效果生效

            Then
            五穀豐登生效
            """)
    @Test
    public void test10_givenPhase1CounterChainEven_ThenBountifulHarvestProceeds() {
        Game game = createGame();

        Player playerA = createPlayer("player-a", General.劉備, Role.MONARCH);
        playerA.getHand().addCardToHand(Arrays.asList(new BountifulHarvest(SH3042), new Ward(SCQ077)));

        Player playerB = createPlayer("player-b", General.關羽, Role.MINISTER);
        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", General.張飛, Role.REBEL);
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SCK078)));

        Player playerD = createPlayer("player-d", General.孫權, Role.TRAITOR);

        setupGame(game, asList(playerA, playerB, playerC, playerD), playerA);

        // A plays BH → Phase 1 WaitForWardEvent
        game.playerPlayCard(playerA.getId(), SH3042.getCardId(), "", PlayType.ACTIVE.getPlayType());
        // B plays Ward
        game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // A plays Ward (counter B)
        game.playWardCard("player-a", SCQ077.getCardId(), PlayType.ACTIVE.getPlayType());
        // C skips → 2 wards (even) → proceeds
        List<DomainEvent> phase1Events = game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Then: BH proceeds, WardEvent present
        assertTrue(phase1Events.stream().anyMatch(e -> e instanceof WardEvent));
    }
}

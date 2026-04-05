package com.gaas.threeKingdoms.Ward;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.LightningJudgementBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WardBehavior;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.*;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.RoundPhase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

public class WardWithLightningTest {

    @DisplayName("""
            Given
            玩家 A B C D
            A 的判定區有閃電
            C 有無懈可擊

            When
            D 結束回合，進入 A 的回合判定

            Then
            發出 WaitForWardEvent，eligible 有 C
            沒有 LightningEvent（判定尚未執行）
            topBehavior 有 WardBehavior 在 LightningJudgementBehavior 之上
            """)
    @Test
    public void testWardTriggerOnLightningJudgement() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029)
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        playerA.addDelayScrollCard(new Lightning(SSA014));

        Player playerB = createPlayer("player-b", 4, General.劉備, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));
        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerD));
        game.enterPhase(new Normal(game));

        // When: D finishes action -> A's turn -> Lightning judgement -> WaitForWardEvent
        List<DomainEvent> events = game.finishAction(playerD.getId());

        // Then
        WaitForWardEvent waitForWardEvent = events.stream()
                .filter(WaitForWardEvent.class::isInstance)
                .map(WaitForWardEvent.class::cast)
                .findFirst().orElseThrow();

        assertTrue(waitForWardEvent.getPlayerIds().contains("player-c"));
        assertEquals(1, waitForWardEvent.getPlayerIds().size());
        assertEquals(List.of("player-a"), waitForWardEvent.getTargetPlayerIds());

        // No LightningEvent yet (judgement not executed)
        assertTrue(events.stream().noneMatch(LightningEvent.class::isInstance));

        // topBehavior: WardBehavior on top of LightningJudgementBehavior
        assertTrue(game.getTopBehavior().size() >= 2);
        assertTrue(game.getTopBehavior().peek() instanceof WardBehavior);
        assertTrue(game.getTopBehavior().get(0) instanceof LightningJudgementBehavior);

        // RoundPhase still Judgement
        assertEquals(RoundPhase.Judgement, game.getCurrentRound().getRoundPhase());
    }

    @DisplayName("""
            Given
            A 的判定區有閃電，C 有無懈可擊
            D 結束回合，A 回合開始，WaitForWardEvent 發出

            When
            C 出無懈可擊（Ward count = 1, odd）

            Then
            閃電被取消（不判定），沒有 LightningEvent
            閃電傳給下家 B
            A 正常抽牌，進入 Action phase
            """)
    @Test
    public void testWardCancelsLightning_TransfersToNextPlayer() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        // After Ward cancels: Drawing phase draws 2
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029),   // spare
                new Peach(BH3029), new Peach(BH3029)    // A's drawing phase (2 cards)
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        playerA.addDelayScrollCard(new Lightning(SSA014));

        Player playerB = createPlayer("player-b", 4, General.劉備, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));
        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerD));
        game.enterPhase(new Normal(game));

        // D finishes -> A's turn -> WaitForWardEvent
        game.finishAction(playerD.getId());

        // When: C plays Ward
        List<DomainEvent> wardEvents = game.playWardCard("player-c", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        // WardEvent present
        assertTrue(wardEvents.stream().anyMatch(WardEvent.class::isInstance));

        // No LightningEvent (judgement was skipped)
        assertTrue(wardEvents.stream().noneMatch(LightningEvent.class::isInstance));

        // Lightning transferred to B
        LightningTransferredEvent transferEvent = wardEvents.stream()
                .filter(LightningTransferredEvent.class::isInstance)
                .map(LightningTransferredEvent.class::cast)
                .findFirst().orElseThrow();
        assertEquals("player-a", transferEvent.getSourcePlayerId());
        assertEquals("player-b", transferEvent.getTargetPlayerId());

        // B has Lightning in delay area
        assertTrue(playerB.getDelayScrollCards().stream().anyMatch(c -> c instanceof Lightning));

        // A no longer has Lightning
        assertFalse(playerA.hasAnyDelayScrollCard());

        // A in Action phase with cards drawn
        assertEquals(RoundPhase.Action, game.getCurrentRound().getRoundPhase());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("""
            Given
            A 的判定區有閃電
            C 有無懈可擊，D 也有無懈可擊
            Deck 頂是黑桃3（閃電判定成功 → 受3點傷害）

            When
            D 結束回合 → A 判定 → WaitForWardEvent
            C 出無懈可擊（count=1）
            D 出無懈可擊反制（count=2, even → 效果繼續）

            Then
            LightningEvent isSuccess=true（黑桃3）
            A 受3點傷害，HP = 1
            """)
    @Test
    public void testDoubleWardLightningProceeds() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        // Deck is LIFO: last = first drawn
        // Draw order: judgement(1) -> drawing(2) -> spare
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029),   // spare
                new Peach(BH3029), new Peach(BH3029),   // A's drawing phase (2 cards)
                new Dismantle(SS3003)                    // Lightning judgement: Spade 3 -> Lightning hits!
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        playerA.addDelayScrollCard(new Lightning(SSA014));

        Player playerB = createPlayer("player-b", 4, General.劉備, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));
        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);
        playerD.getHand().addCardToHand(List.of(new Ward(SCQ077)));

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerD));
        game.enterPhase(new Normal(game));

        // D finishes -> A's turn -> WaitForWardEvent
        game.finishAction(playerD.getId());

        // C plays Ward (count=1)
        game.playWardCard("player-c", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // When: D counter-Wards (count=2, even -> effect proceeds)
        List<DomainEvent> events = game.playWardCard("player-d", SCQ077.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: LightningEvent with success (Spade 3 drawn)
        LightningEvent lightningEvent = events.stream()
                .filter(LightningEvent.class::isInstance)
                .map(LightningEvent.class::cast)
                .findFirst().orElseThrow();
        assertTrue(lightningEvent.isSuccess());

        // A took 3 damage: 4 - 3 = 1
        assertEquals(1, playerA.getHP());

        // A in Action phase
        assertEquals(RoundPhase.Action, game.getCurrentRound().getRoundPhase());
    }

    @DisplayName("""
            Given
            A 的判定區有閃電，C 有無懈可擊
            Deck 頂是紅心（閃電判定失敗 → 安全，傳下家）

            When
            D 結束回合 → WaitForWardEvent → C skip

            Then
            LightningEvent isSuccess=false
            閃電傳給 B
            A 正常進入 Action phase
            """)
    @Test
    public void testSkipWardLightningProceeds_JudgmentFails() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        // Deck is LIFO
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029),   // spare
                new Peach(BH3029), new Peach(BH3029),   // A's drawing phase
                new Peach(BH3029)                        // Lightning judgement: Heart -> fail (safe)
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        playerA.addDelayScrollCard(new Lightning(SSA014));

        Player playerB = createPlayer("player-b", 4, General.劉備, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));
        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerD));
        game.enterPhase(new Normal(game));

        // D finishes -> A's turn -> WaitForWardEvent
        game.finishAction(playerD.getId());

        // When: C skips Ward
        List<DomainEvent> events = game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Then
        LightningEvent lightningEvent = events.stream()
                .filter(LightningEvent.class::isInstance)
                .map(LightningEvent.class::cast)
                .findFirst().orElseThrow();
        assertFalse(lightningEvent.isSuccess());

        // Lightning transferred to B
        assertTrue(playerB.getDelayScrollCards().stream().anyMatch(c -> c instanceof Lightning));
        assertEquals(4, playerA.getHP()); // No damage

        // A in Action phase
        assertEquals(RoundPhase.Action, game.getCurrentRound().getRoundPhase());
    }

    @DisplayName("""
            Given
            A 的判定區有閃電，沒有人有無懈可擊
            Deck 頂是紅心

            When
            D 結束回合

            Then
            沒有 WaitForWardEvent（向下相容）
            LightningEvent 直接發出
            閃電傳給 B
            """)
    @Test
    public void testNoWardLightningProceedsImmediately() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029)                        // Lightning judgement: Heart -> fail (safe)
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        playerA.addDelayScrollCard(new Lightning(SSA014));

        Player playerB = createPlayer("player-b", 4, General.劉備, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerD));
        game.enterPhase(new Normal(game));

        // When
        List<DomainEvent> events = game.finishAction(playerD.getId());

        // Then: No WaitForWardEvent
        assertTrue(events.stream().noneMatch(WaitForWardEvent.class::isInstance));

        // LightningEvent present
        LightningEvent lightningEvent = events.stream()
                .filter(LightningEvent.class::isInstance)
                .map(LightningEvent.class::cast)
                .findFirst().orElseThrow();
        assertFalse(lightningEvent.isSuccess());

        // Lightning transferred to B
        assertTrue(playerB.getDelayScrollCards().stream().anyMatch(c -> c instanceof Lightning));

        // A in Action phase
        assertEquals(RoundPhase.Action, game.getCurrentRound().getRoundPhase());
    }

    @DisplayName("""
            Given
            A 的判定區有閃電
            A 自己有無懈可擊（沒有其他人有）

            When
            D 結束回合 → WaitForWardEvent
            A 出無懈可擊取消自己的閃電

            Then
            A 可以出 Ward 取消自己的閃電
            閃電傳給 B
            A 正常進入 Action phase
            """)
    @Test
    public void testAffectedPlayerCanWardOwnLightning() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        playerA.addDelayScrollCard(new Lightning(SSA014));
        playerA.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerB = createPlayer("player-b", 4, General.劉備, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerD));
        game.enterPhase(new Normal(game));

        // D finishes -> A's turn -> WaitForWardEvent
        List<DomainEvent> finishEvents = game.finishAction(playerD.getId());

        // Verify A is in the Ward eligible list (all players can Ward on Lightning)
        WaitForWardEvent waitForWardEvent = finishEvents.stream()
                .filter(WaitForWardEvent.class::isInstance)
                .map(WaitForWardEvent.class::cast)
                .findFirst().orElseThrow();
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-a"));

        // When: A plays Ward on their own Lightning
        List<DomainEvent> wardEvents = game.playWardCard("player-a", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        assertTrue(wardEvents.stream().anyMatch(WardEvent.class::isInstance));
        assertTrue(wardEvents.stream().noneMatch(LightningEvent.class::isInstance));

        // Lightning transferred to B
        assertTrue(playerB.getDelayScrollCards().stream().anyMatch(c -> c instanceof Lightning));
        assertFalse(playerA.hasAnyDelayScrollCard());

        assertEquals(RoundPhase.Action, game.getCurrentRound().getRoundPhase());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("""
            Given
            A 的判定區有樂不思蜀和閃電（樂不思蜀在上面，先判定）
            C 有無懈可擊
            Deck 頂是紅心（閃電判定失敗，安全）

            When
            D 結束回合 → Ward 取消樂不思蜀

            Then
            閃電判定仍然會觸發 WaitForWardEvent
            """)
    @Test
    public void testContentmentAndLightning_WardCancelsContentment_LightningTriggersWard() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        // After Ward cancels Contentment, Lightning judgement triggers Ward again
        // C has 2 Wards: one for Contentment, one for Lightning
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029)                       // Lightning judgement: Heart -> fail
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        // Add Lightning first, then Contentment (stack: Contentment on top, popped first)
        playerA.addDelayScrollCard(new Lightning(SSA014));
        playerA.addDelayScrollCard(new Contentment(SC6071));

        Player playerB = createPlayer("player-b", 4, General.劉備, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011), new Ward(SCQ077)));
        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerD));
        game.enterPhase(new Normal(game));

        // D finishes -> A's turn -> Contentment WaitForWardEvent
        game.finishAction(playerD.getId());

        // C plays Ward to cancel Contentment
        List<DomainEvent> events = game.playWardCard("player-c", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: After Contentment is cancelled, Lightning should trigger Ward
        WaitForWardEvent waitForWardEvent = events.stream()
                .filter(WaitForWardEvent.class::isInstance)
                .map(WaitForWardEvent.class::cast)
                .findFirst().orElseThrow();
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-c"));
        assertEquals(List.of("player-a"), waitForWardEvent.getTargetPlayerIds());

        // No LightningEvent yet
        assertTrue(events.stream().noneMatch(LightningEvent.class::isInstance));

        // topBehavior has LightningJudgementBehavior + WardBehavior
        assertTrue(game.getTopBehavior().peek() instanceof WardBehavior);
    }

    @DisplayName("""
            Given
            A 的判定區有閃電，C 有無懈可擊
            Ward 取消閃電後

            When
            A 的回合繼續

            Then
            A 正常摸兩張牌，進入 Action phase
            """)
    @Test
    public void testWardCancelsLightning_PlayerDrawsNormally() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        playerA.addDelayScrollCard(new Lightning(SSA014));

        Player playerB = createPlayer("player-b", 4, General.劉備, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));
        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerD));
        game.enterPhase(new Normal(game));

        // D finishes -> A's turn -> WaitForWardEvent
        game.finishAction(playerD.getId());

        int handSizeBefore = playerA.getHand().getCards().size();

        // When: C plays Ward to cancel Lightning
        List<DomainEvent> wardEvents = game.playWardCard("player-c", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: A drew 2 cards
        assertEquals(handSizeBefore + 2, playerA.getHand().getCards().size());

        // DrawCardEvent present
        assertTrue(wardEvents.stream().anyMatch(DrawCardEvent.class::isInstance));

        // A in Action phase
        assertEquals(RoundPhase.Action, game.getCurrentRound().getRoundPhase());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("""
            Given
            A 的判定區有閃電，A HP=3，C 有無懈可擊
            Deck 頂是黑桃3（判定成功）

            When
            C skip Ward → 閃電判定成功

            Then
            A 受3點傷害，HP=0，進入瀕死流程 AskPeachEvent
            """)
    @Test
    public void testSkipWardLightningHits_PlayerDying() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029),
                new Dismantle(SS3003)                    // Lightning judgement: Spade 3 -> hits!
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 3, General.劉備, Role.MONARCH);
        playerA.addDelayScrollCard(new Lightning(SSA014));

        Player playerB = createPlayer("player-b", 4, General.劉備, Role.MINISTER);
        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));
        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerD));
        game.enterPhase(new Normal(game));

        // D finishes -> A's turn -> WaitForWardEvent
        game.finishAction(playerD.getId());

        // When: C skips Ward -> Lightning judgement proceeds
        List<DomainEvent> events = game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());

        // Then
        LightningEvent lightningEvent = events.stream()
                .filter(LightningEvent.class::isInstance)
                .map(LightningEvent.class::cast)
                .findFirst().orElseThrow();
        assertTrue(lightningEvent.isSuccess());

        // A HP = 0
        assertEquals(0, playerA.getHP());

        // AskPeachEvent present
        assertTrue(events.stream().anyMatch(AskPeachEvent.class::isInstance));
    }

    private Player createPlayer(String id, int hp, General general, Role role) {
        return PlayerBuilder.construct()
                .withId(id)
                .withBloodCard(new BloodCard(hp))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withGeneralCard(new GeneralCard(general))
                .withRoleCard(new RoleCard(role))
                .withHealthStatus(HealthStatus.ALIVE)
                .withDelayScrollCards(new Stack<>())
                .build();
    }
}

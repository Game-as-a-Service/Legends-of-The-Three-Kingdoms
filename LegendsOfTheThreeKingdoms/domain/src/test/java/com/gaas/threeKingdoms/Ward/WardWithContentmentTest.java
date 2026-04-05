package com.gaas.threeKingdoms.Ward;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.ContentmentJudgementBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WardBehavior;
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
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
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

import static com.gaas.threeKingdoms.Utils.getEvent;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

public class WardWithContentmentTest {

    @DisplayName("""
            Given
            玩家 A B C D
            A 的回合，B 的判定區有樂不思蜀
            C 有無懈可擊

            When
            A 結束回合，B 的回合開始

            Then
            發出 WaitForWardEvent，eligible 有 C
            沒有 ContentmentEvent（判定尚未執行）
            topBehavior 有 WardBehavior 在 ContentmentJudgementBehavior 之上
            """)
    @Test
    public void testWardTriggerOnContentmentJudgement() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        // Deck needs enough cards: 2 for B's draw + 1 for judgement + spare
        deck.add(List.of(
                new BarbarianInvasion(SSK013), new BarbarianInvasion(SSK013),
                new BarbarianInvasion(SSK013), new BarbarianInvasion(SSK013),
                new BarbarianInvasion(SSK013)
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        Player playerB = createPlayer("player-b", 4, General.劉備, Role.MINISTER);
        playerB.addDelayScrollCard(new Contentment(SC6071));

        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // When: A finishes action -> triggers B's turn
        List<DomainEvent> events = game.finishAction(playerA.getId());

        // Then
        WaitForWardEvent waitForWardEvent = events.stream()
                .filter(WaitForWardEvent.class::isInstance)
                .map(WaitForWardEvent.class::cast)
                .findFirst().orElseThrow();

        assertTrue(waitForWardEvent.getPlayerIds().contains("player-c"));
        assertEquals(1, waitForWardEvent.getPlayerIds().size());
        assertEquals(List.of("player-b"), waitForWardEvent.getTargetPlayerIds());

        // No ContentmentEvent yet
        assertTrue(events.stream().noneMatch(ContentmentEvent.class::isInstance));

        // topBehavior: WardBehavior on top of ContentmentJudgementBehavior
        assertTrue(game.getTopBehavior().size() >= 2);
        assertTrue(game.getTopBehavior().peek() instanceof WardBehavior);
        assertTrue(game.getTopBehavior().get(0) instanceof ContentmentJudgementBehavior);

        // RoundPhase still Judgement
        assertEquals(RoundPhase.Judgement, game.getCurrentRound().getRoundPhase());
    }

    @DisplayName("""
            Given
            B 的判定區有樂不思蜀，C 有無懈可擊
            A 結束回合，B 回合開始，WaitForWardEvent 發出

            When
            C 出無懈可擊（Ward count = 1, odd）

            Then
            樂不思蜀被取消，沒有 ContentmentEvent
            B 正常抽牌，進入 Action phase
            """)
    @Test
    public void testWardCancelsContentment() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        // Cards for: B's draw (2) + spare
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        Player playerB = createPlayer("player-b", 4, General.劉備, Role.MINISTER);
        playerB.addDelayScrollCard(new Contentment(SC6071));

        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // A finishes action -> B's turn -> WaitForWardEvent
        game.finishAction(playerA.getId());

        // When: C plays Ward
        List<DomainEvent> wardEvents = game.playWardCard("player-c", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        // WardEvent should be present
        assertTrue(wardEvents.stream().anyMatch(WardEvent.class::isInstance));

        // No ContentmentEvent (judgement was skipped)
        assertTrue(wardEvents.stream().noneMatch(ContentmentEvent.class::isInstance));

        // B should be in Action phase with cards drawn
        assertEquals(RoundPhase.Action, game.getCurrentRound().getRoundPhase());

        // B's delay scroll area should be empty
        assertFalse(playerB.hasAnyDelayScrollCard());

        // topBehavior should be empty
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("""
            Given
            B 的判定區有樂不思蜀
            C 有無懈可擊，D 也有無懈可擊
            Deck 頂是黑桃（非紅桃 → 樂不思蜀生效）

            When
            A 結束回合 → B 回合 → WaitForWardEvent
            C 出無懈可擊（count=1）
            D 出無懈可擊反制（count=2, even → 效果繼續）

            Then
            ContentmentEvent isSuccess=true（黑桃，樂不思蜀生效）
            B 進入 Discard phase
            """)
    @Test
    public void testDoubleWardContentmentProceeds() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        // Deck is a Stack (LIFO): last element = first drawn
        // Draw order: judgement(1) -> drawing(2) -> spare
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029),   // spare
                new Peach(BH3029), new Peach(BH3029),   // B's drawing phase (2 cards)
                new BarbarianInvasion(SSK013)            // judgement card: Spade -> Contentment succeeds
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        Player playerB = createPlayer("player-b", 1, General.劉備, Role.MINISTER);
        playerB.getHand().addCardToHand(List.of(new Peach(BH3029), new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)));
        playerB.addDelayScrollCard(new Contentment(SC6071));

        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);
        playerD.getHand().addCardToHand(List.of(new Ward(SCQ077)));

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // A finishes -> B's turn -> WaitForWardEvent
        game.finishAction(playerA.getId());

        // C plays Ward (count=1)
        game.playWardCard("player-c", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // When: D counter-Wards (count=2, even -> effect proceeds)
        List<DomainEvent> events = game.playWardCard("player-d", SCQ077.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: ContentmentEvent with success (Spade drawn)
        ContentmentEvent contentmentEvent = events.stream()
                .filter(ContentmentEvent.class::isInstance)
                .map(ContentmentEvent.class::cast)
                .findFirst().orElseThrow();
        assertTrue(contentmentEvent.isSuccess());

        // B enters Discard phase (skips Action)
        assertEquals(RoundPhase.Discard, game.getCurrentRound().getRoundPhase());
    }

    @DisplayName("""
            Given
            B 的判定區有樂不思蜀
            C 和 D 都有無懈可擊
            Deck 頂是黑桃

            When
            A 結束回合 → WaitForWardEvent
            C skip，D skip

            Then
            ContentmentEvent 被發出（判定執行）
            isSuccess = true（黑桃）
            B 進入 Discard phase
            """)
    @Test
    public void testAllPlayersSkipWard() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        // Deck is LIFO: last = first drawn
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029),   // spare
                new Peach(BH3029), new Peach(BH3029),   // B's drawing phase
                new BarbarianInvasion(SSK013)            // judgement card: Spade -> Contentment succeeds
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        Player playerB = createPlayer("player-b", 1, General.劉備, Role.MINISTER);
        playerB.getHand().addCardToHand(List.of(new Peach(BH3029), new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)));
        playerB.addDelayScrollCard(new Contentment(SC6071));

        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);
        playerD.getHand().addCardToHand(List.of(new Ward(SCQ077)));

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        game.finishAction(playerA.getId());

        // When: C and D both skip
        game.playWardCard("player-c", "", PlayType.SKIP.getPlayType());
        List<DomainEvent> events = game.playWardCard("player-d", "", PlayType.SKIP.getPlayType());

        // Then: ContentmentEvent present
        ContentmentEvent contentmentEvent = events.stream()
                .filter(ContentmentEvent.class::isInstance)
                .map(ContentmentEvent.class::cast)
                .findFirst().orElseThrow();
        assertTrue(contentmentEvent.isSuccess()); // Spade = Contentment succeeds

        assertEquals(RoundPhase.Discard, game.getCurrentRound().getRoundPhase());
    }

    @DisplayName("""
            Given
            B 的判定區有樂不思蜀和閃電
            C 有無懈可擊

            When
            Ward 取消樂不思蜀

            Then
            閃電判定仍然執行
            """)
    @Test
    public void testWardCancelsContentmentButLightningStillProcesses() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        // Deck is LIFO: last = first drawn
        // After Ward cancels Contentment, Lightning judgement draws 1, then Drawing phase draws 2
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029),   // spare
                new Peach(BH3029), new Peach(BH3029),   // B's drawing phase
                new Peach(BH3029)                        // Lightning judgement: Heart -> Lightning fails (safe)
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        Player playerB = createPlayer("player-b", 4, General.劉備, Role.MINISTER);
        // Add Lightning first, then Contentment (stack: Contentment on top)
        playerB.addDelayScrollCard(new Lightning(SSA014));
        playerB.addDelayScrollCard(new Contentment(SC6071));

        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // A finishes -> B's turn -> WaitForWardEvent for Contentment
        game.finishAction(playerA.getId());

        // When: C plays Ward to cancel Contentment
        List<DomainEvent> events = game.playWardCard("player-c", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then: No ContentmentEvent (cancelled)
        assertTrue(events.stream().noneMatch(ContentmentEvent.class::isInstance));

        // Lightning should still process
        assertTrue(events.stream().anyMatch(LightningEvent.class::isInstance));

        // B should be in Action phase (Lightning didn't kill, Contentment cancelled)
        assertEquals(RoundPhase.Action, game.getCurrentRound().getRoundPhase());
    }

    @DisplayName("""
            Given
            B 的判定區有樂不思蜀
            沒有任何玩家有無懈可擊
            Deck 頂是黑桃（樂不思蜀生效）

            When
            A 結束回合

            Then
            沒有 WaitForWardEvent
            ContentmentEvent 直接發出
            向下相容：行為與原本一致
            """)
    @Test
    public void testNoWardContentmentProceedsImmediately() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        // Deck is LIFO: last = first drawn
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029),   // spare
                new Peach(BH3029), new Peach(BH3029),   // B's drawing phase
                new BarbarianInvasion(SSK013)            // judgement card: Spade -> Contentment succeeds
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        Player playerB = createPlayer("player-b", 1, General.劉備, Role.MINISTER);
        playerB.getHand().addCardToHand(List.of(new Peach(BH3029), new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)));
        playerB.addDelayScrollCard(new Contentment(SC6071));

        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // When
        List<DomainEvent> events = game.finishAction(playerA.getId());

        // Then: No WaitForWardEvent
        assertTrue(events.stream().noneMatch(WaitForWardEvent.class::isInstance));

        // ContentmentEvent present
        ContentmentEvent contentmentEvent = events.stream()
                .filter(ContentmentEvent.class::isInstance)
                .map(ContentmentEvent.class::cast)
                .findFirst().orElseThrow();
        assertTrue(contentmentEvent.isSuccess()); // Spade = success

        // B enters Discard phase
        assertEquals(RoundPhase.Discard, game.getCurrentRound().getRoundPhase());
    }

    @DisplayName("""
            Given
            B 的判定區有樂不思蜀
            B 自己有無懈可擊（沒有其他人有）

            When
            A 結束回合 → WaitForWardEvent
            B 出無懈可擊取消自己的樂不思蜀

            Then
            B 可以取消自己的樂不思蜀
            B 正常進入 Action phase
            """)
    @Test
    public void testAffectedPlayerCanWardOwnContentment() {
        // Given
        Game game = new Game();
        Deck deck = new Deck();
        deck.add(List.of(
                new Peach(BH3029), new Peach(BH3029),
                new Peach(BH3029), new Peach(BH3029), new Peach(BH3029)
        ));
        game.setDeck(deck);

        Player playerA = createPlayer("player-a", 4, General.劉備, Role.MONARCH);
        Player playerB = createPlayer("player-b", 4, General.劉備, Role.MINISTER);
        playerB.addDelayScrollCard(new Contentment(SC6071));
        playerB.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerC = createPlayer("player-c", 4, General.劉備, Role.REBEL);
        Player playerD = createPlayer("player-d", 4, General.劉備, Role.TRAITOR);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // A finishes -> B's turn -> WaitForWardEvent
        List<DomainEvent> finishEvents = game.finishAction(playerA.getId());

        // Verify B is in the Ward eligible list
        WaitForWardEvent waitForWardEvent = finishEvents.stream()
                .filter(WaitForWardEvent.class::isInstance)
                .map(WaitForWardEvent.class::cast)
                .findFirst().orElseThrow();
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-b"));

        // When: B plays Ward on their own Contentment
        List<DomainEvent> wardEvents = game.playWardCard("player-b", SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        assertTrue(wardEvents.stream().anyMatch(WardEvent.class::isInstance));
        assertTrue(wardEvents.stream().noneMatch(ContentmentEvent.class::isInstance));
        assertEquals(RoundPhase.Action, game.getCurrentRound().getRoundPhase());
        assertFalse(playerB.hasAnyDelayScrollCard());
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

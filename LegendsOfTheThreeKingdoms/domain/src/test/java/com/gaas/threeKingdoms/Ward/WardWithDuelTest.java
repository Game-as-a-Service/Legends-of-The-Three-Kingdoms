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
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.gaas.threeKingdoms.Utils.getEvent;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class WardWithDuelTest {

    @DisplayName("""
            Given
            有 玩家 A B C D
            A 有決鬥
            B 有無懈可擊
            
            When
            A 出決鬥
            
            Then
            A B C D 等待發動無懈可擊 的 event
            event 裡有可以發動無懈可擊 event 的 B
            """)
    @Test
    public void givenPlayerAHasDuelAndPlayerBHasWard_WhenPlayerAPlaysDuel_ThenBReceivesAskPlayWardEventAndABDReceiveWaitForWardEvent() {
        Game game = new Game();
        game.initDeck();
        game.setDeck(new Deck(List.of(new BarbarianInvasion(SSK013), new Dismantle(SS3003))));

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        //Then
        WaitForWardEvent waitForWardEvents = events.stream()
                .filter(WaitForWardEvent.class::isInstance)
                .map(WaitForWardEvent.class::cast)
                .findFirst().get();
        assertTrue(waitForWardEvents.getPlayerIds().contains("player-b"));
        assertEquals(1, waitForWardEvents.getPlayerIds().size());
    }

    @DisplayName("""
            Given
            有 玩家 A B C D
            A 有決鬥、無懈可擊
            B 有無懈可擊
            C 有無懈可擊
            
            When
            A 出決鬥
            
            Then
            A B C D 等待發動無懈可擊 的 event
            event 裡有可以發動無懈可擊 event 的 B C，但沒有出過河拆橋的 A
            """)
    @Test
    public void givenABCHaveWard_WhenAPlaysDuel_ThenABCAreAskedToPlayWardAndDWaits() {
        Game game = new Game();
        game.initDeck();
        game.setDeck(new Deck(List.of(new BarbarianInvasion(SSK013), new Dismantle(SS3003))));

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(List.of(
                new Duel(SSA001),
                new Ward(SSJ011)
        ));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerC.getHand().addCardToHand(List.of(new Ward(SSJ011)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = List.of(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // Then
        WaitForWardEvent waitForWardEvents = events.stream()
                .filter(WaitForWardEvent.class::isInstance)
                .map(WaitForWardEvent.class::cast)
                .findFirst().get();
        assertFalse(waitForWardEvents.getPlayerIds().contains("player-a"));
        assertTrue(waitForWardEvents.getPlayerIds().contains("player-b"));
        assertTrue(waitForWardEvents.getPlayerIds().contains("player-c"));
        assertEquals(2, waitForWardEvents.getPlayerIds().size());
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有決鬥
               B 有無懈可擊
            
               A 出決鬥
               B 收到是否要發動無懈可擊的 event
               A C D 收到等待別人發動無懈可擊 的 event
            
               When
               B 出無懈可擊
            
               Then
               A B C D 收到 event 無懈可擊 抵銷了 決鬥
            """)
    @Test
    public void givenPlayerAHasDuelAndPlayerBHasWard_WhenPlayerAPlaysDuelAndPlayerBPlaysWard_ThenABCDReceive() {
        Game game = new Game();
        game.initDeck();
        game.setDeck(new Deck(List.of(new BarbarianInvasion(SSK013), new Dismantle(SS3003))));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        //When
        List<DomainEvent> events = game.playWardCard(playerB.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        //Then
        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-b", wardEvent.getPlayerId());
        assertEquals("SSA001", wardEvent.getCardId());
        assertEquals("SSJ011", wardEvent.getWardCardId());
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有決鬥
               B 有無懈可擊
               C 有無懈可擊
            
               A 出決鬥
            
               When
               B 出無懈可擊
            
               Then
               A B C D 收到 PlayCard Event  
               C 收到是否要發動無懈可擊的 event
            """)
    @Test
    public void givenPlayerAHasDuelAndPlayerBAndCHasWard_WhenPlayerAPlaysDuelAndPlayerBPlaysWard_ThenABCDReceive() {
        Game game = new Game();
        game.initDeck();
        game.setDeck(new Deck(List.of(new BarbarianInvasion(SSK013), new Dismantle(SS3003))));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        //When
        List<DomainEvent> events = game.playWardCard(playerB.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        //Then
        assertFalse(events.stream().anyMatch(event -> event instanceof WardEvent));

        PlayCardEvent playCardEvent = getEvent(events, PlayCardEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-b", playCardEvent.getPlayerId());
        assertEquals("SSJ011", playCardEvent.getCardId());
        WaitForWardEvent waitForWardEvent = getEvent(events, WaitForWardEvent.class).orElseThrow(RuntimeException::new);
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-c"));
        assertEquals(1, waitForWardEvent.getPlayerIds().size());
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有決鬥
               B 有無懈可擊
               C 有無懈可擊
            
               A 出決鬥
               B 出無懈可擊
            
               When
               C 出無懈可擊
            
               Then
               A B C D 收到 event >> C 的無懈可擊抵銷了 B 的無懈可擊
               A B C D 收到 A 決鬥發動 的 event
            """)
    @Test
    public void givenPlayerAHasDuelAndPlayerBAndCHasWard_WhenPlayerAPlaysDuelAndPlayerBAndCPlaysWard_ThenABCDReceive() {
        Game game = new Game();
        game.initDeck();
        game.setDeck(new Deck(List.of(new BarbarianInvasion(SSK013), new Dismantle(SS3003))));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出決鬥
        game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        // B 出無懈可擊
        game.playWardCard(playerB.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        //When
        // C 發動無懈可擊
        List<DomainEvent> events = game.playWardCard(playerC.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-c", wardEvent.getPlayerId());
        assertEquals("SSJ011", wardEvent.getCardId());
        assertEquals("SSJ011", wardEvent.getWardCardId());
        DuelEvent duelEvent = getEvent(events, DuelEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-a", duelEvent.getDuelPlayerId());
        assertEquals(game.getTopBehavior(), List.of());
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有決鬥
               A 有無懈可擊
               B 有無懈可擊
               C 有無懈可擊
            
               A 出決鬥
               B 出無懈可擊
               A 出無懈可擊
            
               When
               C 出無懈可擊
            
               Then
               A B C D 收到 event >> C 的無懈可擊抵銷了 A 的無懈可擊
               A B C D 收到 event >> B 的無懈可擊抵銷了 A 的決鬥
               A B C D 不會收到決鬥發動 的 event
            """)
    @Test
    public void givenPlayerAHasDuelAndPlayerAAndBAndCHasWard_WhenPlayerAPlaysDuelAndPlayerAAndBAndCPlaysWard_ThenABCDReceive() {
        Game game = new Game();
        game.initDeck();
        game.setDeck(new Deck(List.of(new BarbarianInvasion(SSK013), new Dismantle(SS3003))));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Duel(SSA001), new Ward(SSJ011)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出決鬥
        List<DomainEvent> event1 = game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        // B 出無懈可擊
        List<DomainEvent> event2 = game.playWardCard(playerB.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // A 出無懈可擊
        List<DomainEvent> event3 = game.playWardCard(playerA.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        //When
        List<DomainEvent> events = game.playWardCard(playerC.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        //Then
        List<WardEvent> wardEvents = events.stream()
                .filter(e -> e instanceof WardEvent)
                .map(e -> (WardEvent) e)
                .toList();

        assertEquals(2, wardEvents.size(), "應該有兩個 WardEvent");

        WardEvent wardEventC = wardEvents.stream()
                .filter(e -> e.getPlayerId().equals("player-c"))
                .findFirst().orElseThrow(() -> new AssertionError("找不到 player-c 的 WardEvent"));
        assertEquals("SSJ011", wardEventC.getCardId());
        assertEquals("SSJ011", wardEventC.getWardCardId());

        WardEvent wardEventB = wardEvents.stream()
                .filter(e -> e.getPlayerId().equals("player-b"))
                .findFirst().orElseThrow(() -> new AssertionError("找不到 player-b 的 WardEvent"));
        assertEquals("SSA001", wardEventB.getCardId());
        assertEquals("SSJ011", wardEventB.getWardCardId());

        assertFalse(events.stream().anyMatch(event -> event instanceof DuelEvent));
        assertEquals(0, game.getTopBehavior().size());
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有決鬥
               A 有無懈可擊
               B 有無懈可擊
               C 有無懈可擊
            
               A 出決鬥
               B 出 skip 無懈可擊
               A 出 skip 無懈可擊
            
               When
               C 出 skip 無懈可擊
            
               Then
               A B C D 收到決鬥發動 的 event
            """)
    @Test
    public void givenPlayerAHasDuelAndPlayerAAndBAndCHasWard_WhenPlayerAPlaysDuelAndPlayerAAndCSkipPlaysWard_ThenABCDReceive() {
        Game game = new Game();
        game.initDeck();
        game.setDeck(new Deck(List.of(new BarbarianInvasion(SSK013), new Dismantle(SS3003))));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Duel(SSA001), new Ward(SSJ011)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出決鬥
        List<DomainEvent> event1 = game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        // B skip 無懈可擊
        List<DomainEvent> event2 = game.playWardCard(playerB.getId(), "", PlayType.SKIP.getPlayType());
        // A skip 無懈可擊
        List<DomainEvent> event3 = game.playWardCard(playerA.getId(), "", PlayType.SKIP.getPlayType());

        //When
        List<DomainEvent> events = game.playWardCard(playerC.getId(), "", PlayType.SKIP.getPlayType());

        //Then
        assertFalse(events.stream().anyMatch(event -> event instanceof WardEvent));
        assertTrue(events.stream().anyMatch(event -> event instanceof DuelEvent));
        assertEquals(game.getTopBehavior(), List.of());
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有決鬥
               A 有無懈可擊
               B 有無懈可擊
               C 有無懈可擊
            
               A 出 決鬥
               B 出 無懈可擊
               A 出 無懈可擊
            
               When
               C 出 skip 無懈可擊
            
               Then
               A B C D 收到 event >> A 的無懈可擊 抵銷了 B 的無懈可擊 
               A B C D 收到 event >> A 決鬥發動 的 event
            """)
    @Test
    public void givenPlayerAHasDuelAndPlayerAAndBAndCHasWard_WhenPlayerAPlaysDuelAndPlayerAAndBAndCSkipPlaysWard_ThenABCDReceive() {
        Game game = new Game();
        game.initDeck();
        game.setDeck(new Deck(List.of(new BarbarianInvasion(SSK013), new Dismantle(SS3003))));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Duel(SSA001), new Ward(SSJ011)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出決鬥
        List<DomainEvent> event1 = game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        // B 出無懈可擊
        List<DomainEvent> event2 = game.playWardCard(playerB.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // A 出無懈可擊
        List<DomainEvent> event3 = game.playWardCard(playerA.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        //When
        List<DomainEvent> events = game.playWardCard(playerC.getId(), "", PlayType.SKIP.getPlayType());

        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof DuelEvent));
        assertTrue(events.stream().anyMatch(event -> event instanceof WardEvent));
        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-a", wardEvent.getPlayerId());
        assertEquals("SSJ011", wardEvent.getCardId());
        assertEquals("SSJ011", wardEvent.getWardCardId());
        assertEquals(game.getTopBehavior(), List.of());
    }

    @DisplayName("""
            Given
            玩家 A, B, C, D
            A 有決鬥卡
            B 有無懈可擊卡
            A 是當前回合玩家

            When
            A 對 B 出決鬥
            B 對 A 的決鬥出無懈可擊
            (假設遊戲依序詢問 C, D, A 是否對 B 的無懈可擊出牌)
            C skip 對 B 的無懈可擊出無懈可擊

            Then
            產生 B 的無懈可擊抵銷了 A 的決鬥的事件
            A 的決鬥沒有生效 (沒有產生後續的 Duel 效果事件，如要求出殺的 AskForCardEvent)
            A 仍然是 Active Player (當前回合玩家)
            """)
    @Test
    public void testPlayerADuelsPlayerB_PlayerBWardsSuccessfullyAndOthersSkip_ActivePlayerRemainsPlayerA() {
        // Given
        Game game = new Game();
        game.initDeck(); // Consistent with other tests, though cards are manually added.

        Player playerA = PlayerBuilder.construct().withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        Player playerB = PlayerBuilder.construct().withId("player-b")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.關羽)) // Using a different general for B
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        Player playerC = PlayerBuilder.construct().withId("player-c")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.曹操))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();


        Player playerD = PlayerBuilder.construct().withId("player-d")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.孫權))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Duel duelCard = new Duel(SSA001); // SSA001 is 決鬥
        Ward wardCardForB = new Ward(SSJ011); // SSJ011 is 無懈可擊

        playerA.getHand().addCardToHand(List.of(duelCard));
        playerB.getHand().addCardToHand(List.of(wardCardForB));
        playerC.getHand().addCardToHand(Arrays.asList(new Ward(SSJ011)));

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA)); // Sets current player to A
        game.enterPhase(new Normal(game)); // Ensure the game is in a phase where cards can be played

        game.playerPlayCard(playerA.getId(), duelCard.getId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        game.playWardCard(playerB.getId(), wardCardForB.getId(), PlayType.ACTIVE.getPlayType());

        List<DomainEvent> events = game.playWardCard(playerC.getId(), "", PlayType.SKIP.getPlayType());

        // Then
        // Verify that Player A is still the active player.
        assertEquals(playerA.getId(), game.getActivePlayer().getId(), "Player A should remain the active player.");
        assertEquals(playerA.getId(), game.getCurrentRound().getCurrentRoundPlayer().getId(), "Player A should remain the current player in the round.");
        assertEquals(game.getTopBehavior(), List.of());
    }


    @DisplayName("""
            Given
            玩家 A, B, C, D
            A 有決鬥卡
            A B 有無懈可擊卡各一
            B 的血量為 4
            A 是當前回合玩家

            When
            A 對 B 出決鬥
            B 對 A 的決鬥出無懈可擊
            (假設遊戲依序詢問 C, D, A 是否對 B 的無懈可擊出牌)
            A 對 B 的無懈可擊出無懈可擊

            Then
            產生 A 的無懈可擊抵銷了 B 的決鬥的事件
            
            A 的決鬥生效
            B 的血量為 3
            A 仍然是 Active Player (當前回合玩家)
            """)
    @Test
    public void testPlayerADuelsPlayerB_PlayerADuelSuccessfullyAndOthersSkip_ActivePlayerRemainsPlayerA() {
        // Given
        Game game = new Game();
        game.initDeck(); // Consistent with other tests, though cards are manually added.

        Player playerA = PlayerBuilder.construct().withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        Player playerB = PlayerBuilder.construct().withId("player-b")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.關羽)) // Using a different general for B
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        Player playerC = PlayerBuilder.construct().withId("player-c")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.曹操))
                .withRoleCard(new RoleCard(Role.REBEL))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct().withId("player-d")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.孫權))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Duel duelCard = new Duel(SSA001); // SSA001 is 決鬥
        Ward wardCardForB = new Ward(SSJ011); // SSJ011 is 無懈可擊

        playerA.getHand().addCardToHand(List.of(duelCard, new Ward(SSJ011)));
        playerB.getHand().addCardToHand(List.of(wardCardForB));

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA)); // Sets current player to A
        game.enterPhase(new Normal(game)); // Ensure the game is in a phase where cards can be played

        game.playerPlayCard(playerA.getId(), duelCard.getId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        game.playWardCard(playerB.getId(), wardCardForB.getId(), PlayType.ACTIVE.getPlayType());

        List<DomainEvent> events = game.playWardCard(playerA.getId(), wardCardForB.getId(), PlayType.ACTIVE.getPlayType());
        // Then
        // Verify that Player A is still the active player.
        assertEquals(playerA.getId(), game.getActivePlayer().getId(), "Player A should remain the active player.");
        assertEquals(playerA.getId(), game.getCurrentRound().getCurrentRoundPlayer().getId(), "Player A should remain the current player in the round.");
        assertEquals(3, playerB.getBloodCard().getHp());
        assertEquals(game.getTopBehavior(), List.of());
    }

    @DisplayName("""
            Given
            A有兩張無懈可擊 其餘人沒有無懈可擊。
            B對A使用決鬥
            
            When
            A決定使用無懈可擊
            
            Then
            此時系統應該直接讓無懈可擊生效
            (不應該收到 WaitForWardEvent)
            """)
    @Test
    public void givenPlayerAHasTwoWardsAndOthersHaveNone_WhenPlayerBPlaysDuelToAAndAPlaysWard_ThenWardTakesEffectImmediately() {
        Game game = new Game();
        game.initDeck();
        game.setDeck(new Deck(List.of(new BarbarianInvasion(SSK013), new Dismantle(SS3003))));

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        // A 有兩張無懈可擊
        playerA.getHand().addCardToHand(List.of(new Ward(SSJ011), new Ward(SSJ011)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();
        playerB.getHand().addCardToHand(List.of(new Duel(SSA001)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = List.of(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerB)); // B 的回合

        // B 對 A 出決鬥
        game.playerPlayCard(playerB.getId(), SSA001.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // When
        // A 出無懈可擊
        List<DomainEvent> events = game.playWardCard(playerA.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        System.out.println(events);

        // Then
        // 應該直接生效，也就是收到 WardEvent (抵銷)，而不是 WaitForWardEvent
        assertFalse(events.stream().anyMatch(e -> e instanceof WaitForWardEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof WardEvent));

        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow();
        assertEquals("player-a", wardEvent.getPlayerId());
        assertEquals("SSA001", wardEvent.getCardId());
    }
}

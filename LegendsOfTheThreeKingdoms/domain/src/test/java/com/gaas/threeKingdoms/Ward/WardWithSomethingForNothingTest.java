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
import com.gaas.threeKingdoms.handcard.scrollcard.SomethingForNothing;
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

public class WardWithSomethingForNothingTest {

    @DisplayName("""
            Given
            有 玩家 A B C D
            A 有無中生有
            B 有無懈可擊
                        
            When
            A 出無中生有
                        
            Then
            A B C D 等待發動無懈可擊 的 event
            event 裡有可以發動無懈可擊 event 的 B 
            """)
    @Test
    public void givenPlayerAHasSomethingForNothingAndPlayerBHasWard_WhenPlayerAPlaysSomethingForNothing_ThenBReceivesAskPlayWardEventAndABDReceiveWaitForWardEvent() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new SomethingForNothing(SH7046)));

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

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SH7046.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // Then
//        A B C D 等待發動無懈可擊 的 event
//        event 裡有可以發動無懈可擊 event 的 B
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
            A 有無中生有、無懈可擊
            B 有無懈可擊
            C 有無懈可擊

            When
            A 出無中生有

            Then
            A B C D 等待發動無懈可擊 的 event
            event 裡有可以發動無懈可擊 event 的 A B C
            """)
    @Test
    public void givenABCHaveWard_WhenAPlaysSomethingForNothing_ThenABCAreAskedToPlayWardAndDWaits() {
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
                new SomethingForNothing(SH7046),
                new Ward(SSJ011) // 無懈可擊
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
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SH7046.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // Then: A B C D 收到 WaitForWardEvent
        WaitForWardEvent waitForWardEvents = events.stream()
                .filter(WaitForWardEvent.class::isInstance)
                .map(WaitForWardEvent.class::cast)
                .findFirst().get();
        assertTrue(waitForWardEvents.getPlayerIds().contains("player-a"));
        assertTrue(waitForWardEvents.getPlayerIds().contains("player-b"));
        assertTrue(waitForWardEvents.getPlayerIds().contains("player-c"));
        assertEquals(3, waitForWardEvents.getPlayerIds().size());
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有無中生有
               B 有無懈可擊

               A 出無中生有
               B 收到是否要發動無懈可擊的 event
               A C D 收到等待別人發動無懈可擊 的 event

               When
               B 出無懈可擊

               Then
               A B C D 收到 event 無懈可擊 抵銷了 無中生有
            """)
    @Test
    public void givenPlayerAHasSomethingForNothingAndPlayerBHasWard_WhenPlayerAPlaysSomethingAndPlayerBPlaysWard_ThenABCDReceive() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new SomethingForNothing(SH7046)));

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

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        game.playerPlayCard(playerA.getId(), SH7046.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        //When
        // B 發動無懈可擊的
        List<DomainEvent> events = game.playWardCard(playerB.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-b", wardEvent.getPlayerId());
        assertEquals("SH7046", wardEvent.getCardId());
        assertEquals("SSJ011", wardEvent.getWardCardId());
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有無中生有
               B 有無懈可擊
               C 有無懈可擊
                
               A 出無中生有
               
               When
               B 出無懈可擊

               Then
               A B C D 收到 PlayCard Event  
               C 收到是否要發動無懈可擊的 event
            """)
    @Test
    public void givenPlayerAHasSomethingForNothingAndPlayerBAndCHasWard_WhenPlayerAPlaysSomethingAndPlayerBPlaysWard_ThenABCDReceive() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new SomethingForNothing(SH7046)));

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

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        game.playerPlayCard(playerA.getId(), SH7046.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        //When
        // B 發動無懈可擊的
        List<DomainEvent> events = game.playWardCard(playerB.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
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
               A 有無中生有
               B 有無懈可擊
               C 有無懈可擊
                
               A 出無中生有
               B 出無懈可擊
               
               When
               C 出無懈可擊

               Then
               A B C D 收到 event >> C 的無懈可擊抵銷了 B 的無懈可擊
               A B C D 收到 A 無中生有發動 的 event
            """)
    @Test
    public void givenPlayerAHasSomethingForNothingAndPlayerBAndCHasWard_WhenPlayerAPlaysSomethingAndPlayerBAndCPlaysWard_ThenABCDReceive() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new SomethingForNothing(SH7046)));

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

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出無中生有
        game.playerPlayCard(playerA.getId(), SH7046.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());
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
        SomethingForNothingEvent somethingForNothingEvent = getEvent(events, SomethingForNothingEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-a", somethingForNothingEvent.getPlayerId());
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有無中生有
               A 有無懈可擊
               B 有無懈可擊
               C 有無懈可擊
                
               A 出無中生有
               B 出無懈可擊
               A 出無懈可擊
               
               When
               C 出無懈可擊

               Then
               A B C D 收到 event >> C 的無懈可擊抵銷了 A  的無懈可擊
               A B C D 收到 event >> B 的無懈可擊抵銷了 A  的無中生有
               A B C D 不會收到無中生有發動 的 event
            """)
    @Test
    public void givenPlayerAHasSomethingForNothingAndPlayerAAndBAndCHasWard_WhenPlayerAPlaysSomethingAndPlayerAAndBAndCPlaysWard_ThenABCDReceive() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new SomethingForNothing(SH7046), new Ward(SSJ011)));

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

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出無中生有
        List<DomainEvent> event1 = game.playerPlayCard(playerA.getId(), SH7046.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());
        // B 出無懈可擊
        List<DomainEvent> event2 = game.playWardCard(playerB.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // A 出無懈可擊
        List<DomainEvent> event3 = game.playWardCard(playerA.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        //When
        // C 發動無懈可擊
        List<DomainEvent> events = game.playWardCard(playerC.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
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

        WardEvent wardEventA = wardEvents.stream()
                .filter(e -> e.getPlayerId().equals("player-b"))
                .findFirst().orElseThrow(() -> new AssertionError("找不到 player-a 的 WardEvent"));
        assertEquals("SH7046", wardEventA.getCardId());
        assertEquals("SSJ011", wardEventA.getWardCardId());

        assertFalse(events.stream().anyMatch(event -> event instanceof SomethingForNothingEvent));
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有無中生有
               A 有無懈可擊
               B 有無懈可擊
               C 有無懈可擊
                
               A 出無中生有
               B 出 skip 無懈可擊
               A 出 skip 無懈可擊
               
               When
               C 出 skip 無懈可擊

               Then
               A B C D 收到無中生有發動 的 event
            """)
    @Test
    public void givenPlayerAHasSomethingForNothingAndPlayerAAndBAndCHasWard_WhenPlayerAPlaysSomethingAndPlayerAAndCSkipPlaysWard_ThenABCDReceive() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new SomethingForNothing(SH7046), new Ward(SSJ011)));

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

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出無中生有
        List<DomainEvent> event1 = game.playerPlayCard(playerA.getId(), SH7046.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());
        // B skip 出無懈可擊
        List<DomainEvent> event2 = game.playWardCard(playerB.getId(), "", PlayType.SKIP.getPlayType());
        // A skip 無懈可擊
        List<DomainEvent> event3 = game.playWardCard(playerA.getId(), "", PlayType.SKIP.getPlayType());

        //When
        // C skip 無懈可擊
        List<DomainEvent> events = game.playWardCard(playerC.getId(), "", PlayType.SKIP.getPlayType());

        // Then
        assertFalse(events.stream().anyMatch(event -> event instanceof WardEvent));
        assertTrue(events.stream().anyMatch(event -> event instanceof SomethingForNothingEvent));
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有無中生有
               A 有無懈可擊
               B 有無懈可擊
               C 有無懈可擊
                
               A 出 無中生有
               B 出 無懈可擊
               A 出 無懈可擊
               
               When
               C 出 skip 無懈可擊

               Then
               A B C D 收到 event >> A 的無懈可擊 抵銷了 B 的無懈可擊 
               A B C D 收到 event >> A 無中生有發動 的 event
            """)
    @Test
    public void givenPlayerAHasSomethingForNothingAndPlayerAAndBAndCHasWard_WhenPlayerAPlaysSomethingAndPlayerAAndBAndCSkipPlaysWard_ThenABCDReceive() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new SomethingForNothing(SH7046), new Ward(SSJ011)));

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

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A 出無中生有
        List<DomainEvent> event1 = game.playerPlayCard(playerA.getId(), SH7046.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());
        // B 出無懈可擊
        List<DomainEvent> event2 = game.playWardCard(playerB.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // A 出無懈可擊
        List<DomainEvent> event3 = game.playWardCard(playerA.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        //When
        // C skip 無懈可擊
        List<DomainEvent> events = game.playWardCard(playerC.getId(), "", PlayType.SKIP.getPlayType());

        // Then
        assertTrue(events.stream().anyMatch(event -> event instanceof SomethingForNothingEvent));
        assertTrue(events.stream().anyMatch(event -> event instanceof WardEvent));
        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-a", wardEvent.getPlayerId());
        assertEquals("SSJ011", wardEvent.getCardId());
        assertEquals("SSJ011", wardEvent.getWardCardId());
    }

}

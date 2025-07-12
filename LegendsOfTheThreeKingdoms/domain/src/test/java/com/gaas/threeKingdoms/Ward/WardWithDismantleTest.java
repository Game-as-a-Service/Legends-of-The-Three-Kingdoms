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

public class WardWithDismantleTest {

    @DisplayName("""
            Given
            有 玩家 A B C D
            A 有過河拆橋
            B 有無懈可擊
            
            When
            A 出過河拆橋，指定 B
            A 指定 index 0
            
            Then
            A B C D 等待發動無懈可擊 的 event
            event 裡有可以發動無懈可擊 event 的 B
            """)
    @Test
    public void givenPlayerAHasDismantleAndPlayerBHasWard_WhenPlayerAPlaysDismantle_ThenBReceivesAskPlayWardEventAndABDReceiveWaitForWardEvent() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dismantle(SS3003)));

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
        game.playerPlayCard(playerA.getId(), SS3003.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.useDismantleEffect(playerA.getId(), playerB.getId(), "", 0);

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
            A 有過河拆橋、無懈可擊
            B 有無懈可擊
            C 有無懈可擊
            
            When
            A 出過河拆橋，指定 B
            A 指定 index 0
            
            Then
            A B C D 等待發動無懈可擊 的 event
            event 裡有可以發動無懈可擊 event 的 B C，但沒有出過河拆橋的 A
            """)
    @Test
    public void givenABCHaveWard_WhenAPlaysDismantle_ThenABCAreAskedToPlayWardAndDWaits() {
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
                new Dismantle(SS3003),
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
        game.playerPlayCard(playerA.getId(), SS3003.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        // 直接呼叫 useDismantleEffect
        List<DomainEvent> events = game.useDismantleEffect(playerA.getId(), playerB.getId(), "", 0);
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
               A 有過河拆橋
               B 有無懈可擊
            
               A 出過河拆橋
               B 收到是否要發動無懈可擊的 event
               A C D 收到等待別人發動無懈可擊 的 event
            
               When
               B 出無懈可擊
            
               Then
               A B C D 收到 event 無懈可擊 抵銷了 過河拆橋
            """)
    @Test
    public void givenPlayerAHasDismantleAndPlayerBHasWard_WhenPlayerAPlaysDismantleAndPlayerBPlaysWard_ThenABCDReceive() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dismantle(SS3003)));

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

        game.playerPlayCard(playerA.getId(), SS3003.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useDismantleEffect(playerA.getId(), playerB.getId(), "", 0);

        //When
        List<DomainEvent> events = game.playWardCard(playerB.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        //Then
        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-b", wardEvent.getPlayerId());
        assertEquals("SS3003", wardEvent.getCardId());
        assertEquals("SSJ011", wardEvent.getWardCardId());
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有過河拆橋
               B 有無懈可擊
               C 有無懈可擊
            
               A 出過河拆橋
            
               When
               B 出無懈可擊
            
               Then
               A B C D 收到 PlayCard Event
               C 收到是否要發動無懈可擊的 event
            """)
    @Test
    public void givenPlayerAHasDismantleAndPlayerBAndCHasWard_WhenPlayerAPlaysDismantleAndPlayerBPlaysWard_ThenABCDReceive() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dismantle(SS3003)));

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

        game.playerPlayCard(playerA.getId(), SS3003.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useDismantleEffect(playerA.getId(), playerB.getId(), "", 0);

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
               A 有過河拆橋
               B 有無懈可擊
               C 有無懈可擊
            
               A 出過河拆橋
               B 出無懈可擊
            
               When
               C 出無懈可擊
            
               Then
               A B C D 收到 event >> C 的無懈可擊抵銷了 B 的無懈可擊
               A B C D 收到 A 過河拆橋發動 的 event
            """)
    @Test
    public void givenPlayerAHasDismantleAndPlayerBAndCHasWard_WhenPlayerAPlaysDismantleAndPlayerBAndCPlaysWard_ThenABCDReceive() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dismantle(SS3003)));

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

        // A 出過河拆橋
        game.playerPlayCard(playerA.getId(), SS3003.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useDismantleEffect(playerA.getId(), playerB.getId(), "", 0);
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
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有過河拆橋
               A 有無懈可擊
               B 有無懈可擊
               C 有無懈可擊
            
               A 出過河拆橋
               B 出無懈可擊
               A 出無懈可擊
            
               When
               C 出無懈可擊
            
               Then
               A B C D 收到 event >> C 的無懈可擊抵銷了 A 的無懈可擊
               A B C D 收到 event >> B 的無懈可擊抵銷了 A 的過河拆橋
               A B C D 不會收到過河拆橋發動 的 event
            """)
    @Test
    public void givenPlayerAHasDismantleAndPlayerAAndBAndCHasWard_WhenPlayerAPlaysDismantleAndPlayerAAndBAndCPlaysWard_ThenABCDReceive() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dismantle(SS3003), new Ward(SSJ011)));

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

        // A 出過河拆橋
        List<DomainEvent> event1 = game.playerPlayCard(playerA.getId(), SS3003.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useDismantleEffect(playerA.getId(), playerB.getId(), "", 0);
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
        assertEquals("SS3003", wardEventB.getCardId());
        assertEquals("SSJ011", wardEventB.getWardCardId());

        assertFalse(events.stream().anyMatch(event -> event instanceof DismantleEvent));
        assertTrue(game.getActivePlayer().getId().equals("player-a"));
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有過河拆橋
               A 有無懈可擊
               B 有無懈可擊
               C 有無懈可擊
            
               A 出過河拆橋
               B 出 skip 無懈可擊
               A 出 skip 無懈可擊
            
               When
               C 出 skip 無懈可擊
            
               Then
               A B C D 收到過河拆橋發動 的 event
            """)
    @Test
    public void givenPlayerAHasDismantleAndPlayerAAndBAndCHasWard_WhenPlayerAPlaysDismantleAndPlayerAAndCSkipPlaysWard_ThenABCDReceive() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dismantle(SS3003), new Ward(SSJ011)));

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

        // A 出過河拆橋
        List<DomainEvent> event1 = game.playerPlayCard(playerA.getId(), SS3003.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useDismantleEffect(playerA.getId(), playerB.getId(), "", 0);

        // B skip 無懈可擊
        List<DomainEvent> event2 = game.playWardCard(playerB.getId(), "", PlayType.SKIP.getPlayType());
        // A skip 無懈可擊
        List<DomainEvent> event3 = game.playWardCard(playerA.getId(), "", PlayType.SKIP.getPlayType());

        //When
        List<DomainEvent> events = game.playWardCard(playerC.getId(), "", PlayType.SKIP.getPlayType());

        //Then
        assertFalse(events.stream().anyMatch(event -> event instanceof WardEvent));
        assertTrue(events.stream().anyMatch(event -> event instanceof DismantleEvent));
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有過河拆橋
               A 有無懈可擊
               B 有無懈可擊
               C 有無懈可擊
            
               A 出 過河拆橋
               B 出 無懈可擊
               A 出 無懈可擊
            
               When
               C 出 skip 無懈可擊
            
               Then
               A B C D 收到 event >> A 的無懈可擊 抵銷了 B 的無懈可擊 
               A B C D 收到 event >> A 過河拆橋發動 的 event
            """)
    @Test
    public void givenPlayerAHasDismantleAndPlayerAAndBAndCHasWard_WhenPlayerAPlaysDismantleAndPlayerAAndBAndCSkipPlaysWard_ThenABCDReceive() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dismantle(SS3003), new Ward(SSJ011)));

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

        // A 出過河拆橋
        List<DomainEvent> event1 = game.playerPlayCard(playerA.getId(), SS3003.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useDismantleEffect(playerA.getId(), playerB.getId(), "", 0);
        // B 出無懈可擊
        List<DomainEvent> event2 = game.playWardCard(playerB.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // A 出無懈可擊
        List<DomainEvent> event3 = game.playWardCard(playerA.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        //When
        List<DomainEvent> events = game.playWardCard(playerC.getId(), "", PlayType.SKIP.getPlayType());

        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof DismantleEvent));
        assertTrue(events.stream().anyMatch(event -> event instanceof WardEvent));
        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-a", wardEvent.getPlayerId());
        assertEquals("SSJ011", wardEvent.getCardId());
        assertEquals("SSJ011", wardEvent.getWardCardId());
        assertTrue(game.getActivePlayer().getId().equals("player-a"));
    }
}

package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskPlayWardEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.WaitForWardEvent;
import com.gaas.threeKingdoms.events.WardEvent;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WardTest {

    @DisplayName("""
            Given
            有 玩家 A B C D
            A 有無中生有
            B 有無懈可擊
                        
            When
            A 出無中生有
                        
            Then
            B 收到 是否要發動無懈可擊的 event
            A B D 收到 等待別人發動無懈可擊 的 event
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
        // B 收到是否要發動無懈可擊的 event
        Optional<AskPlayWardEvent> askPlayWardEvent = events.stream()
                .filter(AskPlayWardEvent.class::isInstance)
                .map(AskPlayWardEvent.class::cast)
                .findFirst();

        assertTrue(askPlayWardEvent.isPresent(), "AskPlayWardEvent should be present");
        assertEquals("player-b", askPlayWardEvent.get().getPlayerId(), "AskPlayWardEvent should be sent to player-b");

        // A B D 收到 等待別人發動無懈可擊 的 event
        List<WaitForWardEvent> waitForWardEvents = events.stream()
                .filter(WaitForWardEvent.class::isInstance)
                .map(WaitForWardEvent.class::cast)
                .toList();

        assertEquals(3, waitForWardEvents.size(), "There should be 3 WaitForWardEvents");

        Set<String> waitingPlayerIds = waitForWardEvents.stream()
                .map(WaitForWardEvent::getPlayerId)
                .collect(Collectors.toSet());
        assertTrue(waitingPlayerIds.contains("player-a"), "Player A should be waiting");
        assertTrue(waitingPlayerIds.contains("player-c"), "Player C should be waiting");
        assertTrue(waitingPlayerIds.contains("player-d"), "Player D should be waiting");
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
            A B C 收到是否要發動無懈可擊的 event
            D 收到等待別人發動無懈可擊 的 event
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

        // Then: A B C 收到 AskPlayWardEvent
        List<AskPlayWardEvent> askPlayWardEvents = events.stream()
                .filter(e -> e instanceof AskPlayWardEvent)
                .map(e -> (AskPlayWardEvent) e)
                .toList();

        assertEquals(3, askPlayWardEvents.size(), "There should be 3 AskPlayWardEvents");
        Set<String> askPlayerIds = askPlayWardEvents.stream().map(AskPlayWardEvent::getPlayerId).collect(Collectors.toSet());
        assertTrue(askPlayerIds.contains("player-a"), "Player A should receive AskPlayWardEvent");
        assertTrue(askPlayerIds.contains("player-b"), "Player B should receive AskPlayWardEvent");
        assertTrue(askPlayerIds.contains("player-c"), "Player C should receive AskPlayWardEvent");

        // Then: D 收到 WaitForWardEvent
        List<WaitForWardEvent> waitForWardEvents = events.stream()
                .filter(e -> e instanceof WaitForWardEvent)
                .map(e -> (WaitForWardEvent) e)
                .toList();

        assertEquals(1, waitForWardEvents.size(), "There should be 1 WaitForWardEvent");
        assertEquals("player-d", waitForWardEvents.get(0).getPlayerId(), "Player D should receive WaitForWardEvent");
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
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), SSJ011.getCardId(), playerA.getId(), PlayType.INACTIVE.getPlayType());

        List<WardEvent> wardEvents = events.stream()
                .filter(e -> e instanceof WardEvent)
                .map(e -> (WardEvent) e)
                .toList();

    }

}

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
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
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

public class WardWithBorrowedSwordTest {

    @DisplayName("""
            Given
            有 玩家 A B C D
            A 有借刀殺人
            B 有武器、殺、無懈可擊
            C 為攻擊目標

            When
            A 出借刀殺人，指定 B 殺 C

            Then
            A B C D 等待發動無懈可擊 的 event
            event 裡有可以發動無懈可擊 event 的 B
            """)
    @Test
    public void givenPlayerAHasBorrowedSwordAndPlayerBHasWard_WhenPlayerAPlaysBorrowedSword_ThenBReceivesWaitForWardEvent() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new BorrowedSword(SCK065)));

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Ward(SSJ011)));

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

        // When
        game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId());

        // Then
        WaitForWardEvent waitForWardEvent = events.stream()
                .filter(WaitForWardEvent.class::isInstance)
                .map(WaitForWardEvent.class::cast)
                .findFirst().get();
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-b"));
        assertEquals(1, waitForWardEvent.getPlayerIds().size());
    }

    @DisplayName("""
            Given
            有 玩家 A B C D
            A 有借刀殺人、無懈可擊
            B 有武器、殺、無懈可擊
            C 有無懈可擊

            When
            A 出借刀殺人，指定 B 殺 C

            Then
            A B C D 等待發動無懈可擊 的 event
            event 裡有可以發動無懈可擊 event 的 B C，但沒有出借刀殺人的 A
            """)
    @Test
    public void givenABCHaveWard_WhenAPlaysBorrowedSword_ThenWaitForWardEventContainsBCButNotA() {
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
                new BorrowedSword(SCK065),
                new Ward(SSJ011)
        ));

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();
        playerB.getHand().addCardToHand(List.of(new Kill(BS8008), new Ward(SSJ011)));

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
        game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId());

        // Then
        WaitForWardEvent waitForWardEvent = events.stream()
                .filter(WaitForWardEvent.class::isInstance)
                .map(WaitForWardEvent.class::cast)
                .findFirst().get();
        assertFalse(waitForWardEvent.getPlayerIds().contains("player-a"));
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-b"));
        assertTrue(waitForWardEvent.getPlayerIds().contains("player-c"));
        assertEquals(2, waitForWardEvent.getPlayerIds().size());
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有借刀殺人
               B 有武器、殺、無懈可擊

               A 出借刀殺人，指定 B 殺 C
               B 收到是否要發動無懈可擊的 event
               A C D 收到等待別人發動無懈可擊 的 event

               When
               B 出無懈可擊

               Then
               A B C D 收到 event 無懈可擊 抵銷了 借刀殺人
            """)
    @Test
    public void givenPlayerAHasBorrowedSwordAndPlayerBHasWard_WhenPlayerBPlaysWard_ThenWardCancelsBorrowedSword() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new BorrowedSword(SCK065)));

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Ward(SSJ011)));

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

        game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId());

        // When
        List<DomainEvent> events = game.playWardCard(playerB.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-b", wardEvent.getPlayerId());
        assertEquals("SCK065", wardEvent.getCardId());
        assertEquals("SSJ011", wardEvent.getWardCardId());
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有借刀殺人
               B 有武器、殺、無懈可擊
               C 有無懈可擊

               A 出借刀殺人，指定 B 殺 C

               When
               B 出無懈可擊

               Then
               A B C D 收到 PlayCard Event
               C 收到是否要發動無懈可擊的 event
            """)
    @Test
    public void givenPlayerBAndCHaveWard_WhenPlayerBPlaysWard_ThenCReceivesWaitForWardEvent() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new BorrowedSword(SCK065)));

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Ward(SSJ011)));

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

        game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId());

        // When
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
               A 有借刀殺人
               B 有武器、殺、無懈可擊
               C 有無懈可擊

               A 出借刀殺人，指定 B 殺 C
               B 出無懈可擊

               When
               C 出無懈可擊

               Then
               A B C D 收到 event >> C 的無懈可擊抵銷了 B 的無懈可擊
               A B C D 收到 借刀殺人發動 的 event (BorrowedSwordEvent)
            """)
    @Test
    public void givenPlayerBAndCHaveWard_WhenPlayerBAndCPlayWard_ThenBorrowedSwordTakesEffect() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new BorrowedSword(SCK065)));

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Ward(SSJ011)));

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

        // A 出借刀殺人
        game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId());
        // B 出無懈可擊
        game.playWardCard(playerB.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // When
        // C 發動無懈可擊
        List<DomainEvent> events = game.playWardCard(playerC.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // Then
        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-c", wardEvent.getPlayerId());
        assertEquals("SSJ011", wardEvent.getCardId());
        assertEquals("SSJ011", wardEvent.getWardCardId());
        BorrowedSwordEvent borrowedSwordEvent = getEvent(events, BorrowedSwordEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-b", borrowedSwordEvent.getBorrowedPlayerId());
        assertEquals("player-c", borrowedSwordEvent.getAttackTargetPlayerId());
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有借刀殺人、無懈可擊
               B 有武器、殺、無懈可擊
               C 有無懈可擊

               A 出借刀殺人，指定 B 殺 C
               B skip 無懈可擊
               A skip 無懈可擊

               When
               C skip 無懈可擊

               Then
               A B C D 收到借刀殺人發動 的 event (BorrowedSwordEvent)
            """)
    @Test
    public void givenABCHaveWard_WhenAllSkipWard_ThenBorrowedSwordTakesEffect() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new BorrowedSword(SCK065), new Ward(SSJ011)));

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Ward(SSJ011)));

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

        // A 出借刀殺人
        game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId());

        // B skip 無懈可擊
        game.playWardCard(playerB.getId(), "", PlayType.SKIP.getPlayType());
        // A skip 無懈可擊
        game.playWardCard(playerA.getId(), "", PlayType.SKIP.getPlayType());

        // When
        // C skip 無懈可擊
        List<DomainEvent> events = game.playWardCard(playerC.getId(), "", PlayType.SKIP.getPlayType());

        // Then
        assertFalse(events.stream().anyMatch(event -> event instanceof WardEvent));
        assertTrue(events.stream().anyMatch(event -> event instanceof BorrowedSwordEvent));
        BorrowedSwordEvent borrowedSwordEvent = getEvent(events, BorrowedSwordEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-b", borrowedSwordEvent.getBorrowedPlayerId());
        assertEquals("player-c", borrowedSwordEvent.getAttackTargetPlayerId());
    }

    @DisplayName("""
               Given
               有 玩家 ABCD
               A 有借刀殺人、無懈可擊
               B 有武器、殺、無懈可擊
               C 有無懈可擊

               A 出借刀殺人，指定 B 殺 C
               B 出無懈可擊
               A 出無懈可擊

               When
               C skip 無懈可擊

               Then
               A B C D 收到 event >> A 的無懈可擊 抵銷了 B 的無懈可擊
               A B C D 收到 借刀殺人發動 的 event (BorrowedSwordEvent)
            """)
    @Test
    public void givenABCHaveWard_WhenBPlaysWardAndAPlaysWardAndCSkips_ThenBorrowedSwordTakesEffect() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new BorrowedSword(SCK065), new Ward(SSJ011)));

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Ward(SSJ011)));

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

        // A 出借刀殺人
        game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId());

        // B 出無懈可擊
        game.playWardCard(playerB.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());
        // A 出無懈可擊
        game.playWardCard(playerA.getId(), SSJ011.getCardId(), PlayType.ACTIVE.getPlayType());

        // When
        // C skip 無懈可擊
        List<DomainEvent> events = game.playWardCard(playerC.getId(), "", PlayType.SKIP.getPlayType());

        // Then
        assertTrue(events.stream().anyMatch(event -> event instanceof BorrowedSwordEvent));
        assertTrue(events.stream().anyMatch(event -> event instanceof WardEvent));
        WardEvent wardEvent = getEvent(events, WardEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-a", wardEvent.getPlayerId());
        assertEquals("SSJ011", wardEvent.getCardId());
        assertEquals("SSJ011", wardEvent.getWardCardId());
    }
}

package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.events.PlayerDyingEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class BarbarianInvasionTest {

    @DisplayName("""
            Given
            玩家ABCD
            B的回合
                            
            When
            B玩家出南蠻入侵
                            
            Then
            ABCD 玩家收到南蠻入侵的 event
            C 玩家收到要求出殺的 event
            """)
    @Test
    public void givenPlayerBHasBarbarianInvasion_WhenPlayerBPlayBarbarianInvasion_ThenPlayerABCDAcceptBarbarianInvasionEvent() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new BarbarianInvasion(SS7007)));

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
        game.setCurrentRound(new Round(playerB));

        //When
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        AskKillEvent askKillEvent = events.stream()
                .filter(event -> event instanceof AskKillEvent)
                .map(event -> (AskKillEvent) event)
                .findFirst()
                .orElse(null);

        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof AskKillEvent));
        assertEquals("player-c", askKillEvent != null ? askKillEvent.getPlayerId() : null);
        assertEquals("player-c", game.getActivePlayer().getId());
    }

    @DisplayName("""
                Given
                玩家ABCD
                B的回合
                C 玩家沒有殺
                C 玩家 hp = 3
                B玩家出南蠻入侵
                
                When
                C玩家出skip殺
                
                Then
                C玩家 hp = 2
                D玩家收到要求出殺的event
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayBarbarianInvasion_ThenPlayerCHP2AndPlayerDReceiveKillEvent() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new BarbarianInvasion(SS7007)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(3))
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
        game.setCurrentRound(new Round(playerB));

        game.playerPlayCard(playerB.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        List<DomainEvent> events = game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        AskKillEvent askKillEvent = events.stream()
                .filter(event -> event instanceof AskKillEvent)
                .map(event -> (AskKillEvent) event)
                .findFirst()
                .orElse(null);

        //Then
        assertEquals(2, playerC.getHP());

        events.stream()
                .filter(event -> event instanceof PlayCardEvent)
                .findFirst().orElseThrow();

        assertEquals("player-d", askKillEvent != null ? askKillEvent.getPlayerId() : null);
    }

    @DisplayName("""
                Given
                玩家ABCD
                B的回合
                C 玩家沒有殺
                C 玩家 hp = 1
                B玩家出南蠻入侵
                
                When
                C玩家出skip殺
                
                Then
                C玩家 hp = 0
                C 玩家進入瀕臨死亡
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayBarbarianInvasion_ThenPlayerCHP0AndPlayerCEnterDyingState() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new BarbarianInvasion(SS7007)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(1))
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
        game.setCurrentRound(new Round(playerB));

        game.playerPlayCard(playerB.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        List<DomainEvent> events = game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        PlayerDyingEvent playerDyingEvent = events.stream()
                .filter(event -> event instanceof PlayerDyingEvent)
                .map(event -> (PlayerDyingEvent) event)
                .findFirst()
                .orElseThrow();

        //Then
        assertEquals(0, playerC.getHP());

        events.stream()
                .filter(event -> event instanceof PlayCardEvent)
                .findFirst().orElseThrow();
        assertEquals("player-c", playerDyingEvent.getPlayerId());
        assertEquals("GeneralDying", game.getGamePhase().getPhaseName());
    }

    @DisplayName("""
                Given
                玩家ABCD
                B的回合
                C 玩家沒有殺，有桃
                C 玩家 hp = 1
                B玩家出南蠻入侵
                C玩家出skip殺
                C玩家 hp = 0 ，進入瀕臨死亡
                When
                C 玩家出桃
                
                Then
                D玩家收到要求出殺的event
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayBarbarianInvasionAndPlayerCUsePeach_ThenPlayerDReceiveKillEvent() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new BarbarianInvasion(SS7007)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(1))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerC.getHand().addCardToHand(Arrays.asList(new Peach(BH3029)));

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
        game.setCurrentRound(new Round(playerB));

        game.playerPlayCard(playerB.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        //When
        List<DomainEvent> events = game.playerPlayCard(playerC.getId(), BH3029.getCardId(), playerC.getId(), "active");

        //Then
        AskKillEvent askKillEvent = events.stream()
                .filter(event -> event instanceof AskKillEvent)
                .map(event -> (AskKillEvent) event)
                .findFirst()
                .orElseThrow();

        assertEquals("player-d", askKillEvent.getPlayerId());
        assertEquals("Normal", game.getGamePhase().getPhaseName());
    }

    @DisplayName("""
                Given
                玩家ABCD
                B的回合
                C 玩家沒有殺，有桃
                C 玩家 hp = 1
                B玩家出南蠻入侵
                C 玩家出skip殺
                C 玩家 hp = 0 ，進入瀕臨死亡
                C 玩家沒有桃
                D 玩家有一張桃
                
                When
                C 玩家出skip
                D 玩家出桃
                
                
                Then
                C玩家 hp = 1
                D玩家收到要求出殺的event
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayBarbarianInvasionAndPlayerCEnterDyingStateAndPlayerDUsePeach_ThenPlayerCHP1AndPlayerDReceiveKillEvent() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new BarbarianInvasion(SS7007)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(1))
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

        playerD.getHand().addCardToHand(Arrays.asList(new Peach(BH3029)));

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerB));

        game.playerPlayCard(playerB.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        //When
        List<DomainEvent> eventsOfSkipPlayPeach = game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());


        List<DomainEvent> events = game.playerPlayCard(playerD.getId(), BH3029.getCardId(), playerC.getId(), "active");

        //Then
        AskKillEvent askKillEvent = events.stream()
                .filter(event -> event instanceof AskKillEvent)
                .map(event -> (AskKillEvent) event)
                .findFirst()
                .orElseThrow();

        assertEquals(1, playerC.getHP());
        assertEquals("player-d", askKillEvent.getPlayerId());
        assertEquals("Normal", game.getGamePhase().getPhaseName());
    }

    @DisplayName("""
                Given
                玩家ABCD
                B的回合
                C 玩家有一張殺
                C 玩家 hp = 3
                B玩家出南蠻入侵
                When
                
                C玩家出殺
                
                Then
                
                C玩家 hp = 3
                D玩家收到要求出殺的event
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayBarbarianInvasionAndPlayerCUseKill_ThenPlayerCHP3AndPlayerDReceiveKillEvent() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new BarbarianInvasion(SS7007)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerC.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

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
        game.setCurrentRound(new Round(playerB));

        List<DomainEvent> playerBEvent = game.playerPlayCard(playerB.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        List<DomainEvent> events = game.playerPlayCard(playerC.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        //Then
        AskKillEvent askKillEvent = events.stream()
                .filter(event -> event instanceof AskKillEvent)
                .map(event -> (AskKillEvent) event)
                .findFirst()
                .orElseThrow();

        assertEquals(3, playerC.getHP());
        assertEquals("player-d", askKillEvent.getPlayerId());
    }

    @DisplayName("""
                Given
                玩家ABCD
                B的回合
                C 玩家有一張殺
                C 玩家 hp = 3
                B玩家出南蠻入侵
                When
                
                C玩家出 skip
                
                Then
                
                C玩家 hp = 2
                D玩家收到要求出殺的event
                Active player 是 D 玩家
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayBarbarianInvasionAndPlayerCUseSkip_ThenPlayerCHP2AndPlayerDReceiveKillEvent() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new BarbarianInvasion(SS7007)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerC.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

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
        game.setCurrentRound(new Round(playerB));

        List<DomainEvent> playerBEvent = game.playerPlayCard(playerB.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        List<DomainEvent> events = game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        //Then
        AskKillEvent askKillEvent = events.stream()
                .filter(event -> event instanceof AskKillEvent)
                .map(event -> (AskKillEvent) event)
                .findFirst()
                .orElseThrow();

        assertEquals(2, playerC.getHP());
        assertEquals("player-d", askKillEvent.getPlayerId());
        assertEquals("player-d", game.getActivePlayer().getId());

    }

    @DisplayName("""
            Given
            玩家ABCD
            B的回合
            C 玩家有一張殺
            C 玩家 hp = 3
            D 玩家沒有殺
            D 玩家 hp = 4
                        
            When
            B玩家出南蠻入侵
            C玩家出殺
            C玩家 hp = 3
            D玩家收到要求出殺的event
            D玩家出skip
                        
            Then
            D 玩家 hp = 3
            A 玩家 收到要求出殺
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayBarbarianInvasionAndPlayerCUseKillAndPlayerDUseSkip_ThenPlayerDHP3AndPlayerAReceiveKillEvent() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new BarbarianInvasion(SS7007)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerC.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

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
        game.setCurrentRound(new Round(playerB));

        List<DomainEvent> playerBEvent = game.playerPlayCard(playerB.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        game.playerPlayCard(playerC.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerD.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        //Then
        AskKillEvent askKillEvent = events.stream()
                .filter(event -> event instanceof AskKillEvent)
                .map(event -> (AskKillEvent) event)
                .findFirst()
                .orElseThrow();

        assertEquals(3, playerD.getHP());
        assertEquals("player-a", askKillEvent.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家ABCD
            B的回合
            C 玩家有一張殺
            C 玩家 hp = 3
            D 玩家有一張殺
            D 玩家 hp = 4
            A 玩家有一張殺
            A 玩家 hp = 4
                        
            When
            B玩家出南蠻入侵
            C玩家出殺
            C玩家 hp = 3
            D玩家收到要求出殺的event
            D玩家出殺
            A 玩家收到要求出殺的event
            A玩家出殺
                        
            Then
            A 玩家 hp = 4
            B 玩家的回合
            B 玩家是active player
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayBarbarianInvasionAndPlayerCUseKillAndPlayerDUseKillAndPlayerAUseKill_ThenPlayerAHP4AndPlayerBActivePlayer() {
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new RedRabbitHorse(BH3029), new RedRabbitHorse(BH3029), new RedRabbitHorse(BH3029), new RedRabbitHorse(BH3029), new RedRabbitHorse(BH3029)
                )
        );
        game.setDeck(deck);
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BH0036), new Peach(BH3029), new BarbarianInvasion(SS7007)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new BarbarianInvasion(SS7007)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerC.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerD.getHand().addCardToHand(Arrays.asList(new Kill(BS8009)));

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerB));

        game.playerPlayCard(playerB.getId(), SS7007.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        game.playerPlayCard(playerC.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerD.getId(), BS8009.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), BH0036.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        //Then
        Optional<AskKillEvent> askKillEvent = events.stream()
                .filter(event -> event instanceof AskKillEvent)
                .map(event -> (AskKillEvent) event)
                .findFirst();
        assertFalse(askKillEvent.isPresent());

        assertEquals(4, playerA.getHP());
        assertEquals("player-b", game.getCurrentRoundPlayer().getId());
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());


        //When
        game.finishAction("player-b");

        //Then
        assertTrue(playerC.getHand().getCards().stream().noneMatch(card -> card.getId().equals("BS8008")));
    }


}

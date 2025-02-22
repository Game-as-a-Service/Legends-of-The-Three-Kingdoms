package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
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
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrowBarrageTest {

    @DisplayName("""
            Given
            玩家ABCD
            B的回合
                            
            When
            B玩家出萬箭齊發
                            
            Then
            ABCD 玩家收到萬箭齊發的 event
            C 玩家收到要求出閃的 event
            """)
    @Test
    public void givenPlayerBHasBarbarianInvasion_WhenPlayerBPlayArrowBarrage_ThenPlayerABCDAcceptBarbarianInvasionEvent() {
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

        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089), new Peach(BH3029), new ArrowBarrage(SHA040)));

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
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        AskDodgeEvent askDodgeEvent = events.stream()
                .filter(event -> event instanceof AskDodgeEvent)
                .map(event -> (AskDodgeEvent) event)
                .findFirst()
                .orElse(null);

        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof AskDodgeEvent));
        assertEquals("player-c", askDodgeEvent != null ? askDodgeEvent.getPlayerId() : null);
        assertEquals("player-c", game.getActivePlayer().getId());
    }

    @DisplayName("""
                Given
                玩家ABCD
                B的回合
                C 玩家沒有閃
                C 玩家 hp = 3
                B玩家出萬箭齊發
                
                When
                C玩家出skip
                
                Then
                C玩家 hp = 2
                D玩家收到要求出閃的event
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayArrowBarrage_ThenPlayerCHP2AndPlayerDReceiveDodgeEvent() {
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

        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089), new Peach(BH3029), new ArrowBarrage(SHA040)));

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

        game.playerPlayCard(playerB.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        List<DomainEvent> events = game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        AskDodgeEvent askDodgeEvent = events.stream()
                .filter(event -> event instanceof AskDodgeEvent)
                .map(event -> (AskDodgeEvent) event)
                .findFirst()
                .orElse(null);

        //Then
        assertEquals(2, playerC.getHP());

        events.stream()
                .filter(event -> event instanceof PlayCardEvent)
                .findFirst().orElseThrow();

        assertEquals("player-d", askDodgeEvent != null ? askDodgeEvent.getPlayerId() : null);
    }

    @DisplayName("""
                Given
                玩家ABCD
                B的回合
                C 玩家沒有閃
                C 玩家 hp = 1
                B玩家出萬箭齊發
                
                When
                C玩家出skip
                
                Then
                C玩家 hp = 0
                C 玩家進入瀕臨死亡
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayArrowBarrage_ThenPlayerCHP0AndPlayerCEnterDyingState() {
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

        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089), new Peach(BH3029), new ArrowBarrage(SHA040)));

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

        game.playerPlayCard(playerB.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

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
                C 玩家沒有閃，有桃
                C 玩家 hp = 1
                B玩家出萬箭齊發
                C玩家出skip閃
                C玩家 hp = 0 ，進入瀕臨死亡
                When
                C 玩家出桃
                
                Then
                D玩家收到要求出閃的event
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayArrowBarrageAndPlayerCUsePeach_ThenPlayerDReceiveDodgeEvent() {
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

        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089), new Peach(BH3029), new ArrowBarrage(SHA040)));

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

        game.playerPlayCard(playerB.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        //When
        List<DomainEvent> events = game.playerPlayCard(playerC.getId(), BH3029.getCardId(), playerC.getId(), "active");

        //Then
        AskDodgeEvent askDodgeEvent = events.stream()
                .filter(event -> event instanceof AskDodgeEvent)
                .map(event -> (AskDodgeEvent) event)
                .findFirst()
                .orElseThrow();

        assertEquals("player-d", askDodgeEvent.getPlayerId());
        assertEquals("Normal", game.getGamePhase().getPhaseName());
    }

    @DisplayName("""
                Given
                玩家ABCD
                B的回合
                C 玩家沒有閃，有桃
                C 玩家 hp = 1
                B玩家出萬箭齊發
                C 玩家出skip閃
                C 玩家 hp = 0 ，進入瀕臨死亡
                C 玩家沒有桃
                D 玩家有一張桃
                
                When
                C 玩家出skip
                D 玩家出桃
                
                
                Then
                C玩家 hp = 1
                D玩家收到要求出閃的event
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayArrowBarrageAndPlayerCEnterDyingStateAndPlayerDUsePeach_ThenPlayerCHP1AndPlayerDReceiveDodgeEvent() {
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

        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089), new Peach(BH3029), new ArrowBarrage(SHA040)));

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

        game.playerPlayCard(playerB.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        //When
        List<DomainEvent> eventsOfSkipPlayPeach = game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());


        List<DomainEvent> events = game.playerPlayCard(playerD.getId(), BH3029.getCardId(), playerC.getId(), "active");

        //Then
        AskDodgeEvent askDodgeEvent = events.stream()
                .filter(event -> event instanceof AskDodgeEvent)
                .map(event -> (AskDodgeEvent) event)
                .findFirst()
                .orElseThrow();

        assertEquals(1, playerC.getHP());
        assertEquals("player-d", askDodgeEvent.getPlayerId());
        assertEquals("Normal", game.getGamePhase().getPhaseName());
    }

    @DisplayName("""
                Given
                玩家ABCD
                B的回合
                C 玩家有一張閃
                C 玩家 hp = 3
                B玩家出萬箭齊發
                When
                
                C玩家出閃
                
                Then
                
                C玩家 hp = 3
                D玩家收到要求出閃的event
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayArrowBarrageAndPlayerCUseDodge_ThenPlayerCHP3AndPlayerDReceiveDodgeEvent() {
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

        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089), new Peach(BH3029), new ArrowBarrage(SHA040)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerC.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089)));

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

        List<DomainEvent> playerBEvent = game.playerPlayCard(playerB.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        List<DomainEvent> events = game.playerPlayCard(playerC.getId(), BDJ089.getCardId(), playerB.getId(), PlayType.INACTIVE.getPlayType());

        //Then
        AskDodgeEvent askDodgeEvent = events.stream()
                .filter(event -> event instanceof AskDodgeEvent)
                .map(event -> (AskDodgeEvent) event)
                .findFirst()
                .orElseThrow();

        assertEquals(3, playerC.getHP());
        assertEquals("player-d", askDodgeEvent.getPlayerId());
    }

    @DisplayName("""
                Given
                玩家ABCD
                B的回合
                C 玩家有一張閃
                C 玩家 hp = 3
                B玩家出萬箭齊發
                When
                
                C玩家出 skip
                
                Then
                
                C玩家 hp = 2
                D玩家收到要求出閃的event
                Active player 是 D 玩家
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayArrowBarrageAndPlayerCUseSkip_ThenPlayerCHP2AndPlayerDReceiveDodgeEvent() {
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

        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089), new Peach(BH3029), new ArrowBarrage(SHA040)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerC.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089)));

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

        List<DomainEvent> playerBEvent = game.playerPlayCard(playerB.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        List<DomainEvent> events = game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        //Then
        AskDodgeEvent askDodgeEvent = events.stream()
                .filter(event -> event instanceof AskDodgeEvent)
                .map(event -> (AskDodgeEvent) event)
                .findFirst()
                .orElseThrow();

        assertEquals(2, playerC.getHP());
        assertEquals("player-d", askDodgeEvent.getPlayerId());
        assertEquals("player-d", game.getActivePlayer().getId());

    }

    @DisplayName("""
            Given
            玩家ABCD
            B的回合
            C 玩家有一張閃
            C 玩家 hp = 3
            D 玩家沒有閃
            D 玩家 hp = 4
                        
            When
            B玩家出萬箭齊發
            C玩家出閃
            C玩家 hp = 3
            D玩家收到要求出閃的event
            D玩家出skip
                        
            Then
            D 玩家 hp = 3
            A 玩家 收到要求出閃
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayArrowBarrageAndPlayerCUseDodgeAndPlayerDUseSkip_ThenPlayerDHP3AndPlayerAReceiveDodgeEvent() {
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

        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089), new Peach(BH3029), new ArrowBarrage(SHA040)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerC.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089)));

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

        List<DomainEvent> playerBEvent = game.playerPlayCard(playerB.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        game.playerPlayCard(playerC.getId(), BDJ089.getCardId(), playerB.getId(), PlayType.INACTIVE.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerD.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        //Then
        AskDodgeEvent askDodgeEvent = events.stream()
                .filter(event -> event instanceof AskDodgeEvent)
                .map(event -> (AskDodgeEvent) event)
                .findFirst()
                .orElseThrow();

        assertEquals(3, playerD.getHP());
        assertEquals("player-a", askDodgeEvent.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家ABCD
            B的回合
            C 玩家有一張閃
            C 玩家 hp = 3
            D 玩家有一張閃
            D 玩家 hp = 4
            A 玩家有一張閃
            A 玩家 hp = 4
                        
            When
            B玩家出萬箭齊發
            C玩家出閃
            C玩家 hp = 3
            D玩家收到要求出閃的event
            D玩家出閃
            A 玩家收到要求出閃的event
            A玩家出閃
                        
            Then
            A 玩家 hp = 4
            B 玩家的回合
            B 玩家是active player
            """)
    @Test
    public void givenPlayerABCD_WhenPlayerBPlayArrowBarrageAndPlayerCUseDodgeAndPlayerDUseDodgeAndPlayerAUseDodge_ThenPlayerAHP4AndPlayerBActivePlayer() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089), new Peach(BH3029), new ArrowBarrage(SHA040)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089), new Peach(BH3029), new ArrowBarrage(SHA040)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerC.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerD.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089)));

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerB));

        game.playerPlayCard(playerB.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        game.playerPlayCard(playerC.getId(), BDJ089.getCardId(), playerB.getId(), PlayType.INACTIVE.getPlayType());
        game.playerPlayCard(playerD.getId(), BDJ089.getCardId(), playerB.getId(), PlayType.INACTIVE.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), BDJ089.getCardId(), playerB.getId(), PlayType.INACTIVE.getPlayType());

        //Then
        Optional<AskDodgeEvent> askDodgeEvent = events.stream()
                .filter(event -> event instanceof AskDodgeEvent)
                .map(event -> (AskDodgeEvent) event)
                .findFirst();
        assertFalse(askDodgeEvent.isPresent());

        assertEquals(4, playerA.getHP());
        assertEquals("player-b", game.getCurrentRoundPlayer().getId());
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());

        //When
        game.finishAction("player-b");

        //Then
        assertTrue(playerC.getHand().getCards().stream().noneMatch(card -> card.getId().equals("BDJ089")));
    }
}

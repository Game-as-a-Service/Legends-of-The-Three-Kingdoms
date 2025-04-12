package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.gaas.threeKingdoms.Utils.getEvent;
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
                玩家 A B C D
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

    @DisplayName("""
            Given
            玩家ABC
            A的回合
            B玩家沒有閃
            B玩家 hp = 1
            
            When
            
            A玩家出萬箭齊發
            B C D A玩家出skip
            
            Then
            B玩家 hp = 0
            C玩家收到要求出閃的event
            """)
    @Test
    public void givenPlayerABC_WhenPlayerAPlayArrowBarrageAndPlayerBNoDodgeAndNoPeach_ThenPlayerBDeathAndPlayerCReceiveDodgeEvent() {
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
                .withBloodCard(new BloodCard(1))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

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
        game.setCurrentRound(new Round(playerA));

        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());


        //Then
        Optional<AskDodgeEvent> askDodgeEvent = events.stream()
                .filter(event -> event instanceof AskDodgeEvent)
                .map(event -> (AskDodgeEvent) event)
                .findFirst();
        assertTrue(askDodgeEvent.isPresent());
        assertEquals("player-c", askDodgeEvent.get().getPlayerId());

        assertEquals(0, playerB.getHP());
        assertEquals("player-a", game.getCurrentRoundPlayer().getId());
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());

    }

    @DisplayName("""
            Given
            玩家ABC
            A的回合
            B玩家沒有閃
            B玩家 hp = 1
            
            When
            
            A玩家出萬箭齊發
            
            Then
            B 收到要不要發動裝備卡的 Event
            """)
    @Test
    public void givenPlayerABC_WhenPlayerAPlayArrowBarrageAndPlayerBNoDodgeAndNoPeachHaveEightDiagramTactic_ThenPlayerBReceiveAskPlayEquipmentEffectEvent() {
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


        Equipment equipmentB = new Equipment();
        equipmentB.setArmor(new EightDiagramTactic(ES2015));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();

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
        game.setCurrentRound(new Round(playerA));


        //When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent));
        assertFalse(events.stream().anyMatch(event -> event instanceof AskDodgeEvent));
        AskPlayEquipmentEffectEvent askPlayEquipmentEffectEvent = getEvent(events, AskPlayEquipmentEffectEvent.class).orElseThrow(RuntimeException::new);

        assertEquals("player-b", askPlayEquipmentEffectEvent.getPlayerId());
        assertEquals(List.of("player-b"), askPlayEquipmentEffectEvent.getTargetPlayerIds());
    }

    @DisplayName("""
            Given
            玩家ABC
            A的回合
            B玩家沒有閃
            B玩家 hp = 1
            A玩家出萬箭齊發
            B收到要不要發動裝備卡的event
            
            When
            B 發動裝備卡
            
            Then
            全部人收到 八卦陣效果抽到赤兔馬 (♥3) 的 Event
            Event 內是效果成功， B 不用出閃
            B玩家 hp = 1
            C玩家收到要求出閃的event
            """)
    @Test
    public void givenPlayerABC_WhenBUseEquipment_ThenEffectEventIsSuccess() {
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


        Equipment equipmentB = new Equipment();
        equipmentB.setArmor(new EightDiagramTactic(ES2015));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();

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
        game.setCurrentRound(new Round(playerA));

        // player-a 出萬箭齊發
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        // player-b 發動裝備效果
        List<DomainEvent> events = game.playerUseEquipment(playerB.getId(), ES2015.getCardId(), playerA.getId(), EquipmentPlayType.ACTIVE);

        //Then
        assertTrue(events.stream()
                .filter(event -> event instanceof EffectEvent)
                .map(EffectEvent.class::cast)
                .allMatch(EffectEvent::isSuccess));

        assertTrue(events.stream().anyMatch(event -> event instanceof AskDodgeEvent));
        assertEquals(1, playerB.getHP());

        AskDodgeEvent askDodgeEvent = getEvent(events, AskDodgeEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-c", askDodgeEvent.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家ABC
            A的回合
            B玩家沒有閃
            B玩家 hp = 1
            A玩家出萬箭齊發
            B收到要不要發動裝備卡的event
            
            When
            B 發動裝備卡
            
            Then
            八卦陣效果抽到梅花三
            八卦陣效果event isSuccess = false
            B 收到要求出閃的 event
            """)
    @Test
    public void givenPlayerABC_WhenBUseEquipment_ThenEffectEventIsFailure() {
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Dismantle(SS3003)
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

        Equipment equipmentB = new Equipment();
        equipmentB.setArmor(new EightDiagramTactic(ES2015));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();

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
        game.setCurrentRound(new Round(playerA));

        // player-a 出萬箭齊發
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        // player-b 發動裝備效果
        List<DomainEvent> events = game.playerUseEquipment(playerB.getId(), ES2015.getCardId(), playerA.getId(), EquipmentPlayType.ACTIVE);

        //Then
        assertFalse(events.stream()
                .filter(event -> event instanceof EffectEvent)
                .map(EffectEvent.class::cast)
                .allMatch(EffectEvent::isSuccess));

        AskDodgeEvent askDodgeEvent = getEvent(events, AskDodgeEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-b", askDodgeEvent.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家ABC
            A的回合
            B玩家沒有閃
            B玩家 hp = 2
            A玩家出萬箭齊發
            B收到要不要發動裝備卡的event
            B 發動裝備卡
            八卦陣效果抽到梅花三
            八卦陣效果event isSuccess = false
            B 收到要求出閃的 event
            
            When
            B 出閃
            
            Then
            B hp = 2
            C 收到要求出閃的 event
            """)
    @Test
    public void givenPlayerABC_WhenBUseEquipmentAndBPlayDodge_ThenCReceiveDodgeEvent() {
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Dismantle(SS3003)
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

        Equipment equipmentB = new Equipment();
        equipmentB.setArmor(new EightDiagramTactic(ES2015));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(2))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089)));

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
        game.setCurrentRound(new Round(playerA));

        // player-a 出萬箭齊發
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());
        // player-b 發動裝備效果
        game.playerUseEquipment(playerB.getId(), ES2015.getCardId(), playerA.getId(), EquipmentPlayType.ACTIVE);

        //When
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), BDJ089.getCardId(), playerA.getId(), PlayType.INACTIVE.getPlayType());

        //Then
        AskDodgeEvent askDodgeEvent = getEvent(events, AskDodgeEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-c", askDodgeEvent.getPlayerId());
        assertEquals(2, game.getPlayer("player-b").getHP());
    }

    @DisplayName("""
            Given
            玩家ABC
            A的回合
            B玩家沒有閃
            B玩家 hp = 2
            A玩家出萬箭齊發
            B收到要不要發動裝備卡的event
            B 發動裝備卡
            八卦陣效果抽到梅花三
            八卦陣效果event isSuccess = false
            B 收到要求出閃的 event
            
            When
            B 不出閃
            
            Then
            B hp = 1
            C 收到要求出閃的 event
            """)
    @Test
    public void givenPlayerABC_WhenBUseEquipmentAndBPlaySkip_ThenCReceiveDodgeEvent() {
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Dismantle(SS3003)
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

        Equipment equipmentB = new Equipment();
        equipmentB.setArmor(new EightDiagramTactic(ES2015));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(2))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();

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
        game.setCurrentRound(new Round(playerA));

        // player-a 出萬箭齊發
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());
        // player-b 發動裝備效果
        game.playerUseEquipment(playerB.getId(), ES2015.getCardId(), playerA.getId(), EquipmentPlayType.ACTIVE);

        //When
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        //Then
        AskDodgeEvent askDodgeEvent = getEvent(events, AskDodgeEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-c", askDodgeEvent.getPlayerId());
        assertEquals(1, game.getPlayer("player-b").getHP());
    }

    @DisplayName("""
            Given
            玩家ABC
            A的回合
            B玩家沒有閃
            B玩家 hp = 1
            A玩家出萬箭齊發
            B收到要不要發動裝備卡的event
            
            When
            B 不發動裝備卡
            B 出閃
            
            Then
            C 收到要求出閃的event
            """)
    @Test
    public void givenPlayerABC_WhenBSkipUseEquipmentAndBPlayDodge_ThenCReceiveDodgeEvent() {
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Dismantle(SS3003)
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

        Equipment equipmentB = new Equipment();
        equipmentB.setArmor(new EightDiagramTactic(ES2015));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089)));

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
        game.setCurrentRound(new Round(playerA));

        // player-a 出萬箭齊發
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        // player-b 不發動裝備效果
        game.playerUseEquipment(playerB.getId(), ES2015.getCardId(), playerA.getId(), EquipmentPlayType.SKIP);
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), BDJ089.getCardId(), playerA.getId(), PlayType.INACTIVE.getPlayType());

        //Then
        AskDodgeEvent askDodgeEvent = getEvent(events, AskDodgeEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-c", askDodgeEvent.getPlayerId());
    }

    @DisplayName("""
            Given
            玩家ABC
            A的回合
            B玩家沒有閃
            B玩家 hp = 2
            A玩家出萬箭齊發
            B收到要不要發動裝備卡的event
            
            When
            B 不發動裝備卡
            B 不出閃
            
            Then
            B玩家 hp = 1
            C 收到要求出閃的event
            """)
    @Test
    public void givenPlayerABC_WhenBSkipUseEquipmentAndBSkipPlayDodge_ThenCReceiveDodgeEvent() {
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Dismantle(SS3003)
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

        Equipment equipmentB = new Equipment();
        equipmentB.setArmor(new EightDiagramTactic(ES2015));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(2))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Dodge(BDJ089)));

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
        game.setCurrentRound(new Round(playerA));

        // player-a 出萬箭齊發
        game.playerPlayCard(playerA.getId(), SHA040.getCardId(), "", PlayType.ACTIVE.getPlayType());

        //When
        // player-b 不發動裝備效果
        game.playerUseEquipment(playerB.getId(), ES2015.getCardId(), playerA.getId(), EquipmentPlayType.SKIP);
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        //Then
        AskDodgeEvent askDodgeEvent = getEvent(events, AskDodgeEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-c", askDodgeEvent.getPlayerId());
        assertEquals(1, game.getPlayer("player-b").getHP());
    }


}

package com.gaas.threeKingdoms;


import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
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
import static org.junit.jupiter.api.Assertions.*;

public class PlayerDyingTest {

    @DisplayName("""
            Given 
            A 血量等於 1
            B C D 沒有死亡
                        
            When
            A 玩家被 B 玩家出殺且沒有閃
               
            Then
            A 玩家已瀕臨死亡且狀態是 Dying
                        
            Then
            系統向所有玩家發出 player-b PlayCardEvent
            系統向所有玩家發出 player-a PlayerDamagedEvent
            系統向所有玩家發出 PlayerDyingEvent, playerId = "player-a"
            系統向所有玩家發出 AskPeachEvent, playerId = "player-a"
            activePlayer = "player-b"
            currentRoundPlayer = "player-b"
            dyingPlayer = "player-a"
            RoundPhase(Action)
            GamePhase(GeneralDying)
            """)
    @Test
    public void playerAHas1HP_WhenPlayerAPlayCardSkip_ThenPlayerADyingAndAskPlayerBPeach() {
        //Given
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();


        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerB));
        game.enterPhase(new Normal(game));

        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");

        //When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");
        //Then
        PlayCardEvent playCardEvent = getEvent(events, PlayCardEvent.class).orElseThrow(RuntimeException::new);
        PlayerDamagedEvent playerDamagedEvent = getEvent(events, PlayerDamagedEvent.class).orElseThrow(RuntimeException::new);
        PlayerDyingEvent playerDyingEvent = getEvent(events, PlayerDyingEvent.class).orElseThrow(RuntimeException::new);
        AskPeachEvent askPeachEvent = getEvent(events, AskPeachEvent.class).orElseThrow(RuntimeException::new);
        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow(RuntimeException::new);

        assertNotNull(playCardEvent);
        RoundEvent roundEvent = gameStatusEvent.getRound();
        assertEquals("player-a", playerDamagedEvent.getPlayerId());
        assertEquals("player-a", playerDyingEvent.getPlayerId());
        assertEquals("player-a", askPeachEvent.getPlayerId());
        assertEquals("player-a", roundEvent.getDyingPlayer());

        assertEquals("player-b", roundEvent.getCurrentRoundPlayer());
        assertEquals("player-a", roundEvent.getActivePlayer());
        assertEquals("GeneralDying", game.getGamePhase().getPhaseName());
    }

    @DisplayName("""
            Given
            A 玩家已瀕臨死亡且狀態是 Dying
            A 玩家被詢問要不要出桃救A
                        
            When
            A玩家不出桃救A
               
            Then
            B玩家被詢問要不要出桃救A
            系統向所有玩家發出 player-a PlayCardEvent
            系統向所有玩家發出 AskPeachEvent, playerId = "player-b"
            activePlayer = "player-a"
            currentRoundPlayer = "player-a"
            dyingPlayer = "player-a"
            RoundPhase(Action)
            GamePhase(Normal)
            """)
    @Test
    public void playerADying_WhenPlayerAPlayCardSkip_ThenAskPlayerBPeach() {
        //Given
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();


        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerB));
        game.enterPhase(new Normal(game));

        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");

        //When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), "", playerA.getId(), "skip");

        //Then
        PlayCardEvent playCardEvent = getEvent(events, PlayCardEvent.class).orElseThrow(RuntimeException::new);
        AskPeachEvent askPeachEvent = getEvent(events, AskPeachEvent.class).orElseThrow(RuntimeException::new);
        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow(RuntimeException::new);

        assertNotNull(playCardEvent);
        RoundEvent roundEvent = gameStatusEvent.getRound();
        assertEquals("player-b", askPeachEvent.getPlayerId());
        assertEquals("player-a", roundEvent.getDyingPlayer());

        assertEquals("player-b", roundEvent.getCurrentRoundPlayer());
        assertEquals("player-b", roundEvent.getActivePlayer());
        assertEquals("GeneralDying", game.getGamePhase().getPhaseName());
    }

    @DisplayName("""
            Given
            A 玩家已瀕臨死亡且狀態是 Dying
            A 玩家被詢問要不要出桃救A A skip
            B 玩家被詢問要不要出桃救A
                        
            When
            B玩家不出桃救A
               
            Then
            C玩家被詢問要不要出桃救A
            系統向所有玩家發出 player-b PlayCardEvent
            系統向所有玩家發出 AskPeachEvent, playerId = "player-c"
            activePlayer = "player-c"
            currentRoundPlayer = "player-b"
            dyingPlayer = "player-a"
            RoundPhase(Action)
            GamePhase(Normal)
            """)
    @Test
    public void playerADying_WhenPlayerBPlayCardSkip_ThenAskPlayerCPeach() {
        //Given
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();


        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerB));
        game.enterPhase(new Normal(game));

        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");

        //When
        game.playerPlayCard(playerA.getId(), "", playerA.getId(), "skip");
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        //Then
        PlayCardEvent playCardEvent = getEvent(events, PlayCardEvent.class).orElseThrow(RuntimeException::new);
        AskPeachEvent askPeachEvent = getEvent(events, AskPeachEvent.class).orElseThrow(RuntimeException::new);
        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow(RuntimeException::new);

        assertNotNull(playCardEvent);
        RoundEvent roundEvent = gameStatusEvent.getRound();
        assertEquals("player-c", askPeachEvent.getPlayerId());
        assertEquals("player-a", roundEvent.getDyingPlayer());

        assertEquals("player-b", roundEvent.getCurrentRoundPlayer());
        assertEquals("player-c", roundEvent.getActivePlayer());
        assertEquals("GeneralDying", game.getGamePhase().getPhaseName());
    }

    @DisplayName("""
            Given
            A 玩家已瀕臨死亡且狀態是 Dying
            B 玩家被詢問要不要出桃救A
            B 玩家不出桃救A
           
            When
            C 玩家不出桃救A
            
            Then
            D 玩家被詢問要不要出桃救A
            系統向所有玩家發出 player-b PlayCardEvent
            系統向所有玩家發出 AskPeachEvent, playerId = "player-d"
            activePlayer = "player-d"
            currentRoundPlayer = "player-b"
            dyingPlayer = "player-a"
            RoundPhase(Action)
            GamePhase(Normal)
            """)
    @Test
    public void playerADying_WhenPlayerCPlayCardSkip_ThenAskPlayerDPeach() {
        //Given
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();


        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerB));
        game.enterPhase(new Normal(game));

        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");
        game.playerPlayCard(playerA.getId(), "", playerA.getId(), "skip");
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        //When
        List<DomainEvent> events = game.playerPlayCard(playerC.getId(), "", playerA.getId(), "skip");

        //Then
        PlayCardEvent playCardEvent = getEvent(events, PlayCardEvent.class).orElseThrow(RuntimeException::new);
        AskPeachEvent askPeachEvent = getEvent(events, AskPeachEvent.class).orElseThrow(RuntimeException::new);
        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow(RuntimeException::new);

        assertNotNull(playCardEvent);
        RoundEvent roundEvent = gameStatusEvent.getRound();
        assertEquals("player-d", askPeachEvent.getPlayerId());
        assertEquals("player-a", roundEvent.getDyingPlayer());

        assertEquals("player-b", roundEvent.getCurrentRoundPlayer());
        assertEquals("player-d", roundEvent.getActivePlayer());
        assertEquals("GeneralDying", game.getGamePhase().getPhaseName());
    }

    @DisplayName("""
            Given
            A 是主公，B 玩家是反賊，C玩家是忠臣，D玩家是內奸
            A 玩家已瀕臨死亡且狀態是 Dying
            A 不出桃救自己
            B 玩家被詢問要不要出桃救A
            B 玩家不出桃救A
            C 玩家不出桃救A
                        
            When
            D 玩家不出桃救A
                        
            Then
            系統向所有玩家發出 player-d PlayCardEvent
            系統向所有玩家發出 SettlementEvent - A 玩家已死亡, 死亡玩家爲主公
            系統向所有玩家發出 GaveOverEvent
            GamePhase(GameOver)
            """)
    @Test
    public void playerADying_WhenPlayerDPlayCardSkip_ThenGameOver() {
        //Given
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();


        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerB));
        game.enterPhase(new Normal(game));

        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");
        game.playerPlayCard(playerC.getId(), "", playerA.getId(), "skip");

        //When
        List<DomainEvent> events = game.playerPlayCard(playerD.getId(), "", playerA.getId(), "skip");

        //Then
        SettlementEvent settlementEvent = getEvent(events, SettlementEvent.class).orElseThrow(RuntimeException::new);
        GameOverEvent gameOverEvent = getEvent(events, GameOverEvent.class).orElseThrow(RuntimeException::new);

        assertNotNull(settlementEvent);
        assertNotNull(gameOverEvent);

        assertEquals("player-a", settlementEvent.getPlayerId());
        assertEquals("劉備 死亡，身分是 主公", settlementEvent.getMessage());

        String gameOverMessage =
                """
                        player-a Monarch
                        player-b Minister
                        player-c Minister
                        player-d Minister
                        反賊獲勝""";
        assertEquals(gameOverMessage, gameOverEvent.getMessage());
        assertEquals("GameOver", game.getGamePhase().getPhaseName());
    }


    @DisplayName("""
            Given
            A 是反賊，B 玩家是主公，C玩家是忠臣，D玩家是內奸
            A 玩家已瀕臨死亡且狀態是 Dying
            A 不出桃救自己
            B 玩家不出桃救A
            C 玩家不出桃救A
                        
            When
            D 玩家不出桃救 A
                        
            Then
            系統向所有玩家發出 player-d PlayCardEvent
            SeatingChart 裡沒有player-a
            """)
    @Test
    public void playerADying2_WhenPlayerDPlayCardSkip_ThenGameOver() {
        //Given
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerB));
        game.enterPhase(new Normal(game));

        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");
        game.playerPlayCard(playerC.getId(), "", playerA.getId(), "skip");

        //When
        List<DomainEvent> events = game.playerPlayCard(playerD.getId(), "", playerA.getId(), "skip");

        //Then
        assertFalse(game.getSeatingChart().getPlayers().stream().anyMatch(seat -> seat.getId().equals("player-a")));
    }
}


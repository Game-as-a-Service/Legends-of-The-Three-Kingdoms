package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
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

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class PlayerDyingAndPlayPeachTest {


    @DisplayName("""
            Given 
            A 血量等於 1，A有一張桃
            B C D 沒有死亡
            A 玩家被 B 玩家出殺且沒有閃
            A 玩家已瀕臨死亡且狀態是 Dying
            詢問A玩家是否出桃
                        
            When
            A 玩家出桃
                                    
            Then
            A 玩家 HP = 1, 狀態爲 alive
            還是 B 的回合
            """)
    @Test
    public void playerADying_WhenPlayerAPlayPeach_ThenPlayerAlive() {
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
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)));

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

        // B對A出殺
        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");

        // A玩家出skip
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");

        //When A對A自己出桃
        game.playerPlayCard(playerA.getId(), BH3029.getCardId(), playerA.getId(), "active");


        //Then
        Round currentRound = game.getCurrentRound();
        assertEquals(1, playerA.getHP());
        assertEquals("player-b", currentRound.getActivePlayer().getId());
        assertEquals("player-b", currentRound.getCurrentRoundPlayer().getId());
        assertNull(currentRound.getDyingPlayer());
        assertEquals("Normal", game.getGamePhase().getPhaseName());
    }

    @DisplayName("""
            Given 
            A 血量等於 1，A有一張桃
            B C D 沒有死亡
            A 玩家被 B 玩家出殺且沒有閃
            A 玩家已瀕臨死亡且狀態是 Dying
            詢問A玩家要不要出桃 A玩家沒有桃
            詢問B玩家要不要出桃
                        
            When
            B 玩家出桃
                                    
            Then
            A 玩家 HP = 1, 狀態爲 alive
            還是 B 的回合
            """)
    @Test
    public void playerADying_WhenPlayerBPlayPeach_ThenPlayerAlive() {
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
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)));

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

        // B對A出殺
        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");

        // A玩家出skip
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");

        // A玩家沒有桃出 skip
        game.playerPlayCard(playerA.getId(), "", playerA.getId(), "skip");

        //When B對A出桃
        game.playerPlayCard(playerB.getId(), BH3029.getCardId(), playerA.getId(), "active");


        //Then
        Round currentRound = game.getCurrentRound();
        assertEquals(1, playerA.getHP());
        assertEquals("player-b", currentRound.getActivePlayer().getId());
        assertEquals("player-b", currentRound.getCurrentRoundPlayer().getId());
        assertNull(currentRound.getDyingPlayer());
        assertEquals("Normal", game.getGamePhase().getPhaseName());
    }


    @DisplayName("""
            Given 
            A 血量等於 1，A有一張桃
            B C D 沒有死亡
            A 玩家被 B 玩家出殺且沒有閃
            A 玩家已瀕臨死亡且狀態是 Dying
            詢問A玩家要不要出桃 A玩家沒有桃
            詢問B玩家要不要出桃 B玩家沒有桃
            詢問C玩家要不要出桃
                        
            When
            C 玩家出桃
                                    
            Then
            A 玩家 HP = 1, 狀態爲 alive
            還是 B 的回合
            """)
    @Test
    public void playerADying_WhenPlayerCPlayPeach_ThenPlayerAlive() {
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
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)));

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

        playerC.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)));

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

        // B對A出殺
        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");

        // A玩家出skip
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");

        // A玩家沒有桃出 skip
        game.playerPlayCard(playerA.getId(), "", playerA.getId(), "skip");

        // B玩家沒有桃出 skip
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        //When C對A出桃
        game.playerPlayCard(playerC.getId(), BH3029.getCardId(), playerA.getId(), "active");

        //Then
        Round currentRound = game.getCurrentRound();
        assertEquals(1, playerA.getHP());
        assertEquals("player-b", currentRound.getActivePlayer().getId());
        assertEquals("player-b", currentRound.getCurrentRoundPlayer().getId());
        assertNull(currentRound.getDyingPlayer());
        assertEquals("Normal", game.getGamePhase().getPhaseName());
    }


    @DisplayName("""
            Given 
            A 血量等於 1，A有一張桃
            B C D 沒有死亡
            A 玩家被 B 玩家出殺且沒有閃
            A 玩家已瀕臨死亡且狀態是 Dying
            詢問A玩家要不要出桃 A玩家沒有桃
            詢問B玩家要不要出桃 B玩家沒有桃
            詢問C玩家要不要出桃 C玩家沒有桃
            詢問D玩家要不要出桃
                        
            When
            D 玩家出桃
                                    
            Then
            A 玩家 HP = 1, 狀態爲 alive
            還是 B 的回合
            """)
    @Test
    public void playerADying_WhenPlayerDPlayPeach_ThenPlayerAlive() {
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
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039)));

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

        playerC.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        playerD.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)));

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerB));
        game.enterPhase(new Normal(game));

        // B對A出殺
        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), "active");

        // A玩家出skip
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), "skip");

        // A玩家沒有桃出 skip
        game.playerPlayCard(playerA.getId(), "", playerA.getId(), "skip");

        // B玩家沒有桃出 skip
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        // C玩家沒有桃出 skip
        game.playerPlayCard(playerC.getId(), "", playerA.getId(), "skip");

        //When D對A出桃
        game.playerPlayCard(playerD.getId(), BH3029.getCardId(), playerA.getId(), "active");

        //Then
        Round currentRound = game.getCurrentRound();
        assertEquals(1, playerA.getHP());
        assertEquals("player-b", currentRound.getActivePlayer().getId());
        assertEquals("player-b", currentRound.getCurrentRoundPlayer().getId());
        assertNull(currentRound.getDyingPlayer());
        assertEquals("Normal", game.getGamePhase().getPhaseName());
    }
}

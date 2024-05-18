package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayRepeatingCrossbowTest {

    @DisplayName("""
         Given
         A的回合
         A有一張諸葛連弩


         When
         A出諸葛連弩

         Then
         A玩家裝備卡有諸葛連弩
            """)
    @Test
    public void givenPlayerAHaveRepeatingCrossbow_WhenPlayerAPlayRepeatingCrossbow_ThenPlayerAEquipRepeatingCrossbow() {
        Game game = new Game();

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withEquipment(new Equipment())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS8009), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new RepeatingCrossbowCard(ECA066)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        game.playerPlayCard(playerA.getId(), ECA066.getCardId(), playerA.getId(), "active");

        //Then
        assertEquals(ECA066.getCardId(), game.getPlayer("player-a").getRquipmentWeaponCard().getId());
    }

    @DisplayName("""
         Given
         玩家ABCD
         A的回合
         A有二張殺
         A有一張諸葛連弩
         B沒有閃 B HP =4
         D沒有閃 D HP =4

         When
         A對B出殺
         A對D出殺

         Then
         B HP=3
         D HP=3
            """)
    @Test
    public void givenPlayerAHaveRepeatingCrossbow_WhenPlayerAPlayKillToBAndD_ThenPlayerBandDHPEqualThree() {
        Game game = new Game();

        Equipment equipment = new Equipment();
        equipment.setWeapon(new RepeatingCrossbowCard(ECA066));

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withEquipment(equipment)
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS8009), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), "active");
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        assertEquals("player-a", game.getActivePlayer().getId());

        game.playerPlayCard(playerA.getId(), BS8009.getCardId(), playerD.getId(), "active");
        game.playerPlayCard(playerD.getId(), "", playerA.getId(), "skip");

        //Then
        assertEquals(3, game.getPlayer("player-b").getHP());
        assertEquals(3, game.getPlayer("player-d").getHP());
    }

    @DisplayName("""
         Given
         A的回合
         A已經裝備諸葛連弩
         A有四張殺
         B玩家沒有閃
         B玩家HP=4
    
         When
         A對B出殺四次


         Then
         B玩家HP=0
        
            """)
    @Test
    public void givenPlayerAHaveRepeatingCrossbow_WhenPlayerAPlayKillToBFourTimesAndD_ThenPlayerBHPEqualZero() {
        Game game = new Game();

        Equipment equipment = new Equipment();
        equipment.setWeapon(new RepeatingCrossbowCard(ECA066));

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withEquipment(equipment)
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS8009), new Kill(BS8010), new Kill(BS7020), new Dodge(BH2028)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), "active");
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        game.playerPlayCard(playerA.getId(), BS8009.getCardId(), playerB.getId(), "active");
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        game.playerPlayCard(playerA.getId(), BS8010.getCardId(), playerB.getId(), "active");
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        game.playerPlayCard(playerA.getId(), BS7020.getCardId(), playerB.getId(), "active");
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        //Then
        assertEquals(0, game.getPlayer("player-b").getHP());
    }

}

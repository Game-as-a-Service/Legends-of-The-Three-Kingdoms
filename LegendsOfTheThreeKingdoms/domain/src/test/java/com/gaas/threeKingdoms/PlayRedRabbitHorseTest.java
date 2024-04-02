package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PlayRedRabbitHorseTest {


    @DisplayName("""
         Given
         A的回合
         A有一張赤兔馬 (-1)


         When
         A出赤兔馬

         Then
         A玩家裝備卡有赤兔馬
            """)
    @Test
    public void givenPlayerAHaveRedRabbitHorse_WhenPlayerAPlayRedRabbitHorse_ThenPlayerAEquipRedRabbitHorse() {
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
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS8009), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new RedRabbitHorse(EH5044)));

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
        game.playerPlayCard(playerA.getId(), EH5044.getCardId(), playerA.getId(), "active");

        //Then
        assertEquals(EH5044.getCardId(), game.getPlayer("player-a").getEquipmentMinusOneMountsCard().getId());
    }

    @DisplayName("""
        Given
        A的回合
        A的裝備已有一張赤兔馬 (♠5)
        A的手牌也有一張赤兔馬 (♥5)
        
        When
        A出赤兔馬 (♥5)
        
        Then
        A的裝備為赤兔馬 (♥5)
            """)
    @Test
    public void testRedRabbitHorseCoverageEffect() {
        Game game = new Game();
        Equipment equipment = new Equipment();
        equipment.setMinusOne(new RedRabbitHorse(EH5044));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new RedRabbitHorse(EH5044)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

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
        game.playerPlayCard(playerA.getId(), EH5044.getCardId(), playerA.getId(), "active");


        assertEquals(new RedRabbitHorse(EH5044), game.getPlayer("player-a").getEquipmentMinusOneMountsCard());
    }

    @DisplayName("""
        Given
        A的回合
        A有一張赤兔馬 (-1)
        A出赤兔馬
        C玩家沒有閃
        C玩家Hp = 4
        
        When
        A玩家攻擊C玩家
        
        Then
        C玩家HP = 3
        """)
    @Test
    public void givenPlayerAHasRedRabbitHorse_WhenPlayerAAttackPlayerC_ThenCHpIs3(){
        Game game = new Game();
        Equipment equipment = new Equipment();
        equipment.setMinusOne(new RedRabbitHorse(EH5044));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new RedRabbitHorse(EH5044)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerC.getId(), "active");
        game.playerPlayCard(playerC.getId(), "", playerA.getId(), "skip");

        assertEquals(3, game.getPlayer("player-c").getHP());
    }

    @DisplayName("""
        Given
        有ABCDEF玩家
        A的回合
        A有一張赤兔馬 (-1)
        A出赤兔馬
        
        When
        A玩家攻擊D玩家
        
        Then
        噴出例外
        """)
    @Test
    public void givenPlayerAHasRedRabbitHorse_WhenPlayerAAttackPlayerD_ThenExceptionError(){
        Game game = new Game();
        Equipment equipment = new Equipment();
        equipment.setMinusOne(new RedRabbitHorse(EH5044));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new RedRabbitHorse(EH5044)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .build();

        Player playerE = PlayerBuilder.construct()
                .withId("player-e")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .build();

        Player playerF = PlayerBuilder.construct()
                .withId("player-f")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD, playerE, playerF);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        //Then
        assertThrows(IllegalStateException.class,
                () -> game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerD.getId(), "active"));
    }
}

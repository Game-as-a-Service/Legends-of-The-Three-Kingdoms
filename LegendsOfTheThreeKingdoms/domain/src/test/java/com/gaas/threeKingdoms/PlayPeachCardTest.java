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
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayPeachCardTest {


    @DisplayName("""
            Given
            A 玩家手牌有殺x1, 閃x2, 桃x2
            A玩家為劉備 3HP，上限4HP

            When
            Ａ出 桃

            Then
            A 玩家手牌爲 殺x1, 閃x2, 桃x1
            A玩家4HP
                    """)
    @Test
    public void givenPlayerAWithPeach_WhenPlayerAPlayPeach_ThenPlayerAAddHP() {

        //Given
        Game game = new Game();

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        playerA.damage(1);

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        game.playerPlayCard(playerA.getId(), BH3029.getCardId(), playerA.getId(), "active");

        //Then
        assertEquals(4, game.getPlayer("player-a").getBloodCard().getHp());
    }


    @DisplayName("""
            Given
            A 玩家手牌有殺x1, 閃x2, 桃x2
            A玩家為劉備 4HP，上限4HP

            When
            Ａ出 桃

            Then
            A 玩家手牌爲 殺x1, 閃x2, 桃x1
            A玩家4HP
                    """)
    @Test
    public void givenPlayerAWithMaxHP_WhenPlayerAPlayPeach_ThenPlayerAAddHP() {

        //Given
        Game game = new Game();

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        game.playerPlayCard(playerA.getId(), BH3029.getCardId(), playerA.getId(), "active");

        //Then
        assertEquals(4, game.getPlayer("player-a").getBloodCard().getHp());
    }

}

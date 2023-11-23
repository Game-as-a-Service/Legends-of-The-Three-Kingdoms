package com.waterball.LegendsOfTheThreeKingdoms.domain.unittest;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Round;
import com.waterball.LegendsOfTheThreeKingdoms.domain.RoundPhase;
import com.waterball.LegendsOfTheThreeKingdoms.domain.builders.PlayerBuilder;
import com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase.Normal;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.Deck;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Dodge;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Kill;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.equipmentcard.armorcard.EightDiagrams;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.BloodCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Hand;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.HealthStatus;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.RoleCard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayCard.*;
import static java.util.Arrays.asList;

public class DiscardCardTest {

    @DisplayName("""
            Given
            A 玩家棄牌階段
            A 玩家手牌爲 殺x4 閃x1
            A 玩家血量hp 為 4
                    
            When
            A 玩家棄閃x1
                    
            Then
            A 玩家手牌有殺x4
            RoundPhase 是 Action
            currentRoundPlayer 是 B 玩家
            """)
    @Test
    public void givenAPlayerWithKillAndDodge_WhenDiscardCard_ThenAPlayerOnlyWithKill() {
        //Given
        var game = new Game();

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard("SHU001", "劉備", 4))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .build();

        Player playerB = PlayerBuilder
                .construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard("WEI002", "司馬懿", 3))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .build();

        Player playerC = PlayerBuilder
                .construct()
                .withId("player-C")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard("WEI002", "司馬懿", 3))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .build();

        Player playerD = PlayerBuilder
                .construct()
                .withId("player-D")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard("WEI002", "司馬懿", 3))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .build();

        List<Player> players = asList(
                playerA,
                playerB,
                playerC,
                playerD);

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS8009), new Kill(BH0036), new Kill(BC4056), new Dodge(BHK039)));

        game.setPlayers(players);
        Round currentRound = new Round(playerA);
        currentRound.setRoundPhase(RoundPhase.Discard);
        game.setCurrentRound(currentRound);
        game.enterPhase(new Normal(game));;

        //When
        game.playerDiscardCard(asList("BHK039"));

        //Then
        Assertions.assertEquals(Arrays.asList(new Kill(BS8008), new Kill(BS8009), new Kill(BH0036), new Kill(BC4056)), game.getPlayer("player-a").getHand().getCards());
        Assertions.assertEquals(RoundPhase.Action, game.getCurrentRoundPhase());
        Assertions.assertEquals("player-b", game.getCurrentRoundPlayer().getId());

    }

}

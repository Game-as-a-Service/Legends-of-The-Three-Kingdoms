package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.RoundPhase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
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
        game.initDeck();

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withDelayScrollCards(new Stack<>())
                .build();

        Player playerB = PlayerBuilder
                .construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.司馬懿))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withDelayScrollCards(new Stack<>())
                .build();

        Player playerC = PlayerBuilder
                .construct()
                .withId("player-C")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.司馬懿))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .withDelayScrollCards(new Stack<>())
                .build();

        Player playerD = PlayerBuilder
                .construct()
                .withId("player-D")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.司馬懿))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .withDelayScrollCards(new Stack<>())
                .build();

        List<Player> players = asList(
                playerA,
                playerB,
                playerC,
                playerD);

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009), new Kill(BH0036), new Kill(BC4056), new Dodge(BHK039)));

        game.setPlayers(players);
        Round currentRound = new Round(playerA);
        currentRound.setRoundPhase(RoundPhase.Discard);
        game.setCurrentRound(currentRound);
        game.enterPhase(new Normal(game));;

        //When
        game.playerDiscardCard(asList("BHK039"));

        //Then
        Assertions.assertEquals(Arrays.asList(new Kill(BS8008), new Kill(BS9009), new Kill(BH0036), new Kill(BC4056)), game.getPlayer("player-a").getHand().getCards());
        Assertions.assertEquals(RoundPhase.Action, game.getCurrentRoundPhase());
        Assertions.assertEquals("player-b", game.getCurrentRoundPlayer().getId());

    }

}

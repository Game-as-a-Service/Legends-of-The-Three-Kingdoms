package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.DrawCardEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.SomethingForNothing;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SomethingForNothingTest {

    @DisplayName("""
            Given
            A的回合
            A的手牌共有 3 張
            A的手牌也有一張無中生有
                    
            When
            A 出無中生有
                    
            Then
            A的手牌為 4 張
            有 DrawCardEvent 
                """)
    @Test
    public void givenPlayerAHasQilinBow_WhenPlayerAPlayQilinBow_ThenPlayerAEquipQilinBow() {
        Game game = new Game();
        game.initDeck();
        game.setDeck(new Deck(List.of(new BarbarianInvasion(SSK013), new Dismantle(SS3003))));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new SomethingForNothing(SH7046)));

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
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SH7046.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        //Then
        DrawCardEvent drawCardEvent = (DrawCardEvent) events.stream().filter(event -> event instanceof DrawCardEvent).findFirst().get();
        assertEquals(2, drawCardEvent.getSize());
        assertEquals("player-a", drawCardEvent.getDrawCardPlayerId());
        assertEquals(4, playerA.getHandSize());
        assertTrue(playerA.getHand().getCard(BS8008.getCardId()).isPresent());
        assertTrue(playerA.getHand().getCard(BH3029.getCardId()).isPresent());
        assertTrue(playerA.getHand().getCard(SH7046.getCardId()).isEmpty());
        assertTrue(playerA.getHand().getCard(SSK013.getCardId()).isPresent());
        assertTrue(playerA.getHand().getCard(SS3003.getCardId()).isPresent());


    }

}

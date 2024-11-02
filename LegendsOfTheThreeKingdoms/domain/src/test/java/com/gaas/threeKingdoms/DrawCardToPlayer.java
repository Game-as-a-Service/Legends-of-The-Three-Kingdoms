package com.gaas.threeKingdoms;


import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.Graveyard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;

public class DrawCardToPlayer {

    @DisplayName("""
            Given
            輪到 A 玩家摸牌
            A 玩家有殺 x 1
            牌堆有八卦陣 x 2
                    
            When
            系統讓A 玩家摸牌
                    
            Then
            A 玩家手牌有
            殺 x 1, 八卦陣 x 2
            """)
    @Test
    public void givenAPlayerWithKillAndEightDiagrams_WhenDrawCard_ThenPlayerHaveKillAndEightDiagrams() {
        //Given
        var game = new Game();
        game.initDeck();

        Player player = PlayerBuilder
                .construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        player.getHand().addCardToHand(Arrays.asList(new Kill(BH0036)));

        List<Player> players = asList(player);
        game.setPlayers(players);
        Deck deck = new Deck(new Stack());
        for (int i = 0; i < 2; i++) {
            deck.add(Arrays.asList(new EightDiagramTactic(ES2015), new EightDiagramTactic(ES2015)));
        }
        game.setDeck(deck);
        game.setCurrentRound(new Round(player));
        game.enterPhase(new Normal(game));

        //When
        game.drawCardToPlayer(player);

        //Then
        Assertions.assertEquals(Arrays.asList(new Kill(BH0036), new EightDiagramTactic(ES2015), new EightDiagramTactic(ES2015)), game.getPlayer("player-a").getHand().getCards());

    }

    @DisplayName("""
            Given
            牌堆 0張
            棄牌堆2張殺
            A玩家有閃x1
                    
            When
            A玩家摸牌
                    
            Then
            A玩家手牌有閃x1,殺x2
            棄牌堆0張
            牌堆0張
                    
            """)
    @Test
    public void givenEmptyZeroAndDiscardDeckHaveTwo_WhenPlayerDrawCard_ThenPlayerHaveThreeCards() {
        // given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(new Stack<>());
        Graveyard graveyard = new Graveyard();
        graveyard.add(Arrays.asList(new Kill(BH0036), new Kill(BH0036)));

        Player player = PlayerBuilder
                .construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        player.getHand().addCardToHand(Arrays.asList(new Dodge(BHK039)));

        List<Player> players = asList(
                player);

        game.setDeck(deck);
        game.setPlayers(players);
        game.setGraveyard(graveyard);
        game.setCurrentRound(new Round(player));
        game.enterPhase(new Normal(game));

        // when
        game.drawCardToPlayer(player);

        // then
        Assertions.assertTrue(Utils.compareArrayLists(Arrays.asList(new Kill(BH0036), new Kill(BH0036), new Dodge(BHK039)), game.getPlayer("player-a").getHand().getCards()));
        Assertions.assertTrue(deck.isDeckLessThanCardNum(1));
        Assertions.assertTrue(graveyard.isEmpty());
    }

}

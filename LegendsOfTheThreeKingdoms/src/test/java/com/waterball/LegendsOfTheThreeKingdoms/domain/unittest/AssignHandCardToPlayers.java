package com.waterball.LegendsOfTheThreeKingdoms.domain.unittest;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.builders.PlayerBuilder;
import com.waterball.LegendsOfTheThreeKingdoms.domain.generalcard.GeneralCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.Deck;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.HandCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Dodge;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Kill;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Peach;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.BloodCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Hand;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.HealthStatus;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayCard.*;
import static java.util.Arrays.asList;

public class AssignHandCardToPlayers {

    @DisplayName("""
            Given
            4 位玩家分配完體力
            有 xx 張手牌在 deck
            基礎牌 殺 x 8, 閃 x 8, 桃 x 8
            裝備牌 八卦陣 x 2, 諸葛連怒 x 2
            錦囊牌 無中生有 x 4, 順手牽羊 x 2
                    
            When
            系統發牌
                    
            Then
            每位玩家有四張手牌
            A:殺 殺 殺 殺
            B:殺 殺 殺 殺
            C:閃 閃 閃 閃
            D:閃 閃 閃 閃
            """)
    @Test
    void givenFourPlayers_WhenAssignHandCard_ThenPlayersHaveFourSpecificHandCard() {
        //Given
        var game = new Game();
        List<Player> players = asList(
                PlayerBuilder.construct()
                        .withId("player-a")
                        .build(),
                PlayerBuilder.construct()
                        .withId("player-b")
                        .build(),
                PlayerBuilder.construct()
                        .withId("player-c")
                        .build(),
                PlayerBuilder.construct()
                        .withId("player-d")
                        .build());


        game.setPlayers(players);
        Stack<HandCard> stack = new Stack<>();

        for (int i = 0; i < 8; i++) {
            stack.push(new Dodge(BHK039));
        }
        for (int i = 0; i < 8; i++) {
            stack.push(new Kill(BHJ037));
        }
        var deck = new Deck(stack);
        game.setDeck(deck);

        // when
        game.assignHandCardToPlayers();

        // then
        Assertions.assertEquals(game.getPlayer("player-a").getHand().getCards(), Arrays.asList(new Kill(BHJ037), new Kill(BHJ037), new Kill(BHJ037), new Kill(BHJ037)));
        Assertions.assertEquals(game.getPlayer("player-b").getHand().getCards(), Arrays.asList(new Kill(BHJ037), new Kill(BHJ037), new Kill(BHJ037), new Kill(BHJ037)));
        Assertions.assertEquals(game.getPlayer("player-c").getHand().getCards(), Arrays.asList(new Dodge(BHK039), new Dodge(BHK039), new Dodge(BHK039), new Dodge(BHK039)));
        Assertions.assertEquals(game.getPlayer("player-d").getHand().getCards(), Arrays.asList(new Dodge(BHK039), new Dodge(BHK039), new Dodge(BHK039), new Dodge(BHK039)));
    }
}

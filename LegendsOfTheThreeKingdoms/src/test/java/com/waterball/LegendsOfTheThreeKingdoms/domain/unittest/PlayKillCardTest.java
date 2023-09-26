package com.waterball.LegendsOfTheThreeKingdoms.domain.unittest;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Round;
import com.waterball.LegendsOfTheThreeKingdoms.domain.builders.Players;
import com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase.GamePhase;
import com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase.Normal;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Dodge;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Kill;
import com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.basiccard.Peach;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.BloodCard;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.waterball.LegendsOfTheThreeKingdoms.domain.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class PlayKillCardTest {

    @DisplayName("""
        Given
        輪到 A 玩家出牌
        A 玩家手牌有殺x2, 閃x2, 桃x2, HP 4
        C 玩家不在 A 玩家的攻擊距離, HP 4
        
        When
        A 玩家對 C 玩家出殺
        
        Then
        A 玩家出殺失敗
        C 玩家 HP 4
        """)
    @Test
    public void givenPlayerAAndPlayerCDistance2_WhenPlayerAKillB_ThenPlayerAFail() {
        Game game = new Game();
        Player playerA = Players.defaultPlayer("player-a");
        Player playerB = Players.defaultPlayer("player-b");
        Player playerC = Players.defaultPlayer("player-c");
        Player playerD = Players.defaultPlayer("player-d");

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Kill(BS8009), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));
        playerA.setBloodCard(new BloodCard(4));
        playerC.setBloodCard(new BloodCard(4));
        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));

        //Todo 補上自訂義的 Exception
        assertThrows(IllegalStateException.class,
                () -> game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerC.getId(), "active"));
        assertEquals(4, game.getPlayer("player-c").getBloodCard().getHp());
    }

    @DisplayName("""
        Given
        輪到 A 玩家出牌
        A 玩家對 B 玩家已出過殺
        A 玩家手牌有殺x1, 閃x2, 桃x2
        
        When
        A 玩家對 B 玩家出殺
        
        Then
        A 玩家出殺失敗
        A 玩家手牌爲 殺x1, 閃x2, 桃x2
        A 玩家已出殺的狀態為true
        """)
    @Test
    public void givenPlayerAKilledPlayerB_WhenPlayerAKillB_ThenPlayerAFail() {
        //Given
        Game game = new Game();
        Player playerA = Players.defaultPlayer("player-a");
        Player playerB = Players.defaultPlayer("player-b");
        Player playerC = Players.defaultPlayer("player-c");
        Player playerD = Players.defaultPlayer("player-d");

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Kill(BS8009), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));
        playerA.setBloodCard(new BloodCard(4));
        playerB.setBloodCard(new BloodCard(4));
        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), "active");

        //When Then
        assertThrows(IllegalStateException.class,
                () -> game.playerPlayCard(playerA.getId(), BS8009.getCardId(), playerB.getId(), "active"));
        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
        Assertions.assertTrue(Utils.compareArrayLists(Arrays.asList(
                new Kill(BS8009), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)), game.getPlayer("player-a").getHand().getCards()));
        assertTrue(game.getCurrentRound().isShowKill());
    }
}

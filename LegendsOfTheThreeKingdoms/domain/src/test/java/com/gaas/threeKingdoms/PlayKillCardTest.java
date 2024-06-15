package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.exception.DistanceErrorException;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
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

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand(Arrays.asList(new Kill(BS8008), new Kill(BS8009), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039))))
                .withBloodCard(new BloodCard(4))
                .withEquipment(new Equipment())
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        assertThrows(DistanceErrorException.class,
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
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Kill(BS8009), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

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


        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));
        //playerA 對 playerB打殺
        game.playerPlayCard(playerA.getId(), BS8009.getCardId(), playerB.getId(), "active");


        //When Then
        assertThrows(IllegalStateException.class,
                () -> game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), "active"));
        assertEquals(4, game.getPlayer("player-b").getBloodCard().getHp());
        Assertions.assertTrue(Utils.compareArrayLists(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)), game.getPlayer("player-a").getHand().getCards()));
        assertTrue(game.getCurrentRound().isShowKill());
    }

    @DisplayName("""
            Given
            A 玩家對 B 玩家已出過殺
            A 玩家手牌有殺x1, 閃x2, 桃x2
            A 玩家對 B 玩家出殺
            B 玩家 4 滴血
                    
            When
            B 出 skip (不出牌)
                    
            Then
            B 玩家 3 滴血
            A 玩家手牌爲 閃x2, 桃x2
            """)
    @Test
    public void givenPlayerAKilledPlayerB_AndPlayerBSkip_ThenPlayerBDecreaseHp() {
        //Given
        Game game = new Game();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

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


        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));
        //playerA 對 playerB打殺
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), "active");


        //When
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");


        //Then
        assertEquals(4, game.getPlayer("player-a").getBloodCard().getHp());
        assertEquals(4, game.getPlayer("player-c").getBloodCard().getHp());
        assertEquals(4, game.getPlayer("player-d").getBloodCard().getHp());
        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
        Assertions.assertTrue(Utils.compareArrayLists(Arrays.asList(
                new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)), game.getPlayer("player-a").getHand().getCards()));
        assertTrue(game.getCurrentRound().isShowKill());
    }

    @DisplayName("""
            Given
            A 玩家手牌有殺x1, 閃x2, 桃x2
            A 玩家對 B 玩家出殺
            B 玩家 4 滴血
                    
            When
            B 出 閃
                    
            Then
            B 玩家 4 滴血
            A 玩家手牌爲 閃x2, 桃x2
            """)
    @Test
    public void givenPlayerAKilledPlayerB_AndPlayerBSDodge_ThenPlayerBSameHp() {
        //Given
        Game game = new Game();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withDefault()
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

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
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

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


        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));
        //playerA 對 playerB打殺
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), "active");


        //When
        game.playerPlayCard(playerB.getId(), "BH2028", playerA.getId(), "active");


        //Then
        assertEquals(4, game.getPlayer("player-a").getBloodCard().getHp());
        assertEquals(4, game.getPlayer("player-b").getBloodCard().getHp());
        assertEquals(4, game.getPlayer("player-c").getBloodCard().getHp());
        assertEquals(4, game.getPlayer("player-d").getBloodCard().getHp());
        Assertions.assertTrue(Utils.compareArrayLists(Arrays.asList(
                new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)), game.getPlayer("player-a").getHand().getCards()));
        Assertions.assertTrue(Utils.compareArrayLists(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BHK039)), game.getPlayer("player-b").getHand().getCards()));
        assertTrue(game.getCurrentRound().isShowKill());
    }


    @DisplayName("""
            Given
            輪到 A 玩家出牌

            When
            B 玩家對 A 玩家出殺
                    
            Then
            拋出錯誤
            """)
    @Test
    public void givenPlayerATurn_WhenPlayerBKillA_ThenException() {
        //Given
        Game game = new Game();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Kill(BS8009), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

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
                new Kill(BS8008), new Kill(BS8009), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

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


        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        //When playerB 對 playerA打殺
        //Then Exception
        assertThrows(IllegalStateException.class,
                () -> game.playerPlayCard(playerB.getId(), BS8008.getCardId(), playerA.getId(), "active"));

    }
}

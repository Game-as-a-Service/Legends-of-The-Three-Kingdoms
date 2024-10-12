package com.gaas.threeKingdoms;


import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.RoundPhase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.Utils.getEvent;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FinishActionTest {

    @DisplayName("""
            Given 
            A 玩家手牌有殺x1, 閃x2, 桃x2
            A 玩家 5 滴血
                        
            When
            A 玩家結束出牌
                        
            Then
            系統向 玩家發出DiscardCardsEvent, discardCount == 0
            系統向 玩家發出RoundEndEvent
            系統向 玩家發出RoundStartEvent
            系統向 玩家發出JudgementEvent
            系統向 玩家發出DrawCardEvent, size == 2
            輪到 B 的回合
            RoundPhase(Action)
            GamePhase(Normal)
            """)
    @Test
    public void playerA5HPAndHas5Cards_WhenPlayerAFinishAction_ThenDiscardCountIs0() {
        //Given
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(5))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();


        List<Player> players = Arrays.asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        //When
        List<DomainEvent> events = game.finishAction(playerA.getId());

        //Then
        FinishActionEvent finishActionEvent = getEvent(events, FinishActionEvent.class).orElseThrow(RuntimeException::new);
        NotifyDiscardEvent notifyDiscardEvent = getEvent(events, NotifyDiscardEvent.class).orElseThrow(RuntimeException::new);
        RoundEndEvent roundEndEvent = getEvent(events, RoundEndEvent.class).orElseThrow(RuntimeException::new);
        RoundStartEvent roundStartEvent = getEvent(events, RoundStartEvent.class).orElseThrow(RuntimeException::new);
        JudgementEvent judgementEvent = getEvent(events, JudgementEvent.class).orElseThrow(RuntimeException::new);
        DrawCardEvent drawCardEvent = getEvent(events, DrawCardEvent.class).orElseThrow(RuntimeException::new);


        assertEquals(0, notifyDiscardEvent.getDiscardCount());
        Round round = game.getCurrentRound();
        assertEquals(playerB, round.getCurrentRoundPlayer());
        assertEquals(RoundPhase.Action, round.getRoundPhase());
        assertEquals(2, drawCardEvent.getCardIds().size());
        assertEquals(2, drawCardEvent.getSize());
        assertEquals(6, game.getPlayer("player-b").getHand().getCards().size());
        assertEquals("Normal", game.getGamePhase().getPhaseName());
    }

    @DisplayName("""
            Given
            A 玩家手牌有殺x2, 閃x2, 桃x2
            A 玩家 5 滴血
                        
            When
            A 玩家結束出牌
                        
            Then
            系統向A 玩家發出DiscardCardsEvent, discardCount == 1
                        
            Phase為棄牌階段 (Discard)
            """)
    @Test
    public void playerA5HpAndHas6Cards_WhenPlayerAFinishAction_ThenDiscardCountIs1() {
        //Given
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(5))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8009), new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        game.setPlayers(asList(playerA, playerB, playerC, playerD));
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        //When
        List<DomainEvent> events = game.finishAction(playerA.getId());

        //Then
        NotifyDiscardEvent event = getEvent(events, NotifyDiscardEvent.class).orElseThrow(RuntimeException::new);
        assertEquals(1, event.getDiscardCount());
        assertEquals(RoundPhase.Discard, game.getCurrentRound().getRoundPhase());
    }


    @DisplayName("""
            Given
            B 已經死了
            當前回合玩家是 A ， Active Player 也是 A
                        
            When
            A 玩家結束出牌
                        
            Then
            當前回合玩家是 C ， Active Player 也是 C
            """)
    @Test
    public void playerBisDying_WhenPlayerAFinishAction_ThenCurrentActivePlayerIsC() {
        //Given
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(5))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8009), new Kill(BS8008), new Peach(BH3029), new Peach(BH4030), new Dodge(BH2028), new Dodge(BHK039)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(1))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        game.setPlayers(asList(playerA, playerB, playerC, playerD));
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        game.playerPlayCard(playerB.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        //When
        List<DomainEvent> events = game.finishAction(playerA.getId());

        //Then
        assertEquals("Normal", game.getGamePhase().getPhaseName());
        assertEquals("player-c", game.getCurrentRound().getCurrentRoundPlayer().getId());
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());
    }
}

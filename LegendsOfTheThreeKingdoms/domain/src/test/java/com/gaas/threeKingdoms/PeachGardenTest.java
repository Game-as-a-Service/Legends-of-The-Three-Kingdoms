package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.scrollcard.PeachGarden;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.Utils.getEvent;
import static com.gaas.threeKingdoms.handcard.PlayCard.SHA027;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PeachGardenTest {

    @DisplayName("""
            Given
            玩家ABCD
            A的回合
            A hp=1 max=1
            B hp=2 max=2
            c hp=1 max=2
            d hp=0 max=2
            A有有桃園結義
                        
            When
            A 出桃園結義
                        
            Then
            active player 還是 A
            A hp=1 max=1
            B hp=2 max=2
            c hp=2 max=2
            d hp=0 max=2
            """)
    @Test
    public void givenPlayerABCD_PlayerAHasPeachGarden_WhenPlayerAPlaysPeachGarden_ThenActivePlayerRemainsA_PlayerCHpIncrease() {
        // Given
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(1))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(new PeachGarden(SHA027)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(2))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.關羽))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(2))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();
        playerC.setHealthStatus(HealthStatus.ALIVE);
        playerC.damage(1);

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(2))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.孫權))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();
        playerD.damage(2);
        playerD.setHealthStatus(HealthStatus.DEATH);

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SHA027.getCardId(), null, PlayType.ACTIVE.getPlayType());

        // Then
        PeachGardenEvent peachGardenEvent = getEvent(events, PeachGardenEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-a", peachGardenEvent.getPlayerId());
        List<PeachEvent> peachEvents = peachGardenEvent.getPeachEvents();
        assertEquals(4, peachGardenEvent.getPeachEvents().size());
        PeachEvent peachEvent0 = peachEvents.get(0);
        PeachEvent peachEvent1 = peachEvents.get(1);
        PeachEvent peachEvent2 = peachEvents.get(2);
        PeachEvent peachEvent3 = peachEvents.get(3);

        assertEquals("player-a", peachEvent0.getPlayerId());
        assertEquals(1, peachEvent0.getFrom());
        assertEquals(1, peachEvent0.getTo());
        assertEquals("player-b", peachEvent1.getPlayerId());
        assertEquals(2, peachEvent1.getFrom());
        assertEquals(2, peachEvent1.getTo());
        assertEquals("player-c", peachEvent2.getPlayerId());
        assertEquals(1, peachEvent2.getFrom());
        assertEquals(2, peachEvent2.getTo());
        assertEquals("player-d", peachEvent3.getPlayerId());
        assertEquals(0, peachEvent3.getFrom());
        assertEquals(0, peachEvent3.getTo());

        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertEquals(1, game.getPlayer("player-a").getBloodCard().getHp());
        assertEquals(2, game.getPlayer("player-b").getBloodCard().getHp());
        assertEquals(2, game.getPlayer("player-c").getBloodCard().getHp());
        assertEquals(0, game.getPlayer("player-d").getBloodCard().getHp());
        assertEquals(HealthStatus.DEATH, game.getPlayer("player-d").getHealthStatus());
    }

}

package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.behavior.behavior.BorrowedSwordBehavior;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
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

public class BorrowedSwordTest {

    @DisplayName("""
            Given
            玩家ABCD
            A的回合
            A有借刀殺人
            B 有裝備武器，有一張殺，B攻擊範圍內
            有 C 可以殺
            When
            A 出借刀殺人，指定 B 殺 C
                        
            Then
            ABCD 玩家收到借刀殺人的 event
            B 玩家收到要求出殺的 event
            """)
    @Test
    public void givenPlayerAHasBorrowedSword_WhenPlayerAPlayBorrowedSword_ThenPlayerABCDAcceptBorrowedSwordEvent() {
        Game game = new Game();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new BorrowedSword(SCK065)));

        Equipment equipment = new Equipment();
        equipment.setWeapon(new QilinBowCard(EH5031));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipment)
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

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

        //When 先出借刀殺人卡牌
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        PlayCardEvent playCardEvent = events.stream()
                .filter(event -> event instanceof PlayCardEvent)
                .map(event -> (PlayCardEvent) event)
                .findFirst()
                .orElse(null);

        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof PlayCardEvent));
        assertEquals("player-a", playCardEvent.getPlayerId());
        assertEquals("player-b", playCardEvent.getTargetPlayerId());
        assertEquals("player-a", game.getCurrentRound().getCurrentRoundPlayer().getId());

        //When 指定借刀殺人操作的玩家
        List<DomainEvent> secondEvents = game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId());

        AskKillEvent askKillEvent = secondEvents.stream()
                .filter(event -> event instanceof AskKillEvent)
                .map(event -> (AskKillEvent) event)
                .findFirst()
                .orElse(null);

        assertTrue(secondEvents.stream().anyMatch(event -> event instanceof AskKillEvent));
        assertEquals("player-b", askKillEvent.getPlayerId());

    }


    @DisplayName("""
            Given
            玩家ABCD
            A的回合
            A有借刀殺人
            B 有裝備武器，有一張殺，B攻擊範圍內
            沒有人可以殺
            When
            A 出借刀殺人，指定 B
                        
            Then
            拋出錯誤
            """)
    @Test
    public void givenPlayerAHasBorrowedSword_WhenPlayerAPlayBorrowedSwordButPlayerBHaveNoPlayer_ThenThrowError() {
        Game game = new Game();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new BorrowedSword(SCK065)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

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

        Equipment equipmentA = new Equipment();
        Equipment equipmentC = new Equipment();
        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        equipmentA.setPlusOne(new ShadowHorse(ES5018));
        equipmentC.setPlusOne(new ShadowHorse(ES5018));
        playerA.setEquipment(equipmentA);
        playerB.setEquipment(equipmentB);
        playerC.setEquipment(equipmentC);


        //When A 出借刀殺人
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        //When A 指定 B
        //Then 拋出錯誤
        Assertions.assertThrows(IllegalStateException.class, () -> game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId()));

    }

    @DisplayName("""
            Given
            玩家ABCD
            A的回合
            A有借刀殺人
            B 沒有裝備武器，有一張殺，B攻擊範圍內
            有 C 可以殺
            When
            A 出借刀殺人，指定 B
                        
            Then
            拋出錯誤
            """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasBorrowedSword_WhenPlayerAPlaysBorrowedSwordAndAssignsBWithoutWeaponToKillC_ThenThrowError() {
        Game game = new Game();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new BorrowedSword(SCK065)));

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

        //When A 出借刀殺人
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        //When A 指定 B
        //Then 拋出錯誤
        Assertions.assertThrows(IllegalStateException.class, () -> game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId()));
    }


    @DisplayName("""
            Given
            玩家ABCD
            A的回合
            A有借刀殺人
            B 有裝備武器，有一張殺，B攻擊範圍內
            有 C 可以殺
            A 出借刀殺人,指定 B 殺 C
            C沒有閃
            When
            B 玩家出殺
                        
            Then
            ABCD 玩家收到B玩家出殺的 event
            C扣血之後,stack裡面要沒有借刀殺人的behavior
            active plater 是 A
            """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasBorrowedSword_WhenPlayerAPlaysBorrowedSwordAndAssignsBToKillC_AndPlayerBPlaysKill_ThenPlayersABCDReceivePlayerBPlaysKillEvent() {
        Game game = new Game();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new BorrowedSword(SCK065)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

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

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        playerB.setEquipment(equipmentB);

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId());


        //When B 出殺
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), BS8008.getCardId(), playerC.getId(), PlayType.ACTIVE.getPlayType());

        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof PlayCardEvent));
        assertEquals(3, game.getPlayer("player-c").getBloodCard().getHp());
        assertFalse(game.getTopBehavior().stream().anyMatch(behavior -> behavior instanceof BorrowedSwordBehavior));
        assertEquals(game.getCurrentRound().getCurrentRoundPlayer().getId(), "player-a");
        assertEquals(game.getActivePlayer().getId(), "player-a");
    }

}

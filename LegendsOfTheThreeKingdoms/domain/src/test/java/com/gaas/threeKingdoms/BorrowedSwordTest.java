package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.behavior.behavior.BorrowedSwordBehavior;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
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
                        
            When
            B 玩家出殺
            C出skip
                        
            Then
            ABCD 玩家收到B玩家出殺的 event
            C扣血之後,stack裡面要沒有借刀殺人的behavior
            active player 是 A
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
        //When C 出skip
        List<DomainEvent> skipEvents = game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof PlayCardEvent));
        assertEquals(3, game.getPlayer("player-c").getBloodCard().getHp());

        assertFalse(game.getTopBehavior().stream().anyMatch(behavior -> behavior instanceof BorrowedSwordBehavior));
        assertEquals(game.getCurrentRound().getCurrentRoundPlayer().getId(), "player-a");
        assertEquals(game.getActivePlayer().getId(), "player-a");
    }


    @DisplayName("""
            Given
            玩家ABCD
            A的回合
            A有借刀殺人
            B 有裝備武器，有一張殺，B攻擊範圍內
            有 C 可以殺
            A 出借刀殺人,指定 B 殺 C
                        
            When
            B 玩家出skip
                        
            Then
            ABCD 玩家收到 B 玩家的武器卡給 A的 event
            B的武器卡是空的
            A的手牌有B的武器卡
            stack裡面要沒有借刀殺人的behavior
            active player 是 A
            """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasBorrowedSword_WhenPlayerAPlaysBorrowedSwordAndAssignsBToKillC_AndPlayerBSkips_ThenPlayersABCDReceivePlayerBWeaponCardGivenToPlayerAEvent() {
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


        //When B 出 skip
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), "", playerC.getId(), PlayType.SKIP.getPlayType());

        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof PlayCardEvent));
        assertTrue(events.stream().anyMatch(event -> event instanceof WeaponUsurpationEvent));
        assertEquals(4, game.getPlayer("player-c").getBloodCard().getHp());
        assertNull(playerB.getEquipmentWeaponCard());
        assertTrue(playerA.getHand().getCards().stream().anyMatch(card -> card.getId().equals("ECA066")));
        assertFalse(game.getTopBehavior().stream().anyMatch(behavior -> behavior instanceof BorrowedSwordBehavior));
        assertEquals(game.getCurrentRound().getCurrentRoundPlayer().getId(), "player-a");
        assertEquals(game.getActivePlayer().getId(), "player-a");
    }

    @DisplayName("""
        Given
        玩家ABCD
        A的回合
        A有借刀殺人
        B 有裝備武器，沒有殺，B攻擊範圍內
        有 C 可以殺
        
        When
        A 出借刀殺人,指定 B 殺 C
        
        Then
        ABCD 玩家收到 B 玩家的武器卡給 A的 event
        B的武器卡是空的
        A的手牌有B的武器卡
        stack裡面要沒有借刀殺人的behavior
        active player 是 A
            """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasBorrowedSword_WhenPlayerAPlaysBorrowedSwordAndAssignsBToKillC_AndPlayerHaveNoKill_ThenPlayersABCDReceivePlayerBWeaponCardGivenToPlayerAEvent() {
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

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new RepeatingCrossbowCard(ECA066));
        playerB.setEquipment(equipmentB);

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));


        //When A出借刀殺人, 指定B殺C
        List<DomainEvent> firstEvents = game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> secondEvents = game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId());

        //Then
        assertTrue(secondEvents.stream().anyMatch(event -> event instanceof PlayCardEvent));
        assertTrue(secondEvents.stream().anyMatch(event -> event instanceof WeaponUsurpationEvent));
        assertEquals(4, game.getPlayer("player-c").getBloodCard().getHp());
        assertNull(playerB.getEquipmentWeaponCard());
        assertTrue(playerA.getHand().getCards().stream().anyMatch(card -> card.getId().equals("ECA066")));
        assertFalse(game.getTopBehavior().stream().anyMatch(behavior -> behavior instanceof BorrowedSwordBehavior));
        assertEquals(game.getCurrentRound().getCurrentRoundPlayer().getId(), "player-a");
        assertEquals(game.getActivePlayer().getId(), "player-a");
    }

    @DisplayName("""
            Given
            玩家ABCD
            A的回合
            A有借刀殺人
            B 有裝備武器，有一張殺，B攻擊範圍內
            有 C 可以殺， C 有裝備八卦陣
            A 出借刀殺人,指定 B 殺 C
            B 玩家出殺
                        
            When
            C收到要不要發動裝備卡的event
            C發動八卦陣裝備卡 
                        
            Then
            ABCD 玩家收到抽到赤兔馬 (♥5) 的 Event 
            Event 內是效果成功， C 不用出閃
            C 玩家HP=4
            還是 A 的回合
            C扣血之後,stack裡面要沒有借刀殺人的behavior
            active player 是 A
            """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasBorrowedSword_WhenPlayerAPlaysBorrowedSwordAndAssignsBToKillWithCHaveEightDiagramTactic_AndPlayerBPlaysKill_ThenPlayersABCDReceivePlayerCPlaysEightDiagramTacticWithSuccessEvent() {
        Game game = new Game();
        Deck deck = new Deck(
                List.of(
                        new RedRabbitHorse(BH3029)
                )
        );
        game.setDeck(deck);
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


        Equipment equipment = new Equipment();
        equipment.setArmor(new EightDiagramTactic(ES2015));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipment)
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

        //B 對 C 出殺
        List<DomainEvent> killEvents = game.playerPlayCard(playerB.getId(), BS8008.getCardId(), playerC.getId(), PlayType.ACTIVE.getPlayType());

        // When C 發動八卦陣裝備卡，效果成功
        List<DomainEvent> equipmentEvents = game.playerUseEquipment(playerC.getId(), ES2015.getCardId(), playerB.getId(), EquipmentPlayType.ACTIVE);

        //ThenNormalACTI
        assertTrue(equipmentEvents.stream()
                .filter(event -> event instanceof EffectEvent)
                .map(EffectEvent.class::cast)
                .allMatch(EffectEvent::isSuccess));
        assertEquals(4, game.getPlayer("player-c").getBloodCard().getHp());
        assertFalse(game.getTopBehavior().stream().anyMatch(behavior -> behavior instanceof BorrowedSwordBehavior));
        assertFalse(game.getTopBehavior().stream().anyMatch(behavior -> behavior instanceof NormalActiveKillBehavior));

        assertEquals(game.getCurrentRound().getCurrentRoundPlayer().getId(), "player-a");
        assertEquals(game.getActivePlayer().getId(), "player-a");
    }


    @DisplayName("""
            Given
            玩家 ABCD
            A 的回合
            A有借刀殺人
            B 有裝備武器，有一張殺，B 攻擊範圍內
            有 C 可以殺， C 有裝備八卦陣
            A 出借刀殺人,指定 B 殺 C
            B 玩家出殺
            
            When
            C發動八卦陣裝備卡抽到大老二，效果失敗
            C 出 skip
                        
            Then
            C 玩家HP = 3
            還是 A 的回合
            C 扣血之後, stack 裡面要沒有借刀殺人的 behavior
            active player 是 A
            """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasBorrowedSword_WhenPlayerAPlaysBorrowedSwordAndAssignsBToKillC_AndPlayerBPlaysKill_AndCActivatesBaguaFailsAndPlaysSkip_ThenCPlayerHPIs3_StillPlayerATurn_StackHasNoBorrowedSwordBehavior_ActivePlayerIsA() {
        Game game = new Game();
        Deck deck = new Deck(
                List.of(
                        new RedRabbitHorse(ES2002)
                )
        );
        game.setDeck(deck);
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

        Equipment equipment = new Equipment();
        equipment.setArmor(new EightDiagramTactic(ES2015));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipment)
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
        //When C 出skip
        List<DomainEvent> equipmentEvents = game.playerUseEquipment(playerC.getId(), ES2015.getCardId(), playerB.getId(), EquipmentPlayType.ACTIVE);
        List<DomainEvent> skipEvents = game.playerPlayCard(playerC.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        //Then
        assertTrue(skipEvents.stream().anyMatch(event -> event instanceof PlayCardEvent));
        assertEquals(3, game.getPlayer("player-c").getBloodCard().getHp());
        assertFalse(game.getTopBehavior().stream().anyMatch(behavior -> behavior instanceof BorrowedSwordBehavior));
        assertFalse(game.getTopBehavior().stream().anyMatch(behavior -> behavior instanceof NormalActiveKillBehavior));
        assertEquals(game.getCurrentRound().getCurrentRoundPlayer().getId(), "player-a");
        assertEquals(game.getActivePlayer().getId(), "player-a");
    }
}

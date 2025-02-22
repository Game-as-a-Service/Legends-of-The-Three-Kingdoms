package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.exception.DistanceErrorException;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.EquipmentPlayType;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.ShadowHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class QilinBowTest {

    @DisplayName("""
            Given
            A的回合
            A武器欄沒有裝備卡
            A的手牌也有一張麒麟弓
                    
            When
            A 出麒麟弓
                    
            Then
            A的裝備卡武器欄位有麒麟弓
                """)
    @Test
    public void givenPlayerAHasQilinBow_WhenPlayerAPlayQilinBow_ThenPlayerAEquipQilinBow() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)));

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
        game.playerPlayCard(playerA.getId(), EH5031.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        //Then
        assertEquals(new QilinBowCard(EH5031), game.getPlayer("player-a").getEquipmentWeaponCard());
    }

    @DisplayName("""
            Given
            A的回合
            A裝備諸葛連奴
                    
            When
            A出麒麟弓
                    
            Then
            A玩家裝備卡有麒麟弓
                    """)
    @Test
    public void givenPlayerAEquippedRepeatingCrossbow_WhenPlayerAPlayQilinBow_ThenPlayerAHaveQilinBow() {
        Game game = new Game();
        game.initDeck();
        Equipment equipment = new Equipment();
        equipment.setWeapon(new RepeatingCrossbowCard(ECA066));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)));

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
        game.playerPlayCard(playerA.getId(), EH5031.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        //Then
        assertEquals(EH5031.getCardId(), game.getPlayer("player-a").getEquipmentWeaponCard().getId());
    }

    @DisplayName("""
            Given
            A的回合
            A已裝備麒麟弓
            D玩家HP 3
            D玩家距離A玩家3
                    
            When
            A攻擊D
            D不出閃
                    
            Then
            D HP 2
                    """)
    @Test
    public void givenPlayerATurn_PlayerAEquippedWithQilinBow_PlayerDHP3_PlayerDDistanceFromPlayerA3_WhenPlayerAAttacksPlayerD_PlayerDDoesNotPlayDodge_ThenPlayerDHP2() {
        Game game = new Game();
        game.initDeck();
        Equipment equipment = new Equipment();
        equipment.setWeapon(new QilinBowCard(EH5031));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)));

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
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerE = PlayerBuilder.construct()
                .withId("player-e")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerF = PlayerBuilder.construct()
                .withId("player-f")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerG = PlayerBuilder.construct()
                .withId("player-g")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD, playerE, playerF, playerG);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerD.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerA.getId(), "skip");

        //Then
        assertEquals(2, game.getPlayer("player-d").getBloodCard().getHp());
    }

    @DisplayName("""
        Given
        A的回合
        D玩家HP 3
        D玩家距離A玩家3
        
        When
        A攻擊D
        
        Then
        Exception
                    """)
    @Test
    public void givenPlayerATurn_PlayerDHP3_PlayerDDistanceFromPlayerA3_WhenPlayerAAttacksPlayerD_ThenException() {
        Game game = new Game();
        game.initDeck();
        Equipment equipment = new Equipment();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)));

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
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerE = PlayerBuilder.construct()
                .withId("player-e")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerF = PlayerBuilder.construct()
                .withId("player-f")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerG = PlayerBuilder.construct()
                .withId("player-g")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD, playerE, playerF, playerG);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        //Then
        assertThrows(DistanceErrorException.class, () -> game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerD.getId(), PlayType.ACTIVE.getPlayType()));

    }

    @DisplayName("""
        Given
        A的回合
        A有麒麟弓
        B Hp = 3
        B 有八卦陣
        
        When
        A攻擊B
        B發動八卦陣效果成功
        
        Then
        B Hp = 3
                    """)
    @Test
    public void givenPlayerATurn_PlayerAHasQilinBow_PlayerBHpIs3_PlayerBHasEightDiagramTactic_WhenPlayerAAttacksPlayerB_PlayerBActivatesEightDiagramTacticSuccessfully_ThenPlayerBHpIs3() {
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new RedRabbitHorse(BH3029)
                )
        );
        game.setDeck(deck);
        Equipment equipmentQilinBow = new Equipment();
        equipmentQilinBow.setWeapon(new QilinBowCard(EH5031));
        Equipment equipmentEightDiagramTactic = new Equipment();
        equipmentEightDiagramTactic.setArmor(new EightDiagramTactic(EC2067));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipmentQilinBow)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentEightDiagramTactic)
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
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.playerUseEquipment(playerB.getId(), EC2067.getCardId(), playerB.getId(), EquipmentPlayType.ACTIVE);

        //Then
        assertTrue(events.stream()
                .filter(event -> event instanceof EffectEvent)
                .map(EffectEvent.class::cast)
                .allMatch(EffectEvent::isSuccess));
        assertEquals(3, game.getPlayer("player-b").getHP());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("""
        Given
        A的回合
        A有麒麟弓
        B Hp = 3
        B 有一張閃
        
        When
        A攻擊B
        B出閃
        
        Then
        B Hp = 3
                    """)
    @Test
    public void givenPlayerATurn_PlayerAHasQilinBow_PlayerBHpIs3_PlayerBHasDodge_WhenPlayerAAttacksPlayerB_PlayerBPlaysDodge_ThenPlayerBHpIs3() {
        Game game = new Game();
        game.initDeck();
        Equipment equipment = new Equipment();
        equipment.setWeapon(new QilinBowCard(EH5031));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)));

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
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        //Then
        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("""
        Given
        A的回合
        A已裝備麒麟弓
        D玩家HP 3
        D玩家距離A玩家3
        D玩家有裝備赤兔馬
        A攻擊D
        
        When
        D 沒有出閃
        
        Then
        系統詢問是否發動裝備卡效果
    """)
    @Test
    public void givenPlayerAEquippedQilinBow_PlayerDHP3_PlayerDDistance3_PlayerDEquippedRedRabbitHorse_WhenPlayerAAttacksPlayerD_PlayerDNotPlayDodge_ThenSystemAskToTriggerEquipmentEffect() {
        Game game = new Game();
        game.initDeck();
        Equipment equipmentA= new Equipment();
        equipmentA.setWeapon(new QilinBowCard(EH5031));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipmentA)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)));

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

        Equipment equipmentD= new Equipment();
        equipmentD.setMinusOne(new RedRabbitHorse(EH5044));
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipmentD)
                .build();

        Player playerE = PlayerBuilder.construct()
                .withId("player-e")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerF = PlayerBuilder.construct()
                .withId("player-f")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerG = PlayerBuilder.construct()
                .withId("player-g")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD, playerE, playerF, playerG);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerD.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerD.getId(), "", playerA.getId(), "skip");

        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent));
    }

    @DisplayName("""
       Given
       A的回合
       A已裝備麒麟弓
       D玩家HP 3
       D玩家距離A玩家3
       D玩家沒有坐騎
       A攻擊D
       
       When
       D 沒有出閃
       
       Then
       系統不會詢問是否發動裝備卡效果
       D HP 2
    """)
    @Test
    public void givenPlayerAEquippedQilinBow_PlayerDHP3_PlayerDDistance3_PlayerDNoMount_WhenPlayerAAttacksPlayerD_PlayerDNotPlayDodge_ThenSystemNotAskToTriggerEquipmentEffect_PlayerDHP2() {
        Game game = new Game();
        game.initDeck();
        Equipment equipment = new Equipment();
        equipment.setWeapon(new QilinBowCard(EH5031));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)));

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
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerE = PlayerBuilder.construct()
                .withId("player-e")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerF = PlayerBuilder.construct()
                .withId("player-f")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerG = PlayerBuilder.construct()
                .withId("player-g")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD, playerE, playerF, playerG);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerD.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerD.getId(), "", playerA.getId(), "skip");

        //Then
        assertFalse(events.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent));
        assertEquals(2, game.getPlayer("player-d").getBloodCard().getHp());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("""
        Given
        A的回合
        A已裝備麒麟弓
        D玩家HP 3
        D玩家距離A玩家3
        D玩家有赤兔馬
        A攻擊D
        D 沒有出閃
        系統詢問是否發動裝備卡效果
        
        When
        A決定不發動效果
        
        Then
        殺攻擊到D
        D HP 2
        D 還有赤兔馬
    """)
    @Test
    public void givenPlayerAEquippedQilinBow_PlayerDHP3_PlayerDDistance3_PlayerDEquippedRedRabbitHorse_PlayerDNotPlayDodge_SystemAskToTriggerEquipmentEffect_WhenPlayerADecidesNotToTriggerEffect_ThenAttackHitsPlayerD_PlayerDHP2() {
        Game game = new Game();
        game.initDeck();
        Equipment equipment = new Equipment();
        equipment.setWeapon(new QilinBowCard(EH5031));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)));

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

        Equipment equipmentD= new Equipment();
        equipmentD.setMinusOne(new RedRabbitHorse(EH5044));
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipmentD)
                .build();

        Player playerE = PlayerBuilder.construct()
                .withId("player-e")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerF = PlayerBuilder.construct()
                .withId("player-f")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerG = PlayerBuilder.construct()
                .withId("player-g")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD, playerE, playerF, playerG);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerD.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerA.getId(), "skip");
        List<DomainEvent> events = game.playerUseEquipment(playerA.getId(), EH5031.getCardId(), playerD.getId(), EquipmentPlayType.SKIP);

        //Then
        assertEquals(2, game.getPlayer("player-d").getBloodCard().getHp());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertNotNull(playerD.getEquipmentMinusOneMountsCard());
    }

    @DisplayName("""
        Given
        A的回合
        A已裝備麒麟弓
        D玩家HP 3
        D玩家距離A玩家3
        D玩家有赤兔馬
        A攻擊D
        D 沒有出閃
        
        When
        A決定發動效果
        
        Then
        D 的赤兔馬已棄置
        殺成功， D HP 2
    """)
    @Test
    public void givenPlayerAEquippedQilinBow_PlayerDHP3_PlayerDDistance3_PlayerDEquippedRedRabbitHorse_PlayerDNotPlayDodge_WhenPlayerADecidesToTriggerEffect_ThenPlayerDRedRabbitHorseDiscarded_AttackHitsPlayerD_PlayerDHP2() {
        Game game = new Game();
        game.initDeck();
        Equipment equipment = new Equipment();
        equipment.setWeapon(new QilinBowCard(EH5031));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)));

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

        Equipment equipmentD = new Equipment();
        equipmentD.setMinusOne(new RedRabbitHorse(EH5044));
        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipmentD)
                .build();

        Player playerE = PlayerBuilder.construct()
                .withId("player-e")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerF = PlayerBuilder.construct()
                .withId("player-f")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerG = PlayerBuilder.construct()
                .withId("player-g")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD, playerE, playerF, playerG);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerD.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerA.getId(), "skip");
        List<DomainEvent> events = game.playerUseEquipment(playerA.getId(), EH5031.getCardId(), playerD.getId(), EquipmentPlayType.ACTIVE);

        //Then
        assertEquals(2, game.getPlayer("player-d").getBloodCard().getHp());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertTrue(events.stream().anyMatch(event -> event instanceof QilinBowCardEffectEvent));
        assertNull(playerD.getEquipment().getMinusOne());
    }

    @DisplayName("""
        Given
        A的回合
        A已裝備麒麟弓
        D玩家HP 3
        D玩家距離A玩家3
        D玩家有赤兔馬 與 絕影
        A攻擊D
        D 沒有出閃
        系統詢問是否發動裝備卡效果
        
        When
        A決定發動效果
        
        Then
        收到已推給被攻擊者的馬 的event
    """)
    @Test
    public void givenPlayerAEquippedQilinBow_PlayerDHP3_PlayerDDistance3_PlayerDEquippedRedRabbitHorseAndShadowRunner_PlayerDNotPlayDodge_SystemAskToTriggerEquipmentEffect_WhenPlayerADecidesToTriggerEffect_ThenEventTriggeredForDiscardingAttackedPlayerMounts() {
        Game game = new Game();
        game.initDeck();
        Equipment equipment = new Equipment();
        equipment.setWeapon(new QilinBowCard(EH5031));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)));

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

        Equipment equipmentD = new Equipment();
        equipmentD.setMinusOne(new RedRabbitHorse(EH5044));
        equipmentD.setPlusOne(new ShadowHorse(ES5018));

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipmentD)
                .build();

        Player playerE = PlayerBuilder.construct()
                .withId("player-e")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerF = PlayerBuilder.construct()
                .withId("player-f")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerG = PlayerBuilder.construct()
                .withId("player-g")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD, playerE, playerF, playerG);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        //When
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerD.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerA.getId(), "skip");
        List<DomainEvent> events = game.playerUseEquipment(playerA.getId(), EH5031.getCardId(), playerD.getId(), EquipmentPlayType.ACTIVE);

        //Then
        assertEquals(4, game.getPlayer("player-d").getBloodCard().getHp());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertTrue(events.stream().anyMatch(event -> event instanceof AskChooseMountCardEvent));
    }

    @DisplayName("""
        Given
        A的回合
        A已裝備麒麟弓
        D玩家HP 3
        D玩家距離A玩家3
        D玩家有赤兔馬 與 決影
        A攻擊D
        D 沒有出閃
        系統詢問是否發動裝備卡效果
        A決定發動效果
        A玩家收到D玩家有赤兔馬與決影的event
        
        When
        A選擇指定的馬 赤兔馬
        
        Then
        D玩家 HP 2
        D玩家有 決影 沒有赤兔馬
    """)
    @Test
    public void givenPlayerAEquippedQilinBow_PlayerDHP3_PlayerDDistance3_PlayerDEquippedRedRabbitHorseAndShadowRunner_PlayerDNotPlayDodge_SystemAskToTriggerEquipmentEffect_PlayerAReceivesEventPlayerDEquippedRedRabbitHorseAndShadowRunner_WhenPlayerASelectsRedRabbitHorse_ThenPlayerDHP2_PlayerDHasShadowRunnerNoRedRabbitHorse() {
        Game game = new Game();
        game.initDeck();
        Equipment equipment = new Equipment();
        equipment.setWeapon(new QilinBowCard(EH5031));

        Equipment equipmentD = new Equipment();
        equipmentD.setMinusOne(new RedRabbitHorse(EH5044));
        equipmentD.setPlusOne(new ShadowHorse(ES5018));

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)));

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
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipmentD)
                .build();

        Player playerE = PlayerBuilder.construct()
                .withId("player-e")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerF = PlayerBuilder.construct()
                .withId("player-f")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerG = PlayerBuilder.construct()
                .withId("player-g")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD, playerE, playerF, playerG);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerD.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerA.getId(), "skip");
        List<DomainEvent> events = game.playerUseEquipment(playerA.getId(), EH5031.getCardId(), playerD.getId(), EquipmentPlayType.ACTIVE);
        //when
        List<DomainEvent> chooseHorseEvents = game.playerChooseHorseForQilinBow("player-a", "EH5044");
        //Then
        assertEquals(2, game.getPlayer("player-d").getBloodCard().getHp());
        assertEquals(Stage.Normal, game.getCurrentRound().getStage());
        assertNull(game.getPlayer("player-d").getEquipment().getMinusOne());
        assertNotNull(game.getPlayer("player-d").getEquipment().getPlusOne());
    }


    @DisplayName("""
        Given
        A的回合
        A已裝備麒麟弓
        D玩家HP 1
        D玩家距離A玩家3
        D玩家有赤兔馬
        A攻擊D
        D 沒有出閃
        系統詢問是否發動裝備卡效果
        
        When
        A決定發動效果
        A玩家收到D玩家有赤兔馬與決影的event
        
        Then
        D玩家 HP 0
        D玩家 沒有赤兔馬
        要求其他玩家出桃的 event
    """)
    @Test
    public void givenPlayerAEquippedQilinBow_PlayerDHP1_PlayerDDistance3_PlayerDEquippedRedRabbitHorseAndShadowRunner_PlayerDNotPlayDodge_SystemAskToTriggerEquipmentEffect_PlayerAReceivesEventPlayerDEquippedRedRabbitHorseAndShadowRunner_WhenPlayerASelectsRedRabbitHorse_ThenPlayerDHP0_PlayerDHasShadowRunnerNoRedRabbitHorse() {
        Game game = new Game();
        game.initDeck();
        Equipment equipment = new Equipment();
        equipment.setWeapon(new QilinBowCard(EH5031));

        Equipment equipmentD = new Equipment();
        equipmentD.setMinusOne(new RedRabbitHorse(EH5044));

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)));

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
                .withBloodCard(new BloodCard(1))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipmentD)
                .build();

        Player playerE = PlayerBuilder.construct()
                .withId("player-e")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerF = PlayerBuilder.construct()
                .withId("player-f")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerG = PlayerBuilder.construct()
                .withId("player-g")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD, playerE, playerF, playerG);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerD.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerA.getId(), "skip");
        //when
        List<DomainEvent> events = game.playerUseEquipment(playerA.getId(), EH5031.getCardId(), playerD.getId(), EquipmentPlayType.ACTIVE);
        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof AskPeachEvent));
        assertTrue(events.stream().anyMatch(event -> event instanceof QilinBowCardEffectEvent));
        assertEquals(0, game.getPlayer("player-d").getBloodCard().getHp());
        assertNull(game.getPlayer("player-d").getEquipment().getMinusOne());
    }


    @DisplayName("""
        Given
        A 的回合
        A 已裝備麒麟弓
        A 有殺
        D 玩家 HP 1
        D 玩家距離 A 玩家3
        D 玩家有赤兔馬與絕影
        A 攻擊 D
        A 玩家出閃
        A 選擇發動麒麟弓效果
        拆掉絕影
        
        When
        D 玩家選擇不出桃
        
        Then
        D 玩家 HP 0
        詢問 E 玩家是否出桃
    """)
    @Test
    public void testBShouldBeAskPlayPeach() {
        Game game = new Game();
        game.initDeck();
        Equipment equipment = new Equipment();
        equipment.setWeapon(new QilinBowCard(EH5031));

        Equipment equipmentD = new Equipment();
        equipmentD.setMinusOne(new RedRabbitHorse(EH5044));
        equipmentD.setPlusOne(new ShadowHorse(ES5018));

        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new QilinBowCard(EH5031)));

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
                .withBloodCard(new BloodCard(1))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipmentD)
                .build();

        Player playerE = PlayerBuilder.construct()
                .withId("player-e")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerF = PlayerBuilder.construct()
                .withId("player-f")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        Player playerG = PlayerBuilder.construct()
                .withId("player-g")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD, playerE, playerF, playerG);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerD.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerD.getId(), "", playerA.getId(), "skip");
        game.playerUseEquipment(playerA.getId(), EH5031.getCardId(), playerD.getId(), EquipmentPlayType.ACTIVE);
        game.playerChooseHorseForQilinBow(playerA.getId(), "ES5018");

        //when
        List<DomainEvent> events = game.playerPlayCard(playerD.getId(), "", playerD.getId(), "skip");

        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof AskPeachEvent));
        assertFalse(events.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent));
        AskPeachEvent askPeachEvent = (AskPeachEvent) events.stream().filter(event -> event instanceof AskPeachEvent).findFirst().get();
        assertEquals("player-e", askPeachEvent.getPlayerId());

        //when
        List<DomainEvent> eventsPlayerE = game.playerPlayCard(playerE.getId(), "", playerD.getId(), "skip");
        assertTrue(eventsPlayerE.stream().anyMatch(event -> event instanceof AskPeachEvent));
        assertFalse(eventsPlayerE.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent));
        askPeachEvent = (AskPeachEvent) eventsPlayerE.stream().filter(event -> event instanceof AskPeachEvent).findFirst().get();
        assertEquals("player-f", askPeachEvent.getPlayerId());

        game.playerPlayCard(playerF.getId(), "", playerD.getId(), "skip");
        game.playerPlayCard(playerG.getId(), "", playerD.getId(), "skip");
        game.playerPlayCard(playerA.getId(), "", playerD.getId(), "skip");
        game.playerPlayCard(playerB.getId(), "", playerD.getId(), "skip");
        List<DomainEvent> eventsEnd = game.playerPlayCard(playerC.getId(), "", playerD.getId(), "skip");
        assertFalse(eventsEnd.stream().anyMatch(event -> event instanceof AskPeachEvent));
        assertFalse(eventsEnd.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent));
    }

    @DisplayName("""
        Given
        A的回合
        A有麒麟弓
        B Hp = 3
        B 有八卦陣
        B 沒有閃
        B 沒有馬
        
        When
        A攻擊B
        B發動八卦陣效果失敗
        B Skip 出閃
        
        Then
        有 player damage event
        B hp = 2
        Active player 為 A
    """)
    @Test
    public void givenTurnA_AHasQilinBow_BHasEightDiagramTacticAndNoDodge_WhenAAttacksB_BFailsEightDiagramEffectAndSkipsDodge_ThenAReceivesQilinBowTriggerEvent() {
        // Given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Kill(BC2054)
                )
        );
        game.setDeck(deck);

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        playerA.getEquipment().setWeapon(new QilinBowCard(EH5031));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8009)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(3))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        playerB.getEquipment().setArmor(new EightDiagramTactic(EC2067));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        game.playerPlayCard(playerA.getId(), BS8009.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerUseEquipment(playerB.getId(), EC2067.getCardId(), playerB.getId(), EquipmentPlayType.ACTIVE);
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // then
        assertFalse(events.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent));
        PlayerDamagedEvent playerDamagedEvent = (PlayerDamagedEvent) events.stream().filter(event -> event instanceof PlayerDamagedEvent).findFirst().get();
        assertEquals("player-b", playerDamagedEvent.getPlayerId());
        assertEquals(2 , game.getPlayer("player-b").getBloodCard().getHp());
        assertEquals("player-a", game.getActivePlayer().getId());
    }

    @DisplayName("""
        Given
        A的回合
        A有麒麟弓
        B Hp = 3
        B 有八卦陣
        B 沒有閃
        B 沒有馬
        
        When
        A攻擊B
        B 不發動八卦陣效果
        B Skip 出閃
        
        Then
        有 player damage event
        B hp = 2
        Active player 為 A
    """)
    @Test
    public void givenTurnA_AHasQilinBow_BHasEightDiagramTacticAndNoDodge_WhenAAttacksB_BDoesNotActivateEightDiagramAndSkipsDodge_ThenAReceivesQilinBowTriggerEvent() {
        // Given
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        playerA.getEquipment().setWeapon(new QilinBowCard(EH5031));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8009)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(3))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        playerB.getEquipment().setArmor(new EightDiagramTactic(EC2067));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        game.playerPlayCard(playerA.getId(), BS8009.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerUseEquipment(playerB.getId(), EC2067.getCardId(), playerA.getId(), EquipmentPlayType.SKIP);
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // Then
        PlayerDamagedEvent playerDamagedEvent = (PlayerDamagedEvent) events.stream().filter(event -> event instanceof PlayerDamagedEvent).findFirst().get();
        assertEquals("player-b", playerDamagedEvent.getPlayerId());
        assertEquals(2 , game.getPlayer("player-b").getBloodCard().getHp());
        assertEquals("player-a", game.getActivePlayer().getId());
    }

    @DisplayName("""
        Given
        A的回合
        A有麒麟弓
        B Hp = 3
        B 有八卦陣
        B 有赤兔馬與絕影馬
        
        When
        A攻擊B
        A發動麒麟弓
        
        Then
        噴出 Exception
    """)
    @Test
    public void givenTurnA_AHasQilinBow_BHasEightDiagramTactic_WhenAAttacksBAndActivatesQilinBow_ThenThrowException() {
        // Given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new RedRabbitHorse(BH3029)
                )
        );
        game.setDeck(deck);

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        playerA.getEquipment().setWeapon(new QilinBowCard(EH5031));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8009)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(3))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        playerB.getEquipment().setArmor(new EightDiagramTactic(EC2067));
        playerB.getEquipment().setMinusOne(new RedRabbitHorse(EH5044));
        playerB.getEquipment().setPlusOne(new ShadowHorse(ES5018));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.張飛))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withHealthStatus(HealthStatus.ALIVE)
                .build();

        List<Player> players = Arrays.asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));
        game.playerPlayCard(playerA.getId(), BS8009.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // When then
        assertThrows(IllegalStateException.class, () -> game.playerUseEquipment(playerA.getId(), EH5031.getCardId(), playerD.getId(), EquipmentPlayType.ACTIVE));
    }

    @DisplayName("""
    Given
    A的回合
    A有麒麟弓
    B Hp = 3
    B 有八卦陣
    B 沒有閃
    B 有 +1馬
    When
    A攻擊B
    B發動八卦陣效果失敗
    B Skip 出閃
    A 收到是否要發動麒麟弓的事件
    A 發動麒麟弓效果
    Then
    B 沒有馬
    B HP = 2
""")
    @Test
    public void givenPlayerAHasQilinBow_PlayerBHasEightDiagramAndPlusOneHorse_WhenPlayerAAttacksBAndBTriggersEightDiagramFailsAndAUsesQilinBow_ThenBHasNoHorseAndHpIs2() throws Exception {
        // Given
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Kill(BC2054)
                )
        );
        game.setDeck(deck);
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        playerA.getEquipment().setWeapon(new QilinBowCard(EH5031));
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8009)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(3))
                .withGeneralCard(new GeneralCard(General.關羽))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        playerB.getEquipment().setArmor(new EightDiagramTactic(ES2015));
        playerB.getEquipment().setPlusOne(new ShadowHorse(ES5018));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.張飛))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.趙雲))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withEquipment(new Equipment())
                .withHand(new Hand())
                .build();

        game.setPlayers(Arrays.asList(playerA, playerB, playerC, playerD));
        game.setCurrentRound(new Round(playerA));
        game.enterPhase(new Normal(game));

        // When
        game.playerPlayCard(playerA.getId(), BS8009.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events1 = game.playerUseEquipment(playerB.getId(), ES2015.getCardId(), playerA.getId(), EquipmentPlayType.ACTIVE);

        // B 發動八卦陣效果失敗
        assertFalse(events1.stream()
                .filter(event -> event instanceof EffectEvent)
                .map(EffectEvent.class::cast)
                .findFirst().get().isSuccess());

        List<DomainEvent> events2 = game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // A 收到是否要發動麒麟弓的事件，並選擇發動麒麟弓
        assertTrue(events2.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent));


        // A 發動麒麟弓效果
        List<DomainEvent> events3 = game.playerUseEquipment(playerA.getId(), EH5031.getCardId(), playerB.getId(), EquipmentPlayType.ACTIVE);

        // Then
        assertTrue(events3.stream().anyMatch(event -> event instanceof QilinBowCardEffectEvent));
        assertEquals(2, playerB.getBloodCard().getHp()); // B 受到傷害 Hp = 2
        assertNull(playerB.getEquipment().getPlusOne()); // B 沒有馬
    }

    @DisplayName("""
        Given
        A的回合
        A有麒麟弓
        B Hp = 3
        B 有八卦陣
        B 沒有閃
        B 有 + 1 與 -1 馬
    
        When
        A攻擊B
        B發動八卦陣效果失敗
        B Skip 出閃
        A 收到是否要發動麒麟弓的事件
        A 發動麒麟弓效果
        A 選擇 -1馬移除
    
        Then
        B HP = 2
        B 有 + 1 馬
    """)
    @Test
    public void givenPlayerATurn_PlayerAHasQilinBow_PlayerBHpIs3_PlayerBHasEightDiagramTacticNoDodge_PlayerBHasMounts_WhenPlayerAAttacksPlayerB_PlayerBActivatesEightDiagramFails_PlayerBSkipsDodge_PlayerAActivatesQilinBowEffectAndChoosesMinusOneHorse_ThenPlayerBHp2AndStillHasPlusOneHorse() {
        Game game = new Game();
        game.initDeck();
        Deck deck = new Deck(
                List.of(
                        new Kill(BC2054)
                )
        );
        game.setDeck(deck);
        // 設定 A 擁有麒麟弓
        Equipment equipmentA = new Equipment();
        equipmentA.setWeapon(new QilinBowCard(EH5031));

        // 設定 B 擁有八卦陣與 +1、-1 馬
        Equipment equipmentB = new Equipment();
        equipmentB.setArmor(new EightDiagramTactic(ES2015));
        equipmentB.setMinusOne(new RedRabbitHorse(EH5044));
        equipmentB.setPlusOne(new ShadowHorse(ES5018));

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipmentA)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(3))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
                .build();

        List<Player> players = asList(playerA, playerB);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> eightDiagramEvents = game.playerUseEquipment(playerB.getId(), ES2015.getCardId(), playerB.getId(), EquipmentPlayType.ACTIVE);

        // 八卦陣效果失敗，B 選擇 Skip 出閃
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), "skip");

        // A 啟動麒麟弓效果並選擇 -1 馬移除
        List<DomainEvent> qilinBowEvents = game.playerUseEquipment(playerA.getId(), EH5031.getCardId(), playerB.getId(), EquipmentPlayType.ACTIVE);
        List<DomainEvent> chooseHorseEvents = game.playerChooseHorseForQilinBow(playerA.getId(), EH5044.getCardId());

        // Then
        assertEquals(2, game.getPlayer("player-b").getBloodCard().getHp()); // B 血量變為 2
        assertNull(game.getPlayer("player-b").getEquipment().getMinusOne()); // B 沒有 -1 馬
        assertNotNull(game.getPlayer("player-b").getEquipment().getPlusOne()); // B 仍然有 +1 馬
    }

}

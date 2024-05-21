package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
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
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.EffectEvent;
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
        assertTrue(events.stream().map(EffectEvent.class::cast).allMatch(EffectEvent::isSuccess));
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
}

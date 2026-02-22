package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.behavior.behavior.DuelBehavior;
import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.Utils.getEvent;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class DuelTest {

    @DisplayName("""
            Given
            玩家ABCD
            A的回合
            A有決鬥 x 1 沒有殺
                        
            B 沒有殺，B 4hp
            When
            A 出決鬥，指定 B
                        
            Then
            B 扣血, B 3hp
            """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasDuelAndNoKill_BPlayerHasNoKillAndIsAt4HP_WhenPlayerAPlaysDuelAndAssignsB_ThenPlayerBLoses1HPAndIsAt3HP() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

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
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());


        //Then
        DuelEvent duelEvent = getEvent(events, DuelEvent.class).orElseThrow(RuntimeException::new);
        PlayerDamagedEvent playerDamagedEvent = getEvent(events, PlayerDamagedEvent.class).orElseThrow(RuntimeException::new);
        GameStatusEvent gameStatusEvent = getEvent(events, GameStatusEvent.class).orElseThrow(RuntimeException::new);

        assertFalse(game.getTopBehavior().stream().anyMatch(behavior -> behavior instanceof DuelBehavior));
        assertNotNull(duelEvent);
        assertNotNull(playerDamagedEvent);
        assertNotNull(gameStatusEvent);
        assertEquals("player-a", duelEvent.getDuelPlayerId());
        assertEquals("SSA001", duelEvent.getDuelCardId());
        assertEquals("player-b", duelEvent.getTargetPlayerId());
        assertEquals("player-a", game.getActivePlayer().getId());

        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
        assertEquals(4, game.getPlayer("player-a").getBloodCard().getHp());
    }

    @DisplayName("""
            Given
            玩家ABCD
            A的回合，A有裝備麒麟弓
            A有決鬥 x 1 沒有殺
                    
            B 沒有殺，B 4hp
            When
            A 出決鬥，指定 B
                    
            Then
            B 扣血, B 3hp
            不會詢問是否發動麒麟弓效果
                    """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAEquippedWithKylinBowHasDuelAndNoKill_BPlayerHasNoKillAndIsAt4HP_WhenPlayerAPlaysDuelAndAssignsB_ThenPlayerBLoses1HPAndIsAt3HPWithoutPromptingQilinBowEffect() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Equipment equipmentQilinBow = new Equipment();
        equipmentQilinBow.setWeapon(new QilinBowCard(EH5031));
        playerA.setEquipment(equipmentQilinBow);

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
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());


        //Then
        assertFalse(events.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent));
        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
    }

    @DisplayName("""
            Given
            玩家ABCD
            A的回合，B有裝備八卦陣
            A有決鬥 x 1 沒有殺
                    
            B 沒有殺，B 4hp
            When
            A 出決鬥，指定 B
                    
            Then
            B 扣血, B 3hp
            不會詢問是否發動八卦陣效果
                    """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasDuelAndNoKill_BPlayerEquippedWithEightDiagramTacticAndHasNoKillAndIsAt4HP_WhenPlayerAPlaysDuelAndAssignsB_ThenPlayerBLoses1HPAndIsAt3HPWithoutPromptingEightDiagramTacticEffect() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        Equipment equipment = new Equipment();
        equipment.setArmor(new EightDiagramTactic(ES2015));
        playerB.setEquipment(equipment);

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
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());


        //Then
        assertFalse(events.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent));
        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
    }

    @DisplayName("""
            Given
            玩家 A B C D
            A有決鬥 x 1 沒有殺
            
            B 有殺 x 2 ，B 4hp
            
            When
            A 出決鬥，指定 B
            B 出殺
            
            Then
            active player 是 玩家A 
                    """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAEquippedWithRepeatingCrossbowHasDuelAndNoKill_BPlayerHasNoKillAndIsAt4HP_WhenPlayerAPlaysDuelAndAssignsB_ThenPlayerBLoses1HPAndIsAt3HPWithoutPromptingRepeatingCrossbowEffect() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        Equipment equipment = new Equipment();
        equipment.setArmor(new EightDiagramTactic(ES2015));
        playerB.setEquipment(equipment);

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009), new Dodge(BHK039), new Duel(SSA001)));

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
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        //Then 要求 player-b 出殺 AskKillEvent
        AskKillEvent askKillEvent = getEvent(events, AskKillEvent.class).orElseThrow(RuntimeException::new);
        assertEquals("player-b", askKillEvent.getPlayerId());

        //When player-b 出殺
        events = game.playerPlayCard(playerB.getId(), BS8008.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        //Then
        assertEquals("player-a", game.getActivePlayer().getId());
        assertEquals(4, game.getPlayer("player-b").getBloodCard().getHp());

    }

    @DisplayName("""
            Given
            玩家 A B C D
            A的回合
            A有決鬥 x 1,沒有殺, A 4hp
            
            B 殺 x 1，B 4hp
            
            When
            A 出決鬥，指定 B
            B 出殺
            
            Then
            A 扣血, A 3hp
            B 沒扣血, B 4hp
            """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasDuelAndNoKillWith4HP_BPlayerHasKillAnd4HP_WhenPlayerAPlaysDuelAndAssignsB_AndPlayerBPlaysKill_ThenPlayerALoses1HPAndIsAt3HPWhilePlayerBRemainsAt4HP() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Dodge(BHK039), new Duel(SSA001)));

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
        game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), BS8008.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        //Then
        assertEquals("player-a", game.getActivePlayer().getId());
        assertEquals(4, game.getPlayer("player-b").getBloodCard().getHp());
        assertEquals(3, game.getPlayer("player-a").getBloodCard().getHp());

    }

    @DisplayName("""
            Given
            玩家 A B C D
            A的回合
            A有決定 x 1,沒有殺, A 4hp
           
            B 殺 x 1，B 4hp
            
            When
            A 出決鬥，指定 B
            B 不出殺
            
            Then
            A 不扣血, A 4hp
            B 扣血, B 3hp
            """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasDuelAndNoKillWith4HP_BPlayerHasKillAnd4HP_WhenPlayerAPlaysDuelAndAssignsB_AndPlayerBDoesNotPlayKill_ThenPlayerADoesNotLoseHPAndRemainsAt4HPWhilePlayerBLoses1HPAndIsAt3HP() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Dodge(BHK039), new Duel(SSA001)));

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
        game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        //Then
        assertEquals("player-a", game.getActivePlayer().getId());
        assertEquals(4, game.getPlayer("player-a").getBloodCard().getHp());
        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());

    }

    @DisplayName("""
            Given
            玩家ABCD
            A的回合
            A有決鬥 x 1,殺 x 1, A 4hp
            
            B 殺 x 2，B 4hp
            
            When
            A 出決鬥，指定 B
            B 出殺
            A 出殺
            B 出殺
            
          
            Then
            A 扣血, A 3hp
            B 沒扣血, B 4hp
            """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasDuelAndKillWith4HP_BPlayerHasTwoKillsAnd4HP_WhenPlayerAPlaysDuelAndAssignsB_AndPlayersAlternateKillsUntilBPlaysLastKill_ThenPlayerALoses1HPAndIsAt3HPWhilePlayerBRemainsAt4HP() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Kill(BS8008), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS8008), new Dodge(BHK039), new Duel(SSA001)));

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
        game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerA.getId(), "BS8008", playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), PlayType.ACTIVE.getPlayType());

        //Then
        assertEquals("player-a", game.getActivePlayer().getId());
        assertEquals(4, game.getPlayer("player-b").getBloodCard().getHp());
        assertEquals(3, game.getPlayer("player-a").getBloodCard().getHp());
    }

    @DisplayName("""
            Given
            玩家A B C D
            A的回合
            A有決鬥 x 1,殺 x 1, A 4hp
            
            B 殺 x 2，B 4hp
            
            When
            A 出決鬥，指定 B
            B 出殺
            A 出殺
            B 不出殺
            
            Then
            A 沒扣血, A 4hp
            B 扣血, B 3hp
                    """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasDuelAndKillWith4HP_BPlayerHasTwoKillsAnd4HP_WhenPlayerAPlaysDuelAndAssignsB_AndPlayersAlternateKillsUntilBDoesNotPlayKill_ThenPlayerADoesNotLoseHPAndRemainsAt4HPWhilePlayerBLoses1HPAndIsAt3HP() {
        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Kill(BS8008), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS8008), new Dodge(BHK039), new Duel(SSA001)));

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
        game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), "BS8008", playerA.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerA.getId(), "BS8008", playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        //Then
        assertEquals("player-a", game.getActivePlayer().getId());
        assertEquals(3, game.getPlayer("player-b").getBloodCard().getHp());
        assertEquals(4, game.getPlayer("player-a").getBloodCard().getHp());

    }

    @DisplayName("""
            Given
            玩家A B C D
            A的回合
            A有決鬥 x 1,殺 x 1, A 4hp
            
            B 殺 x 2，B 4hp
            
            When
            A 出決鬥，指定 A
            
            Then
            拋錯，錯誤操作
            """)
    @Test
    public void givenPlayerABCD_PlayerATurn_PlayerAHasDuelAndKillWith4HP_BPlayerHasTwoKillsAnd4HP_WhenPlayerAPlaysDuelAndAssignsSelf_ThenThrowIllegalOperationException() {

        Game game = new Game();
        game.initDeck();
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withEquipment(new Equipment())
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Peach(BH3029), new Kill(BS8008), new Dodge(BH2028), new Dodge(BHK039), new Duel(SSA001)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(new Equipment())
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS8008), new Dodge(BHK039), new Duel(SSA001)));

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
        //Then
        Assertions.assertThrows(IllegalArgumentException.class, () -> game.playerPlayCard(playerA.getId(), SSA001.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType()));

    }
}

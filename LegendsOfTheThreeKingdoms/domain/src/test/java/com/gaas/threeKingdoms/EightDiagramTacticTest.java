package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskPlayEquipmentEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.EffectEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class EightDiagramTacticTest {

    @DisplayName("""
            Given
            A的回合
            A的手牌也有一張八卦陣
                    
            When
            A 出八卦陣
                    
            Then
            A的裝備卡防具欄位有八卦陣
                """)
    @Test
    public void givenPlayerAHasEightDiagramTactic_WhenPlayerAPlayEightDiagramTactic_ThenPlayerAHaveEightDiagramTactic() {
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

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new EightDiagramTactic(EC2067)));

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
        game.playerPlayCard(playerA.getId(), EC2067.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        //Then
        assertEquals(new EightDiagramTactic(EC2067), game.getPlayer("player-a").getEquipmentArmorCard());
    }

    @DisplayName("""
            Given
            B的回合
            A已經裝備八卦陣
            B有四張殺
            A玩家HP=4
                   
                   
            When
            B玩家攻擊A玩家
                   
                   
            Then
            A 收到要不要發動裝備卡的 Event
              """)
    @Test
    public void givenPlayerAHasRedRabbitHorse_WhenPlayerAAttackPlayerC_ThenCHpIs3() {
        Game game = new Game();
        Equipment equipment = new Equipment();
        equipment.setArmor(new EightDiagramTactic(ES2015));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS8008), new Kill(BS8008), new Kill(BS8008)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerB));

        //When
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), BS8008.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        //Then
        assertTrue(events.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent));
    }

    @DisplayName("""
            Given
            B的回合
            A已經裝備八卦陣
            B有四張殺
            A玩家HP=4
            B玩家攻擊A玩家
            A收到要不要發動裝備卡的event
                    
            When
            A發動裝備卡
                        
            Then 
            全部人收到 八卦陣效果抽到赤兔馬 (♥5) 的 Event 
            Event 內是效果成功， A 不用出閃
            A玩家HP=4
            還是 B 的回合
            """)
    @Test
    public void givenPlayerAUesEightDiagramTactic_WhenGetRedRabbitHorse_ThenPlayerADontNeedToDodgeAndHpIs4() {
        Game game = new Game();
        Deck deck = new Deck(
                List.of(
                        new RedRabbitHorse(BH3029)
                )
        );
        game.setDeck(deck);
        Equipment equipment = new Equipment();
        equipment.setArmor(new EightDiagramTactic(ES2015));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BD6084), new Kill(BD7085), new Kill(BD8086), new Kill(BD0088)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerB));
        game.playerPlayCard(playerB.getId(), BD6084.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        //When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), ES2015.getCardId(), playerA.getId(), PlayType.EQUIPMENT_ACTIVE.getPlayType());

        //Then
        assertTrue(events.stream().map(EffectEvent.class::cast).allMatch(EffectEvent::isSuccess));
        assertEquals("player-b", game.getCurrentRound().getCurrentRoundPlayer().getId());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertEquals(4, game.getPlayer("player-a").getHP());
    }

    @DisplayName("""
            Given
            B的回合
            A已經裝備八卦陣，且有閃
            B有四張殺
            A玩家HP=4
            B玩家攻擊A玩家
            A發動裝備卡效果
                    
            When
            八卦陣效果抽到大老二
                    
            Then
            八卦陣效果event isSuccess = false
                """)
    @Test
    public void givenPlayerAUesEightDiagramTactic_WhenGetBig2_ThenPlayerABeenAskIfWantToDodge() {
        Game game = new Game();
        Deck deck = new Deck(
                List.of(
                        new RedRabbitHorse(ES2002)
                )
        );
        game.setDeck(deck);
        Equipment equipment = new Equipment();
        equipment.setArmor(new EightDiagramTactic(ES2015));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BD6084), new Kill(BD7085), new Kill(BD8086), new Kill(BD0088)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerB));

        game.playerPlayCard(playerB.getId(), BD6084.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        //When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), ES2015.getCardId(), playerA.getId(), PlayType.EQUIPMENT_ACTIVE.getPlayType());

        assertFalse(events.stream().map(EffectEvent.class::cast).allMatch(EffectEvent::isSuccess));
        assertEquals("player-b", game.getCurrentRound().getCurrentRoundPlayer().getId());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertEquals(4, game.getPlayer("player-a").getHP());
    }

    @DisplayName("""
            Given
            B的回合
            A已經裝備八卦陣
            B有四張殺
            A玩家HP=4
                    
            When
            B玩家攻擊A玩家
            A不發動裝備卡
            A不出閃
                    
            Then
            A HP=3
            還是 B 的回合
                """)
    @Test
    public void givenPlayerAHaveEightDiagramTactic_WhenANotUseEightDiagramTacticAndNotUseDodge_ThenPlayerAHpIs3AndCurrentRoundIsB() {
        Game game = new Game();
        Equipment equipment = new Equipment();
        equipment.setArmor(new EightDiagramTactic(ES2015));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BD6084), new Kill(BD7085), new Kill(BD8086), new Kill(BD0088)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerB));

        game.playerPlayCard(playerB.getId(), BD6084.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        //When
        game.playerPlayCard(playerA.getId(), ES2015.getCardId(), playerA.getId(), PlayType.EQUIPMENT_SKIP.getPlayType());
        game.playerPlayCard(playerA.getId(), "", playerB.getId(), PlayType.SKIP.getPlayType());

        assertEquals("player-b", game.getCurrentRound().getCurrentRoundPlayer().getId());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
        assertEquals(3, game.getPlayer("player-a").getHP());
    }

    @DisplayName("""
            Given
            B的回合
            A已經裝備八卦陣
            B有四張殺
            A玩家HP=4
                    
            When
            B玩家攻擊A玩家
            A不發動裝備卡
            A出閃
                    
            Then
            A HP=4
            還是 B 的回合
                """)
    @Test
    public void givenPlayerAHaveEightDiagramTactic_WhenANotUseEightDiagramTacticAndUseDodge_ThenPlayerAHpIs4AndCurrentRoundIsB() {
        Game game = new Game();
        Equipment equipment = new Equipment();
        equipment.setArmor(new EightDiagramTactic(ES2015));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipment)
                .withBloodCard(new BloodCard(4))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028), new Dodge(BHK039), new EightDiagramTactic(EC2067)));


        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BD6084), new Kill(BD7085), new Kill(BD8086), new Kill(BD0088)));

        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

        Player playerD = PlayerBuilder.construct()
                .withId("player-d")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.MINISTER))
                .build();

        List<Player> players = asList(
                playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerB));

        game.playerPlayCard(playerB.getId(), BD6084.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        //When
        game.playerPlayCard(playerA.getId(), ES2015.getCardId(), playerA.getId(), PlayType.EQUIPMENT_SKIP.getPlayType());
        game.playerPlayCard(playerA.getId(), BH2028.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        assertEquals("player-b", game.getCurrentRound().getCurrentRoundPlayer().getId());
        assertEquals(null, game.getCurrentRound().getActivePlayer());
        assertEquals(4, game.getPlayer("player-a").getHP());
    }
}

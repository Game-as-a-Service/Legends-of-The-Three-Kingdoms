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
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.BlackPommelCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class BlackPommelTest {

    @DisplayName("""
            Given
            A 的回合
            A 手牌有青釭劍 (ES6019)

            When
            A 出青釭劍

            Then
            A 的裝備卡武器欄位有青釭劍
            """)
    @Test
    public void givenPlayerAHasBlackPommel_WhenPlayerAPlaysBlackPommel_ThenPlayerAEquipsBlackPommel() {
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new BlackPommelCard(ES6019)));

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
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        game.playerPlayCard(playerA.getId(), ES6019.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // Then
        assertEquals(new BlackPommelCard(ES6019), game.getPlayer("player-a").getEquipmentWeaponCard());
    }

    @DisplayName("""
            Given
            A 的回合
            A 裝備青釭劍，手牌有殺
            B 裝備八卦陣，HP=4

            When
            A 對 B 出殺

            Then
            不會出現 AskPlayEquipmentEffectEvent
            直接出現 AskDodgeEvent（無視八卦陣）
            """)
    @Test
    public void givenPlayerAHasBlackPommelAndPlayerBHasEightDiagramTactic_WhenPlayerAKillsPlayerB_ThenArmorIsIgnored() {
        Game game = new Game();
        game.initDeck();

        Equipment equipmentA = new Equipment();
        equipmentA.setWeapon(new BlackPommelCard(ES6019));
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipmentA)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Kill(BS9009), new Dodge(BH2028)));

        Equipment equipmentB = new Equipment();
        equipmentB.setArmor(new EightDiagramTactic(ES2015));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
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
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // Then - no AskPlayEquipmentEffectEvent, directly AskDodgeEvent
        assertFalse(events.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent));
        assertTrue(events.stream().anyMatch(event -> event instanceof AskDodgeEvent));
    }

    @DisplayName("""
            Given
            A 的回合
            A 沒有武器，手牌有殺
            B 裝備八卦陣，HP=4

            When
            A 對 B 出殺

            Then
            出現 AskPlayEquipmentEffectEvent（正常觸發八卦陣）
            """)
    @Test
    public void givenPlayerAHasNoWeaponAndPlayerBHasEightDiagramTactic_WhenPlayerAKillsPlayerB_ThenArmorTriggersNormally() {
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(new Equipment())
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Kill(BS9009), new Dodge(BH2028)));

        Equipment equipmentB = new Equipment();
        equipmentB.setArmor(new EightDiagramTactic(ES2015));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
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
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        List<DomainEvent> events = game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());

        // Then - AskPlayEquipmentEffectEvent should appear (normal armor trigger)
        assertTrue(events.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent));
    }

    @DisplayName("""
            Given
            A 的回合
            A 已裝備諸葛連弩，手牌有青釭劍

            When
            A 出青釭劍

            Then
            A 武器欄變成青釭劍
            諸葛連弩進墓地
            """)
    @Test
    public void givenPlayerAHasRepeatingCrossbow_WhenPlayerAEquipsBlackPommel_ThenWeaponIsReplaced() {
        Game game = new Game();
        game.initDeck();

        Equipment equipmentA = new Equipment();
        equipmentA.setWeapon(new RepeatingCrossbowCard(ECA066));
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipmentA)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new BlackPommelCard(ES6019)));

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
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When
        game.playerPlayCard(playerA.getId(), ES6019.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // Then
        assertEquals(new BlackPommelCard(ES6019), game.getPlayer("player-a").getEquipmentWeaponCard());
        assertTrue(game.getGraveyard().getGraveyard().stream()
                .anyMatch(card -> card.getId().equals(ECA066.getCardId())));
    }

    @DisplayName("""
            Given
            A 的回合
            A 裝備青釭劍，手牌有殺
            B 裝備八卦陣，HP=4
            A 對 B 出殺（無視八卦陣，直接問閃）

            When
            B 不出閃

            Then
            B HP=3
            """)
    @Test
    public void givenBlackPommelKillIgnoresArmor_WhenTargetDoesNotDodge_ThenTargetTakesDamage() {
        Game game = new Game();
        game.initDeck();

        Equipment equipmentA = new Equipment();
        equipmentA.setWeapon(new BlackPommelCard(ES6019));
        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withEquipment(equipmentA)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Kill(BS9009)));

        Equipment equipmentB = new Equipment();
        equipmentB.setArmor(new EightDiagramTactic(ES2015));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withEquipment(equipmentB)
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
                .withRoleCard(new RoleCard(Role.MINISTER))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .build();

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // A kills B - armor ignored
        List<DomainEvent> killEvents = game.playerPlayCard(playerA.getId(), BS8008.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        assertFalse(killEvents.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent));

        // When - B does not dodge
        game.playerPlayCard(playerB.getId(), "", playerA.getId(), PlayType.SKIP.getPlayType());

        // Then
        assertEquals(3, game.getPlayer("player-b").getHP());
    }
}

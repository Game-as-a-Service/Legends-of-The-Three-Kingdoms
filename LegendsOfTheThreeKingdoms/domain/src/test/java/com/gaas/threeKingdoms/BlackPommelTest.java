package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
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

    @DisplayName("givenPlayerAHasBlackPommel_WhenPlayerAPlayBlackPommel_ThenPlayerAEquipBlackPommel")
    @Test
    public void givenPlayerAHasBlackPommel_WhenPlayerAPlayBlackPommel_ThenPlayerAEquipBlackPommel() {
        // Given: A的回合, A手牌有青釭劍, A武器欄沒有裝備
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

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028),
                new Dodge(BHK039), new BlackPommelCard(ES6019)));

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

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When: A出青釭劍
        game.playerPlayCard(playerA.getId(), ES6019.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // Then: A的裝備卡武器欄位有青釭劍, 攻擊距離為2
        assertEquals(new BlackPommelCard(ES6019), game.getPlayer("player-a").getEquipmentWeaponCard());
        assertEquals(2, game.getPlayer("player-a").getEquipmentWeaponCard().getWeaponDistance());
    }

    @DisplayName("givenPlayerBHasBlackPommel_WhenPlayerBKillsPlayerAWithEightDiagram_ThenSkipArmorEffect")
    @Test
    public void givenPlayerBHasBlackPommel_WhenPlayerBKillsPlayerAWithEightDiagram_ThenSkipArmorEffect() {
        // Given: B裝備青釭劍, A已裝備八卦陣
        Game game = new Game();
        game.initDeck();

        Equipment equipmentA = new Equipment();
        equipmentA.setArmor(new EightDiagramTactic(ES2015));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipmentA)
                .withBloodCard(new BloodCard(4))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new BlackPommelCard(ES6019));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipmentB)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();
        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009)));

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
        game.setCurrentRound(new Round(playerB));

        // When: B攻擊A
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), BS8008.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // Then: 無視八卦陣, 直接收到AskDodgeEvent
        assertFalse(events.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent),
                "BlackPommel should bypass armor effect");
        assertTrue(events.stream().anyMatch(event -> event instanceof AskDodgeEvent),
                "Target should be asked to dodge directly");
    }

    @DisplayName("givenPlayerBHasNoWeapon_WhenPlayerBKillsPlayerAWithEightDiagram_ThenArmorEffectTriggered")
    @Test
    public void givenPlayerBHasNoWeapon_WhenPlayerBKillsPlayerAWithEightDiagram_ThenArmorEffectTriggered() {
        // Given: B沒有武器, A已裝備八卦陣 (對照組)
        Game game = new Game();
        game.initDeck();

        Equipment equipmentA = new Equipment();
        equipmentA.setArmor(new EightDiagramTactic(ES2015));
        Player playerA = PlayerBuilder
                .construct()
                .withId("player-a")
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipmentA)
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
        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Kill(BS9009)));

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
        game.setCurrentRound(new Round(playerB));

        // When: B攻擊A
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), BS8008.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // Then: 正常觸發八卦陣
        assertTrue(events.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent),
                "Without BlackPommel, armor effect should trigger normally");
    }

    @DisplayName("givenBorrowedSword_WhenBorrowedPlayerHasBlackPommelAndTargetHasArmor_ThenSkipArmorEffect")
    @Test
    public void givenBorrowedSword_WhenBorrowedPlayerHasBlackPommelAndTargetHasArmor_ThenSkipArmorEffect() {
        // Given: A出借刀殺人, B裝備青釭劍殺C(裝備八卦陣)
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
        playerA.getHand().addCardToHand(Arrays.asList(new Kill(BS8008)));

        Equipment equipmentB = new Equipment();
        equipmentB.setWeapon(new BlackPommelCard(ES6019));
        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipmentB)
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();
        playerB.getHand().addCardToHand(Arrays.asList(new Kill(BS9009), new Dodge(BH2028)));

        Equipment equipmentC = new Equipment();
        equipmentC.setArmor(new EightDiagramTactic(EC2067));
        Player playerC = PlayerBuilder.construct()
                .withId("player-c")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(equipmentC)
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

        // A plays BorrowedSword targeting B to kill C
        game.playerPlayCard(playerA.getId(), SCK065.getCardId(), playerB.getId(), PlayType.ACTIVE.getPlayType());
        game.useBorrowedSwordEffect(playerA.getId(), playerB.getId(), playerC.getId());

        // When: B出殺攻擊C
        List<DomainEvent> events = game.playerPlayCard(playerB.getId(), BS9009.getCardId(), playerC.getId(), PlayType.ACTIVE.getPlayType());

        // Then: 青釭劍無視八卦陣
        assertFalse(events.stream().anyMatch(event -> event instanceof AskPlayEquipmentEffectEvent),
                "BlackPommel should bypass armor effect in BorrowedSword scenario");
        assertTrue(events.stream().anyMatch(event -> event instanceof AskDodgeEvent),
                "Target should be asked to dodge directly");
    }

    @DisplayName("givenPlayerAEquippedRepeatingCrossbow_WhenPlayerAPlayBlackPommel_ThenPlayerAHaveBlackPommel")
    @Test
    public void givenPlayerAEquippedRepeatingCrossbow_WhenPlayerAPlayBlackPommel_ThenPlayerAHaveBlackPommel() {
        // Given: A裝備諸葛連弩
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

        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028),
                new Dodge(BHK039), new BlackPommelCard(ES6019)));

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

        List<Player> players = asList(playerA, playerB, playerC, playerD);
        game.setPlayers(players);
        game.enterPhase(new Normal(game));
        game.setCurrentRound(new Round(playerA));

        // When: A出青釭劍
        game.playerPlayCard(playerA.getId(), ES6019.getCardId(), playerA.getId(), PlayType.ACTIVE.getPlayType());

        // Then: 武器欄位變成青釭劍
        assertEquals(new BlackPommelCard(ES6019), game.getPlayer("player-a").getEquipmentWeaponCard());
    }
}

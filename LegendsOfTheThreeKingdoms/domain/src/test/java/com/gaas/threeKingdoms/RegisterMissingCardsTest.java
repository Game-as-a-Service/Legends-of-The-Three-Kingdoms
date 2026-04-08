package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.*;
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

public class RegisterMissingCardsTest {

    @Test
    @DisplayName("EC2067 八卦陣 should be registered in factory and return EightDiagramTactic")
    public void testEC2067_EightDiagramTactic() {
        HandCard card = PlayCard.findById("EC2067");
        assertNotNull(card);
        assertInstanceOf(EightDiagramTactic.class, card);
        assertEquals("EC2067", card.getId());
    }

    @Test
    @DisplayName("EDA092 諸葛連弩 should be registered in factory and return RepeatingCrossbowCard")
    public void testEDA092_RepeatingCrossbow() {
        HandCard card = PlayCard.findById("EDA092");
        assertNotNull(card);
        assertInstanceOf(RepeatingCrossbowCard.class, card);
        assertEquals("EDA092", card.getId());
    }

    @Test
    @DisplayName("EC5070 的盧 should be registered in factory and return DiLu (+1 mount)")
    public void testEC5070_DiLu() {
        HandCard card = PlayCard.findById("EC5070");
        assertNotNull(card);
        assertInstanceOf(DiLu.class, card);
        assertInstanceOf(PlusMountsCard.class, card);
        assertEquals("EC5070", card.getId());
    }

    @Test
    @DisplayName("EDK104 紫騂 should be registered in factory and return ZiXing (-1 mount)")
    public void testEDK104_ZiXing() {
        HandCard card = PlayCard.findById("EDK104");
        assertNotNull(card);
        assertInstanceOf(ZiXing.class, card);
        assertInstanceOf(MinusMountsCard.class, card);
        assertEquals("EDK104", card.getId());
    }

    @Test
    @DisplayName("EHK052 爪黃飛電 should be registered in factory and return ClawYellowFlyingElectric (+1 mount)")
    public void testEHK052_ClawYellowFlyingElectric() {
        HandCard card = PlayCard.findById("EHK052");
        assertNotNull(card);
        assertInstanceOf(ClawYellowFlyingElectric.class, card);
        assertInstanceOf(PlusMountsCard.class, card);
        assertEquals("EHK052", card.getId());
    }

    @Test
    @DisplayName("ESK026 黃爪飛電 should be registered in factory and return YellowClawFlyingElectric (-1 mount)")
    public void testESK026_YellowClawFlyingElectric() {
        HandCard card = PlayCard.findById("ESK026");
        assertNotNull(card);
        assertInstanceOf(YellowClawFlyingElectric.class, card);
        assertInstanceOf(MinusMountsCard.class, card);
        assertEquals("ESK026", card.getId());
    }

    @Test
    @DisplayName("isMountsCard should return true for all 6 mount cards")
    public void testIsMountsCard() {
        assertTrue(ES5018.isMountsCard(), "絕影 should be a mount");
        assertTrue(EH5044.isMountsCard(), "赤兔 should be a mount");
        assertTrue(EC5070.isMountsCard(), "的盧 should be a mount");
        assertTrue(EDK104.isMountsCard(), "紫騂 should be a mount");
        assertTrue(EHK052.isMountsCard(), "爪黃飛電 should be a mount");
        assertTrue(ESK026.isMountsCard(), "黃爪飛電 should be a mount");
    }

    @Test
    @DisplayName("DiLu (+1 mount) should equip as plusOne and affect distance calculation")
    public void testDiLuEquipAndDistance() {
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withEquipment(new Equipment())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028),
                new Dodge(BHK039), new DiLu(EC5070)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

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
        game.setCurrentRound(new Round(playerA));

        // When A plays DiLu
        game.playerPlayCard(playerA.getId(), EC5070.getCardId(), playerA.getId(), "active");

        // Then A's equipment should have DiLu as +1 mount
        assertEquals(EC5070.getCardId(), game.getPlayer("player-a").getEquipmentPlusOneMountsCard().getId());
    }

    @Test
    @DisplayName("ZiXing (-1 mount) should equip as minusOne and affect distance calculation")
    public void testZiXingEquipAndDistance() {
        Game game = new Game();
        game.initDeck();

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withEquipment(new Equipment())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .build();
        playerA.getHand().addCardToHand(Arrays.asList(
                new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028),
                new Dodge(BHK039), new ZiXing(EDK104)));

        Player playerB = PlayerBuilder.construct()
                .withId("player-b")
                .withBloodCard(new BloodCard(4))
                .withHand(new Hand())
                .withGeneralCard(new GeneralCard(General.劉備))
                .withHealthStatus(HealthStatus.ALIVE)
                .withEquipment(new Equipment())
                .withRoleCard(new RoleCard(Role.TRAITOR))
                .build();

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
        game.setCurrentRound(new Round(playerA));

        // When A plays ZiXing
        game.playerPlayCard(playerA.getId(), EDK104.getCardId(), playerA.getId(), "active");

        // Then A's equipment should have ZiXing as -1 mount
        assertEquals(EDK104.getCardId(), game.getPlayer("player-a").getEquipmentMinusOneMountsCard().getId());
    }
}

package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.*;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

public class RegisterMissingCardsTest {

    @DisplayName("EC2067 八卦陣 should be findable via factory")
    @Test
    public void testEC2067EightDiagramTacticFactory() {
        HandCard card = PlayCard.findById("EC2067");
        assertNotNull(card);
        assertInstanceOf(EightDiagramTactic.class, card);
    }

    @DisplayName("EDA092 諸葛連弩 should be findable via factory")
    @Test
    public void testEDA092RepeatingCrossbowFactory() {
        HandCard card = PlayCard.findById("EDA092");
        assertNotNull(card);
        assertInstanceOf(RepeatingCrossbowCard.class, card);
    }

    @DisplayName("EC5070 的盧 (HexMark) findById returns PlusMountsCard and isMountsCard")
    @Test
    public void testEC5070HexMarkFactory() {
        HandCard card = PlayCard.findById("EC5070");
        assertNotNull(card);
        assertInstanceOf(HexMark.class, card);
        assertInstanceOf(PlusMountsCard.class, card);
        assertTrue(EC5070.isMountsCard());
    }

    @DisplayName("EDK104 紫騂 (VioletStallion) findById returns MinusMountsCard and isMountsCard")
    @Test
    public void testEDK104VioletStallionFactory() {
        HandCard card = PlayCard.findById("EDK104");
        assertNotNull(card);
        assertInstanceOf(VioletStallion.class, card);
        assertInstanceOf(MinusMountsCard.class, card);
        assertTrue(EDK104.isMountsCard());
    }

    @DisplayName("EHK052 爪黃飛電 (YellowFlash) findById returns PlusMountsCard and isMountsCard")
    @Test
    public void testEHK052YellowFlashFactory() {
        HandCard card = PlayCard.findById("EHK052");
        assertNotNull(card);
        assertInstanceOf(YellowFlash.class, card);
        assertInstanceOf(PlusMountsCard.class, card);
        assertTrue(EHK052.isMountsCard());
    }

    @DisplayName("ESK026 爪黃飛電 (FerghanaHorse) findById returns MinusMountsCard and isMountsCard")
    @Test
    public void testESK026FerghanaHorseFactory() {
        HandCard card = PlayCard.findById("ESK026");
        assertNotNull(card);
        assertInstanceOf(FerghanaHorse.class, card);
        assertInstanceOf(MinusMountsCard.class, card);
        assertTrue(ESK026.isMountsCard());
    }

    @DisplayName("HexMark (+1馬) equipped increases escape distance by 1")
    @Test
    public void testHexMarkEscapeDistance() {
        Equipment equipmentWithHexMark = new Equipment();
        equipmentWithHexMark.setPlusOne(new HexMark(EC5070));

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withEquipment(equipmentWithHexMark)
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
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

        assertEquals(1, playerA.judgeEscapeDistance());
        assertEquals(0, playerB.judgeEscapeDistance());
    }

    @DisplayName("VioletStallion (-1馬) equipped increases attack distance by 1")
    @Test
    public void testVioletStallionAttackDistance() {
        Equipment equipmentWithVioletStallion = new Equipment();
        equipmentWithVioletStallion.setMinusOne(new VioletStallion(EDK104));

        Player playerA = PlayerBuilder.construct()
                .withId("player-a")
                .withHand(new Hand())
                .withBloodCard(new BloodCard(4))
                .withEquipment(equipmentWithVioletStallion)
                .withGeneralCard(new GeneralCard(General.劉備))
                .withRoleCard(new RoleCard(Role.MONARCH))
                .withHealthStatus(HealthStatus.ALIVE)
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

        // With -1 mount: attack distance = 0(weapon) + 1(minusOne) + 1(base) = 2
        assertEquals(2, playerA.judgeAttackDistance());
        // Without: attack distance = 0(weapon) + 0(minusOne) + 1(base) = 1
        assertEquals(1, playerB.judgeAttackDistance());
    }
}

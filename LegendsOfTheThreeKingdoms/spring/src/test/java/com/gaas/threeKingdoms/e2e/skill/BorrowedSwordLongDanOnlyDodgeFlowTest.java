package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 借刀殺人：趙雲只有閃（沒有殺）→ 要問他出殺，龍膽以閃當殺後 c 被問閃、武器不被奪（每步經 MongoDB 讀回）。 */
public class BorrowedSwordLongDanOnlyDodgeFlowTest extends AbstractBaseIntegrationTest {

    @DisplayName("借刀殺人 → 趙雲只有閃 → 龍膽出殺 → c 被問閃")
    @Test
    public void zhaoYunOnlyDodgeCanAnswerBorrowedSwordWithLongDan() throws Exception {
        Player a = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH, new BorrowedSword(SCK065));
        Player zhaoYun = createPlayer("player-b", 4, General.趙雲, HealthStatus.ALIVE, Role.MINISTER, new Dodge(BH2028));
        zhaoYun.getEquipment().setWeapon(new QilinBowCard(EH5031));
        Player c = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player d = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        repository.save(initGame(gameId, Arrays.asList(a, zhaoYun, c, d), a));

        mockMvcUtil.playCard(gameId, "player-a", "player-b", SCK065.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        mockMvcUtil.useBorrowedSwordEffect(gameId, "player-a", "player-b", "player-c").andExpect(status().isOk());

        Game asked = repository.findById(gameId).orElseThrow();
        assertEquals("player-b", asked.getCurrentRound().getActivePlayer().getId(), "要問趙雲出殺（MongoDB 讀回）");
        assertNotNull(asked.getPlayer("player-b").getEquipmentWeaponCard(), "武器還在趙雲身上");

        mockMvcUtil.useSkillEffect(gameId, "player-b", "龍膽", "KILL", List.of(BH2028.getCardId()), "player-c")
                .andExpect(status().isOk());

        Game after = repository.findById(gameId).orElseThrow();
        assertEquals("player-c", after.getCurrentRound().getActivePlayer().getId(), "c 被問閃");
        assertInstanceOf(QilinBowCard.class, after.getPlayer("player-b").getEquipmentWeaponCard());
        assertEquals(0, after.getPlayer("player-a").getHand().getCards().size(), "a 沒拿到武器");
    }
}

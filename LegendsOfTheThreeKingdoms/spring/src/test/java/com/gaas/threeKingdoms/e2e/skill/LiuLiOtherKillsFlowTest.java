package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.HeavenlyDoubleHalberdKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.ViperSpearKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.EighteenSpanViperSpearCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.HeavenlyDoubleHalberdCard;
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

/**
 * 流離適用丈八蛇矛、方天畫戟的殺（跨 HTTP 請求，每步經 MongoDB 讀回）。
 * 座位 a → b → c → d。
 */
public class LiuLiOtherKillsFlowTest extends AbstractBaseIntegrationTest {

    private void givenGame(Player attacker, General b, General c) {
        Player playerB = createPlayer("player-b", 4, b, HealthStatus.ALIVE, Role.REBEL);
        Player playerC = createPlayer("player-c", 4, c, HealthStatus.ALIVE, Role.MINISTER);
        Player playerD = createPlayer("player-d", 4, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Player daQiao = b == General.大喬 ? playerB : playerC;
        daQiao.getHand().addCardToHand(List.of(new Peach(BH3029), new Dodge(BH2028)));
        Game game = initGame(gameId, Arrays.asList(attacker, playerB, playerC, playerD), attacker);
        repository.save(game);
    }

    private Player halberdAttacker() {
        Player a = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH, new Kill(BS8008));
        a.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        return a;
    }

    private void assertAskingLiuLi() {
        Game asking = repository.findById(gameId).orElseThrow();
        WaitingSkillEffectBehavior liuLi = assertInstanceOf(WaitingSkillEffectBehavior.class, asking.peekTopBehavior());
        assertEquals("流離", liuLi.getSkillName());
    }

    @DisplayName("方天畫戟殺 [大喬, d] → 流離轉給 c → c 不出閃扣血 → 輪到 d → 結束")
    @Test
    public void halberdRedirect() throws Exception {
        givenGame(halberdAttacker(), General.大喬, General.甘寧);

        mockMvcUtil.useHeavenlyDoubleHalberdKill(gameId, "player-a", BS8008.getCardId(), List.of("player-b", "player-d"))
                .andExpect(status().isOk());
        assertAskingLiuLi();

        mockMvcUtil.useSkillEffect(gameId, "player-b", "流離", "ACCEPT", List.of(BH3029.getCardId()), "player-c")
                .andExpect(status().isOk());
        Game redirected = repository.findById(gameId).orElseThrow();
        HeavenlyDoubleHalberdKillBehavior halberd =
                assertInstanceOf(HeavenlyDoubleHalberdKillBehavior.class, redirected.peekTopBehavior());
        assertEquals(List.of("player-c", "player-d"), halberd.getReactionPlayers(), "c 取代大喬的位置（MongoDB 讀回）");
        assertEquals("player-c", halberd.getCurrentReactionPlayer().getId());

        mockMvcUtil.playCard(gameId, "player-c", "player-a", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());
        Game afterC = repository.findById(gameId).orElseThrow();
        assertEquals(3, afterC.getPlayer("player-c").getHP());
        assertEquals("player-d", afterC.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.playCard(gameId, "player-d", "player-a", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());
        Game done = repository.findById(gameId).orElseThrow();
        assertEquals(3, done.getPlayer("player-d").getHP());
        assertEquals(4, done.getPlayer("player-b").getHP());
        assertTrue(done.getTopBehavior().isEmpty());
    }

    @DisplayName("方天畫戟殺 [b, 大喬] → b 結算後才問流離 → 放棄 → 大喬被問閃，不會再問一次流離")
    @Test
    public void halberdDeclineOnLaterTarget() throws Exception {
        givenGame(halberdAttacker(), General.甘寧, General.大喬);

        mockMvcUtil.useHeavenlyDoubleHalberdKill(gameId, "player-a", BS8008.getCardId(), List.of("player-b", "player-c"))
                .andExpect(status().isOk());
        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());
        assertAskingLiuLi();

        mockMvcUtil.useSkillEffect(gameId, "player-c", "流離", "SKIP", null, null).andExpect(status().isOk());
        Game declined = repository.findById(gameId).orElseThrow();
        HeavenlyDoubleHalberdKillBehavior halberd =
                assertInstanceOf(HeavenlyDoubleHalberdKillBehavior.class, declined.peekTopBehavior());
        assertEquals("player-c", halberd.getParam(NormalActiveKillBehavior.PARAM_LIULI_DECIDED_TARGET),
                "已決定流離（MongoDB 讀回）");
        assertEquals("player-c", declined.getCurrentRound().getActivePlayer().getId());

        mockMvcUtil.playCard(gameId, "player-c", "player-a", BH2028.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());
        Game done = repository.findById(gameId).orElseThrow();
        assertEquals(3, done.getPlayer("player-b").getHP());
        assertEquals(4, done.getPlayer("player-c").getHP(), "大喬出閃");
        assertTrue(done.getTopBehavior().isEmpty());
    }

    @DisplayName("丈八蛇矛殺大喬 → 流離轉給 c → c 不出閃扣血")
    @Test
    public void viperSpearRedirect() throws Exception {
        Player a = createPlayer("player-a", 4, General.甘寧, HealthStatus.ALIVE, Role.MONARCH,
                new Kill(BS8008), new Kill(BS9009));
        a.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        givenGame(a, General.大喬, General.甘寧);

        mockMvcUtil.useViperSpearKill(gameId, "player-a", "player-b", List.of(BS8008.getCardId(), BS9009.getCardId()))
                .andExpect(status().isOk());
        assertAskingLiuLi();

        mockMvcUtil.useSkillEffect(gameId, "player-b", "流離", "ACCEPT", List.of(BH3029.getCardId()), "player-c")
                .andExpect(status().isOk());
        Game redirected = repository.findById(gameId).orElseThrow();
        ViperSpearKillBehavior viper = assertInstanceOf(ViperSpearKillBehavior.class, redirected.peekTopBehavior());
        assertEquals(List.of("player-c"), viper.getReactionPlayers());
        assertEquals("true", viper.getParam(NormalActiveKillBehavior.PARAM_LIULI_REDIRECTED), "MongoDB 讀回");

        mockMvcUtil.playCard(gameId, "player-c", "player-a", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());
        Game done = repository.findById(gameId).orElseThrow();
        assertEquals(3, done.getPlayer("player-c").getHP());
        assertEquals(4, done.getPlayer("player-b").getHP());
        assertTrue(done.getTopBehavior().isEmpty());
    }
}

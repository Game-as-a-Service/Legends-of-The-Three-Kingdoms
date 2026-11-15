package com.gaas.threeKingdoms.e2e.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.DyingAskPeachBehavior;
import com.gaas.threeKingdoms.behavior.behavior.HeavenlyDoubleHalberdKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.e2e.testcontainer.test.AbstractBaseIntegrationTest;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.HeavenlyDoubleHalberdCard;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.e2e.MockUtil.createPlayer;
import static com.gaas.threeKingdoms.e2e.MockUtil.initGame;
import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 方天畫戟的殺判鐵騎（跨 HTTP 請求，每步經 MongoDB 讀回）。座位 a → b → c → d。
 */
public class HalberdTieQiFlowTest extends AbstractBaseIntegrationTest {

    private Player maChaoWithHalberd() {
        Player a = createPlayer("player-a", 4, General.馬超, HealthStatus.ALIVE, Role.MONARCH, new Kill(BS8008));
        a.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        return a;
    }

    /** Deck 是 stack：list 最後一張最先被抽。 */
    private void saveWithDeck(Game game, List<com.gaas.threeKingdoms.handcard.HandCard> bottomToTop) {
        Deck deck = new Deck();
        deck.add(new ArrayList<>(bottomToTop));
        game.setDeck(deck);
        repository.save(game);
    }

    @DisplayName("方天畫戟殺 [b, c]：b 的鐵騎判紅 → d（司馬懿）鬼才改黑 → 問 b 出閃 → b 不出 → c 判紅直接扣血")
    @Test
    public void guiCaiPauseDuringHalberdTieQi() throws Exception {
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerC = createPlayer("player-c", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        // 司馬懿只有一張手牌：用掉後 c 的判定不會再暫停
        Player simaYi = createPlayer("player-d", 4, General.司馬懿, HealthStatus.ALIVE, Role.TRAITOR, new Kill(BS9009));
        Player maChao = maChaoWithHalberd();
        Game game = initGame(gameId, Arrays.asList(maChao, playerB, playerC, simaYi), maChao);
        // 抽牌順序：b 的判定（紅）→ c 的判定（紅）
        saveWithDeck(game, List.of(new Peach(BH7033), new Peach(BH4030)));

        mockMvcUtil.useHeavenlyDoubleHalberdKill(gameId, "player-a", BS8008.getCardId(), List.of("player-b", "player-c"))
                .andExpect(status().isOk());
        Game paused = repository.findById(gameId).orElseThrow();
        WaitingSkillEffectBehavior guiCai = assertInstanceOf(WaitingSkillEffectBehavior.class, paused.peekTopBehavior());
        assertEquals("鬼才", guiCai.getSkillName());

        mockMvcUtil.useSkillEffect(gameId, "player-d", "鬼才", "ACCEPT", List.of(BS9009.getCardId()), null)
                .andExpect(status().isOk());
        Game resumed = repository.findById(gameId).orElseThrow();
        assertInstanceOf(HeavenlyDoubleHalberdKillBehavior.class, resumed.peekTopBehavior());
        assertEquals("player-b", resumed.getCurrentRound().getActivePlayer().getId(), "改成黑色 → 照常問 b 出閃");
        assertEquals(4, resumed.getPlayer("player-b").getHP());

        mockMvcUtil.playCard(gameId, "player-b", "player-a", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());
        Game done = repository.findById(gameId).orElseThrow();
        assertEquals(3, done.getPlayer("player-b").getHP());
        assertEquals(3, done.getPlayer("player-c").getHP(), "c 判紅 → 不能出閃直接扣血");
        assertTrue(done.getTopBehavior().isEmpty());
        assertEquals("player-a", done.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("方天畫戟殺 [c, d]（都 1 血）：c 被鐵騎打死 → 接回時 d 也被鐵騎打到瀕死 → 出桃救回 → 殺結束")
    @Test
    public void tieQiDamageWhileResumingFromDeath() throws Exception {
        Player playerB = createPlayer("player-b", 4, General.甘寧, HealthStatus.ALIVE, Role.MINISTER);
        Player playerC = createPlayer("player-c", 1, General.甘寧, HealthStatus.ALIVE, Role.REBEL);
        Player playerD = createPlayer("player-d", 1, General.甘寧, HealthStatus.ALIVE, Role.TRAITOR);
        Player maChao = maChaoWithHalberd();
        Game game = initGame(gameId, Arrays.asList(maChao, playerB, playerC, playerD), maChao);
        // 抽牌順序：c 的判定（紅）→ 反賊獎勵摸 3 → d 的判定（紅）
        saveWithDeck(game, List.of(new Peach(BH7033), new Kill(BS9009), new Kill(BS9009), new Kill(BS9009),
                new Peach(BH4030)));

        mockMvcUtil.useHeavenlyDoubleHalberdKill(gameId, "player-a", BS8008.getCardId(), List.of("player-c", "player-d"))
                .andExpect(status().isOk());
        Game state = repository.findById(gameId).orElseThrow();
        assertInstanceOf(DyingAskPeachBehavior.class, state.peekTopBehavior(), "c 瀕死");

        // c 瀕死：所有人不出桃
        while (state.peekTopBehavior() instanceof DyingAskPeachBehavior
                && "player-c".equals(state.getCurrentRound().getDyingPlayer().getId())) {
            String asked = state.getCurrentRound().getActivePlayer().getId();
            mockMvcUtil.playCard(gameId, asked, "player-c", "", PlayType.SKIP.getPlayType()).andExpect(status().isOk());
            state = repository.findById(gameId).orElseThrow();
        }
        assertTrue(state.getPlayer("player-c").isAlreadyDeath());
        assertEquals(0, state.getPlayer("player-d").getHP(), "接回後 d 判紅直接扣血");
        assertEquals("GeneralDying", state.getGamePhase().getPhaseName());
        assertEquals(2, state.getTopBehavior().size(), "halberd + d 的瀕死（舊瀕死已移出）：" + state.getTopBehavior());
        assertEquals("player-d", state.getCurrentRound().getDyingPlayer().getId());

        // 被問桃的人出桃救 d（直接塞一張桃給他）
        String asked = state.getCurrentRound().getActivePlayer().getId();
        state.getPlayer(asked).getHand().addCardToHand(List.of(new Peach(BH3029)));
        repository.save(state);
        mockMvcUtil.playCard(gameId, asked, "player-d", BH3029.getCardId(), PlayType.ACTIVE.getPlayType())
                .andExpect(status().isOk());

        Game done = repository.findById(gameId).orElseThrow();
        assertEquals(1, done.getPlayer("player-d").getHP());
        assertTrue(done.getTopBehavior().isEmpty(), "d 是最後一位 → 殺結束：" + done.getTopBehavior());
        assertEquals("player-a", done.getCurrentRound().getActivePlayer().getId());
    }
}

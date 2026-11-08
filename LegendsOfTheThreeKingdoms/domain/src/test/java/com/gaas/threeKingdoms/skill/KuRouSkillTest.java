package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.DyingAskPeachBehavior;
import com.gaas.threeKingdoms.events.AskPeachEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayerDyingEvent;
import com.gaas.threeKingdoms.gamephase.GeneralDying;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.BH3029;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 黃蓋 苦肉：失去 1 點體力（不是傷害），然後摸 2 張牌。
 * HP 1 也能發動 → 進瀕死求桃；救回才摸牌，死了不摸，也沒有人能領擊殺獎勵。
 */
public class KuRouSkillTest extends PassiveSkillTestBase {

    private static final String SKIP = PlayType.SKIP.getPlayType();

    /** 黃蓋為反賊、其他人用甘寧（不干擾流程）；輪到黃蓋的出牌階段。 */
    private Game createGameWithHuangGaiAsRebel() {
        Game game = new Game();
        game.initDeck();
        Player a = buildPlayer("player-a", General.黃蓋, Role.REBEL);
        Player b = buildPlayer("player-b", General.甘寧, Role.MONARCH);
        Player c = buildPlayer("player-c", General.甘寧, Role.MINISTER);
        Player d = buildPlayer("player-d", General.甘寧, Role.TRAITOR);
        game.setPlayers(asList(a, b, c, d));
        game.setCurrentRound(new Round(a));
        game.enterPhase(new Normal(game));
        return game;
    }

    @DisplayName("HP ≥ 2：失去 1 點體力並立即摸 2 張，不進瀕死")
    @Test
    public void kuRouWithEnoughHpDrawsImmediately() {
        Game game = createGameWithHuangGaiAsRebel();
        Player a = game.getPlayer("player-a");

        List<DomainEvent> events = game.playerUseSkillEffect("player-a", "苦肉", "ACCEPT", null, null);

        assertEquals(3, a.getHP());
        assertEquals(2, a.getHandSize());
        assertTrue(game.isTopBehaviorEmpty());
        assertTrue(events.stream().noneMatch(e -> e instanceof PlayerDyingEvent));
    }

    @DisplayName("HP 1：失去最後 1 點體力 → 進瀕死求桃，先不摸牌")
    @Test
    public void kuRouAtOneHpEntersDying() {
        Game game = createGameWithHuangGaiAsRebel();
        Player a = game.getPlayer("player-a");
        a.getBloodCard().setHp(1);

        List<DomainEvent> events = game.playerUseSkillEffect("player-a", "苦肉", "ACCEPT", null, null);

        assertEquals(0, a.getHP());
        assertEquals(0, a.getHandSize(), "救回前不摸牌");
        assertTrue(game.getGamePhase() instanceof GeneralDying);
        assertTrue(events.stream().anyMatch(e -> e instanceof PlayerDyingEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof AskPeachEvent));
        DyingAskPeachBehavior dying = assertInstanceOf(DyingAskPeachBehavior.class, game.peekTopBehavior());
        assertEquals(2, dying.getPendingDrawOnRevival());
        assertEquals("player-a", game.getCurrentRound().getDyingPlayer().getId());
    }

    @DisplayName("HP 1 苦肉後被桃救回 → 回到 1 血、摸 2 張，繼續自己的出牌階段")
    @Test
    public void kuRouRescuedDrawsTwo() {
        Game game = createGameWithHuangGaiAsRebel();
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getBloodCard().setHp(1);
        b.getHand().addCardToHand(List.of(new Peach(BH3029)));

        game.playerUseSkillEffect("player-a", "苦肉", "ACCEPT", null, null);
        game.playerPlayCard("player-a", "", "player-a", SKIP);           // 黃蓋自己沒有桃
        game.playerPlayCard("player-b", BH3029.getCardId(), "player-a", "active");

        assertEquals(1, a.getHP());
        assertEquals(2, a.getHandSize(), "救回後才摸 2 張");
        assertTrue(game.getGamePhase() instanceof Normal);
        assertTrue(game.isTopBehaviorEmpty());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("HP 1 苦肉沒人救 → 死亡、不摸牌；沒有傷害來源，沒人領反賊獎勵，不會卡住")
    @Test
    public void kuRouDeathHasNoKillerReward() {
        Game game = createGameWithHuangGaiAsRebel();
        Player a = game.getPlayer("player-a");
        a.getBloodCard().setHp(1);
        int bHand = game.getPlayer("player-b").getHandSize();
        int cHand = game.getPlayer("player-c").getHandSize();
        int dHand = game.getPlayer("player-d").getHandSize();

        game.playerUseSkillEffect("player-a", "苦肉", "ACCEPT", null, null);
        game.playerPlayCard("player-a", "", "player-a", SKIP);
        game.playerPlayCard("player-b", "", "player-a", SKIP);
        game.playerPlayCard("player-c", "", "player-a", SKIP);
        assertDoesNotThrow(() -> game.playerPlayCard("player-d", "", "player-a", SKIP));

        assertTrue(a.isAlreadyDeath());
        assertEquals(0, a.getHandSize(), "死了不摸牌");
        // 輪到下一位，下一位只拿到回合開始的摸牌，不會額外拿到 3 張擊殺獎勵
        assertNotEquals("player-a", game.getCurrentRound().getCurrentRoundPlayer().getId());
        assertEquals(cHand, game.getPlayer("player-c").getHandSize());
        assertEquals(dHand, game.getPlayer("player-d").getHandSize());
        assertTrue(game.getPlayer("player-b").getHandSize() - bHand <= 2, "player-b 只有回合開始的摸牌");
    }
}

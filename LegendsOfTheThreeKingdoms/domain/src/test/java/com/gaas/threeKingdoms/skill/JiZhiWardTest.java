package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.DrawCardEvent;
import com.gaas.threeKingdoms.events.WaitForWardEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.SomethingForNothing;
import com.gaas.threeKingdoms.handcard.scrollcard.Ward;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 集智 × 無懈可擊：無懈可擊是非延時錦囊，黃月英每使用一張無懈可擊即摸一張。
 */
public class JiZhiWardTest extends PassiveSkillTestBase {

    private static final String ACTIVE = PlayType.ACTIVE.getPlayType();
    private static final String SKIP = PlayType.SKIP.getPlayType();

    /** 牌堆頂放幾張殺，避免集智摸到無懈可擊而改變反制名單。 */
    private void stackKillsOnTop(Game game) {
        game.getDeck().putBackOnTop(List.of(
                new Kill(BS8008), new Kill(BS9009), new Kill(BS0010), new Kill(BS7020), new Kill(BS8021)));
    }

    private long drawEventsFor(List<DomainEvent> events, String playerId) {
        return events.stream()
                .filter(e -> e instanceof DrawCardEvent d && playerId.equals(d.getDrawCardPlayerId()))
                .count();
    }

    @DisplayName("黃月英出無懈可擊抵銷他人的無中生有 → 集智摸一張；鏈結束後回到回合玩家")
    @Test
    public void huangYueYingDrawsOneWhenPlayingWard() {
        Game game = createGame(General.甘寧, General.黃月英, General.甘寧, General.甘寧);
        stackKillsOnTop(game);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new SomethingForNothing(SH7046));
        b.getHand().addCardToHand(new Ward(SSJ011));

        game.playerPlayCard("player-a", SH7046.getCardId(), "player-a", ACTIVE);
        List<DomainEvent> events = game.playWardCard("player-b", SSJ011.getCardId(), ACTIVE);

        assertEquals(1, b.getHandSize(), "出無懈後 0 + 集智 1 = 1");
        assertEquals(1, drawEventsFor(events, "player-b"));
        assertFalse(b.getHand().getCards().stream().anyMatch(c -> c.getId().equals(SSJ011.getCardId())));
        assertEquals(0, a.getHandSize(), "無中生有被抵銷，A 不摸牌");
        assertTrue(game.getTopBehavior().isEmpty());
        assertEquals(Stage.Normal, game.getCurrentRound().getStage());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("黃月英反制別人的無懈可擊 → 集智摸一張；偶數無懈，自己的無中生有照常生效")
    @Test
    public void huangYueYingDrawsOneWhenCounterWarding() {
        Game game = createGame(General.黃月英, General.甘寧, General.甘寧, General.甘寧);
        stackKillsOnTop(game);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(List.of(new SomethingForNothing(SH7046), new Ward(SCQ077)));
        b.getHand().addCardToHand(new Ward(SSJ011));

        game.playerPlayCard("player-a", SH7046.getCardId(), "player-a", ACTIVE);
        assertEquals(2, a.getHandSize(), "出無中生有 → 剩無懈 1 + 集智 1");

        List<DomainEvent> bWard = game.playWardCard("player-b", SSJ011.getCardId(), ACTIVE);
        WaitForWardEvent wait = (WaitForWardEvent) bWard.stream()
                .filter(WaitForWardEvent.class::isInstance).findFirst().orElseThrow();
        assertTrue(wait.getPlayerIds().contains("player-a"), "A 可反制 B 的無懈");
        assertEquals(0, drawEventsFor(bWard, "player-b"), "非黃月英出無懈不摸");
        assertEquals(0, b.getHandSize());

        List<DomainEvent> aWard = game.playWardCard("player-a", SCQ077.getCardId(), ACTIVE);

        // 2 - 無懈 1 + 集智 1 + 無中生有 2 = 4
        assertEquals(4, a.getHandSize());
        assertEquals(2, drawEventsFor(aWard, "player-a"), "集智一次 + 無中生有一次");
        assertTrue(game.getTopBehavior().isEmpty());
        assertEquals(Stage.Normal, game.getCurrentRound().getStage());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("反制鏈中黃月英出兩張無懈 → 集智各觸發一次（共摸兩張）")
    @Test
    public void huangYueYingDrawsOncePerWardInChain() {
        Game game = createGame(General.甘寧, General.黃月英, General.甘寧, General.甘寧);
        stackKillsOnTop(game);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        Player c = game.getPlayer("player-c");
        a.getHand().addCardToHand(new SomethingForNothing(SH7046));
        b.getHand().addCardToHand(List.of(new Ward(SSJ011), new Ward(SCQ077)));
        c.getHand().addCardToHand(new Ward(SCK078));

        game.playerPlayCard("player-a", SH7046.getCardId(), "player-a", ACTIVE);
        game.playWardCard("player-b", SSJ011.getCardId(), ACTIVE);
        assertEquals(2, b.getHandSize(), "2 - 1 + 集智 1");
        assertEquals(Stage.Wait_Accept_Ward_Effect, game.getCurrentRound().getStage());

        game.playWardCard("player-c", SCK078.getCardId(), ACTIVE);
        assertEquals(2, b.getHandSize(), "C 出無懈，B 不摸");

        List<DomainEvent> events = game.playWardCard("player-b", SCQ077.getCardId(), ACTIVE);
        assertEquals(1, drawEventsFor(events, "player-b"));
        assertEquals(2, b.getHandSize(), "2 - 1 + 集智 1");
        assertTrue(b.getHand().getCards().stream().noneMatch(Ward.class::isInstance));

        // 三張無懈（奇數）→ 無中生有被抵銷
        assertEquals(0, a.getHandSize());
        assertTrue(game.getTopBehavior().isEmpty());
        assertEquals(Stage.Normal, game.getCurrentRound().getStage());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("黃月英放棄出無懈（skip）→ 不摸牌")
    @Test
    public void huangYueYingSkipWardDoesNotDraw() {
        Game game = createGame(General.甘寧, General.黃月英, General.甘寧, General.甘寧);
        stackKillsOnTop(game);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new SomethingForNothing(SH7046));
        b.getHand().addCardToHand(new Ward(SSJ011));

        game.playerPlayCard("player-a", SH7046.getCardId(), "player-a", ACTIVE);
        game.playWardCard("player-b", "", SKIP);

        assertEquals(1, b.getHandSize());
        assertEquals(2, a.getHandSize(), "無中生有生效");
    }
}

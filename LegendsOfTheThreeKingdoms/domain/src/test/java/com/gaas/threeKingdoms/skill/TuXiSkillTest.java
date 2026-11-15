package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.RoundPhase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 張遼突襲（身份標準版）：摸牌階段，你可以放棄摸牌，改為獲得最多兩名其他角色的各一張手牌。
 * 使用者回報：突襲在摸牌階段沒有事件請前端選擇是否發動（原本做成出牌階段主動技、摸牌照摸）。
 * 情境：player-a（甘寧）結束回合 → 輪到 player-b（張遼）。
 */
public class TuXiSkillTest extends PassiveSkillTestBase {

    private Game givenZhangLiaoTurnNext() {
        Game game = createGame(General.甘寧, General.張遼, General.甘寧, General.甘寧);
        game.getPlayer("player-c").getHand().addCardToHand(List.of(new Kill(BS8008)));
        game.getPlayer("player-d").getHand().addCardToHand(List.of(new Dodge(BH2028)));
        game.getDeck().add(List.of(new Peach(BH4030), new Peach(BH7033)));
        return game;
    }

    private static AskSkillEffectEvent tuXiAsk(List<DomainEvent> events) {
        return events.stream().filter(e -> e instanceof AskSkillEffectEvent a && "突襲".equals(a.getSkillName()))
                .map(e -> (AskSkillEffectEvent) e).findFirst().orElseThrow(() -> new AssertionError("沒有突襲詢問：" + events));
    }

    @DisplayName("輪到張遼 → 摸牌前詢問是否發動突襲（還沒摸牌）")
    @Test
    public void asksBeforeDrawing() {
        Game game = givenZhangLiaoTurnNext();

        List<DomainEvent> events = game.finishAction("player-a");

        AskSkillEffectEvent ask = tuXiAsk(events);
        assertEquals("player-b", ask.getPlayerId());
        assertEquals(List.of("ACCEPT", "SKIP"), ask.getOptions());
        assertEquals("突襲：張遼 要發動突襲嗎？（放棄摸牌，改為獲得至多兩名其他角色的各一張手牌）", ask.getMessage());
        assertEquals(0, game.getPlayer("player-b").getHandSize(), "詢問時還沒摸牌");
        assertInstanceOf(WaitingSkillEffectBehavior.class, game.peekTopBehavior());
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("發動：拿兩名角色各一張手牌，不摸牌，進入出牌階段")
    @Test
    public void acceptTakesCardsInsteadOfDrawing() {
        Game game = givenZhangLiaoTurnNext();
        game.finishAction("player-a");
        int deckBefore = game.getDeck().size();

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "突襲", "ACCEPT",
                List.of("player-c", "player-d"), null);

        Player b = game.getPlayer("player-b");
        assertEquals(2, b.getHandSize());
        assertTrue(b.getHand().getCard(BS8008.getCardId()).isPresent());
        assertTrue(b.getHand().getCard(BH2028.getCardId()).isPresent());
        assertEquals(0, game.getPlayer("player-c").getHandSize());
        assertEquals(0, game.getPlayer("player-d").getHandSize());
        assertEquals(deckBefore, game.getDeck().size(), "發動就不從牌堆摸牌");
        assertEquals(RoundPhase.Action, game.getCurrentRound().getRoundPhase());
        assertTrue(game.isTopBehaviorEmpty());
        SkillEffectEvent effect = events.stream().filter(e -> e instanceof SkillEffectEvent se && "突襲".equals(se.getSkillName()))
                .map(e -> (SkillEffectEvent) e).findFirst().orElseThrow();
        assertTrue(effect.isAccepted());
        assertEquals("突襲：張遼 放棄摸牌，獲得 甘寧、甘寧 各一張手牌", effect.getMessage());
    }

    @DisplayName("發動時可以只選一名角色")
    @Test
    public void acceptWithSingleTarget() {
        Game game = givenZhangLiaoTurnNext();
        game.finishAction("player-a");

        game.playerUseSkillEffect("player-b", "突襲", "ACCEPT", List.of("player-c"), null);

        assertEquals(1, game.getPlayer("player-b").getHandSize());
        assertEquals(RoundPhase.Action, game.getCurrentRound().getRoundPhase());
    }

    @DisplayName("不發動：照常摸 2 張")
    @Test
    public void skipDrawsNormally() {
        Game game = givenZhangLiaoTurnNext();
        game.finishAction("player-a");

        game.playerUseSkillEffect("player-b", "突襲", "SKIP", null, null);

        assertEquals(2, game.getPlayer("player-b").getHandSize());
        assertEquals(1, game.getPlayer("player-c").getHandSize(), "沒被拿牌");
        assertEquals(RoundPhase.Action, game.getCurrentRound().getRoundPhase());
    }

    @DisplayName("其他角色都沒有手牌 → 不能發動，不詢問，直接摸牌")
    @Test
    public void noAskWhenNobodyHasHandCards() {
        Game game = createGame(General.甘寧, General.張遼, General.甘寧, General.甘寧);
        game.getDeck().add(List.of(new Peach(BH4030), new Peach(BH7033)));

        List<DomainEvent> events = game.finishAction("player-a");

        assertTrue(events.stream().noneMatch(e -> e instanceof AskSkillEffectEvent));
        assertEquals(2, game.getPlayer("player-b").getHandSize());
    }

    @DisplayName("不合法的目標（自己／同一人兩次／超過兩人／沒手牌）→ 400，仍可重新選擇")
    @Test
    public void invalidTargetsRejectedAndCanRetry() {
        Game game = givenZhangLiaoTurnNext();
        game.finishAction("player-a");

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseSkillEffect("player-b", "突襲", "ACCEPT", List.of("player-b"), null));
        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseSkillEffect("player-b", "突襲", "ACCEPT", List.of("player-c", "player-c"), null));
        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseSkillEffect("player-b", "突襲", "ACCEPT", List.of("player-a", "player-c", "player-d"), null));
        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseSkillEffect("player-b", "突襲", "ACCEPT", List.of("player-a"), null), "甘寧 a 沒手牌");

        assertInstanceOf(WaitingSkillEffectBehavior.class, game.peekTopBehavior(), "詢問還在");
        game.playerUseSkillEffect("player-b", "突襲", "ACCEPT", List.of("player-c"), null);
        assertEquals(1, game.getPlayer("player-b").getHandSize());
    }

    @DisplayName("樂不思蜀生效時仍可發動突襲，之後跳過出牌階段")
    @Test
    public void tuXiStillAvailableUnderContentment() {
        Game game = givenZhangLiaoTurnNext();
        game.getPlayer("player-b").addDelayScrollCard(new Contentment(SH6045));
        // Deck 是 Stack：最後一張最先抽 → 判定牌黑桃9（非紅心 → 樂不思蜀生效）
        game.getDeck().add(List.of(new Kill(BS9009)));
        game.finishAction("player-a");

        game.playerUseSkillEffect("player-b", "突襲", "ACCEPT", List.of("player-c", "player-d"), null);

        assertEquals(2, game.getPlayer("player-b").getHandSize());
        assertNotEquals("player-b", game.getCurrentRound().getCurrentRoundPlayer().getId(), "跳過出牌階段，回合結束");
    }
}

package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.WaitingSkillEffectBehavior;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.player.HealthStatus;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.RoundPhase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 觀星（諸葛亮）：回合開始（準備階段，判定之前）詢問，ARRANGE / SKIP 後接續判定 → 摸牌。
 */
public class GuanXingSkillTest extends PassiveSkillTestBase {

    /** 堆頂抽序 = BS8008, BS9009, BH3029, BH4030（Stack：後 add 先抽）。 */
    private void stackDeckTop(Game game) {
        game.getDeck().add(List.of(new Peach(BH4030), new Peach(BH3029), new Kill(BS9009), new Kill(BS8008)));
    }

    private AskSkillEffectEvent findGuanXingAsk(List<DomainEvent> events) {
        return events.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(e -> (AskSkillEffectEvent) e)
                .filter(e -> "觀星".equals(e.getSkillName()))
                .findFirst().orElse(null);
    }

    private boolean handHas(Player p, String cardId) {
        return p.getHand().getCards().stream().anyMatch(c -> c.getId().equals(cardId));
    }

    @DisplayName("諸葛亮回合開始先詢問觀星（判定、摸牌之前暫停），dataCardIds = 堆頂 4 張（存活數），options = ARRANGE/SKIP")
    @Test
    public void asksGuanXingAtTurnStartBeforeJudgementAndDraw() {
        Game game = createGame(General.諸葛亮, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        stackDeckTop(game);

        List<DomainEvent> events = game.playerTakeTurn(a);

        AskSkillEffectEvent ask = findGuanXingAsk(events);
        assertNotNull(ask, "回合開始應詢問觀星");
        assertEquals("player-a", ask.getPlayerId());
        assertEquals(List.of(BS8008.getCardId(), BS9009.getCardId(), BH3029.getCardId(), BH4030.getCardId()),
                ask.getDataCardIds());
        assertEquals(List.of("ARRANGE", "SKIP"), ask.getOptions());
        assertEquals(0, a.getHandSize(), "詢問中尚未摸牌");
        assertEquals(RoundPhase.Judgement, game.getCurrentRoundPhase());
        assertTrue(game.peekTopBehavior() instanceof WaitingSkillEffectBehavior w && w.getSkillName().equals("觀星"));
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("觀星 ARRANGE：重排後接續摸牌，摸到的是重排後的堆頂；未列出的置底")
    @Test
    public void arrangeThenDrawFromArrangedTop() {
        Game game = createGame(General.諸葛亮, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        stackDeckTop(game);
        game.playerTakeTurn(a);

        // BH4030, BH3029 回堆頂（BH4030 最上），BS8008, BS9009 置底
        game.playerUseSkillEffect("player-a", "觀星", "ARRANGE",
                List.of(BH4030.getCardId(), BH3029.getCardId()), null);

        assertEquals(2, a.getHandSize());
        assertTrue(handHas(a, BH4030.getCardId()) && handHas(a, BH3029.getCardId()), "摸到重排後的堆頂兩張");
        assertEquals(RoundPhase.Action, game.getCurrentRoundPhase());
        assertTrue(game.isTopBehaviorEmpty());
        List<String> bottomTwo = game.getDeck().getCardDeck().subList(0, 2).stream().map(HandCard::getId).toList();
        assertTrue(bottomTwo.containsAll(List.of(BS8008.getCardId(), BS9009.getCardId())), "未選的牌置牌堆底");
    }

    @DisplayName("觀星 SKIP：牌堆不變，照原順序摸 2 進出牌階段")
    @Test
    public void skipKeepsDeckAndDraws() {
        Game game = createGame(General.諸葛亮, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        stackDeckTop(game);
        game.playerTakeTurn(a);

        game.playerUseSkillEffect("player-a", "觀星", "SKIP", null, null);

        assertTrue(handHas(a, BS8008.getCardId()) && handHas(a, BS9009.getCardId()));
        assertEquals(2, a.getHandSize());
        assertEquals(RoundPhase.Action, game.getCurrentRoundPhase());
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("觀星在判定之前：把紅桃排到堆頂 → 樂不思蜀判定不生效，可正常出牌")
    @Test
    public void arrangeAffectsContentmentJudgement() {
        Game game = createGame(General.諸葛亮, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.addDelayScrollCard(new Contentment(SH6045));
        stackDeckTop(game);

        List<DomainEvent> askEvents = game.playerTakeTurn(a);
        assertNotNull(findGuanXingAsk(askEvents));
        assertTrue(a.hasThisDelayScrollCard(SH6045.getCardId()), "觀星詢問時尚未判定樂不思蜀");

        // 原本判定牌 = BS8008（黑桃 → 樂不思蜀生效）；改排 BH3029 最上 → 紅桃不生效
        game.playerUseSkillEffect("player-a", "觀星", "ARRANGE",
                List.of(BH3029.getCardId(), BS8008.getCardId(), BS9009.getCardId()), null);

        assertFalse(a.hasThisDelayScrollCard(SH6045.getCardId()));
        assertTrue(game.getGraveyard().contains(BH3029.getCardId()), "紅桃為判定牌");
        assertTrue(handHas(a, BS8008.getCardId()) && handHas(a, BS9009.getCardId()), "判定後摸 2");
        assertEquals(RoundPhase.Action, game.getCurrentRoundPhase(), "樂不思蜀未生效 → 進出牌階段");
    }

    @DisplayName("觀星張數 = 存活角色數：一人陣亡 → 只看 3 張")
    @Test
    public void peekCountEqualsAlivePlayers() {
        Game game = createGame(General.諸葛亮, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        game.getPlayer("player-d").setHealthStatus(HealthStatus.DEATH);
        stackDeckTop(game);

        AskSkillEffectEvent ask = findGuanXingAsk(game.playerTakeTurn(a));

        assertNotNull(ask);
        assertEquals(List.of(BS8008.getCardId(), BS9009.getCardId(), BH3029.getCardId()), ask.getDataCardIds());
    }

    @DisplayName("非諸葛亮回合開始不詢問觀星，直接摸牌")
    @Test
    public void nonZhugeLiangNoAsk() {
        Game game = createGame(General.甘寧, General.諸葛亮, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        stackDeckTop(game);

        List<DomainEvent> events = game.playerTakeTurn(a);

        assertNull(findGuanXingAsk(events));
        assertEquals(2, a.getHandSize());
        assertEquals(RoundPhase.Action, game.getCurrentRoundPhase());
    }

    @DisplayName("觀星不再能於出牌階段主動發動")
    @Test
    public void cannotActivateInActionPhase() {
        Game game = createGame(General.諸葛亮, General.甘寧, General.甘寧, General.甘寧);
        game.getCurrentRound().setRoundPhase(RoundPhase.Action);

        assertThrows(IllegalStateException.class, () ->
                game.playerUseSkillEffect("player-a", "觀星", "ACCEPT", null, null));
    }

    @DisplayName("觀星 choice 非 ARRANGE/SKIP → 拋例外")
    @Test
    public void invalidChoiceThrows() {
        Game game = createGame(General.諸葛亮, General.甘寧, General.甘寧, General.甘寧);
        stackDeckTop(game);
        game.playerTakeTurn(game.getPlayer("player-a"));

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseSkillEffect("player-a", "觀星", "ACCEPT", null, null));
    }
}

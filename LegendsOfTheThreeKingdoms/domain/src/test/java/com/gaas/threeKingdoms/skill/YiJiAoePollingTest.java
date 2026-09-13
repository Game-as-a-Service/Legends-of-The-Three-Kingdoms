package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 遺計 × AOE polling（南蠻入侵 / 萬箭齊發）— 官方「受到傷害後」含 AOE 傷害（issue #250）。
 * 先前 YiJiSkill 在 polling caller 上直接 return（刻意留的 v1 缺口），本測試驗證：
 * 受傷 → 遺計詢問（polling-advance defer）→ ACCEPT / GIVE / SKIP 結算後
 * 收鏈 resume 輪詢，回到原本的 AOE 詢問輪（不跳人、不卡住）。
 *
 * 對照剛烈的同型修正（issue #233 / GangLieAoePollingTest）；遺計為單段詢問，
 * 但插入的 WaitingSkillEffectBehavior 一樣必須帶 PARAM_RESUME_POLLING。
 */
public class YiJiAoePollingTest extends PassiveSkillTestBase {

    private List<String> askKillTargets(List<DomainEvent> events) {
        return events.stream().filter(e -> e instanceof AskKillEvent)
                .map(e -> ((AskKillEvent) e).getPlayerId()).toList();
    }

    private List<String> askDodgeTargets(List<DomainEvent> events) {
        return events.stream().filter(e -> e instanceof AskDodgeEvent)
                .map(e -> ((AskDodgeEvent) e).getPlayerId()).toList();
    }

    private boolean hasSkillAsk(List<DomainEvent> events, String skillName, String playerId) {
        return events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent ask
                && ask.getSkillName().equals(skillName) && ask.getPlayerId().equals(playerId));
    }

    @DisplayName("南蠻：b=郭嘉 skip 受傷 → 遺計 ACCEPT 摸兩張 → resume 輪詢問 c")
    @Test
    public void yiJiAcceptInBarbarianInvasion_thenNextAskedIsC() {
        Game game = createGame(General.甘寧, General.郭嘉, General.甘寧, General.甘寧);
        Player b = game.getPlayer("player-b");
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));
        game.setDeck(new Deck(List.of(new Peach(BH3029), new Dismantle(SS3003))));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        List<DomainEvent> e2 = game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());

        assertTrue(hasSkillAsk(e2, "遺計", "player-b"), "AOE 詢問輪中受傷後應詢問遺計");
        assertTrue(askKillTargets(e2).isEmpty(), "遺計詢問中，不應先問下一個 reactor");
        assertEquals(3, b.getHP());
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());

        List<DomainEvent> e3 = game.playerUseSkillEffect("player-b", "遺計", "ACCEPT", null, null);

        assertEquals(2, b.getHandSize(), "遺計摸兩張");
        assertEquals(List.of("player-c"), askKillTargets(e3),
                "遺計結算後 resume 輪詢問 c — 跳到 d 或卡住即為 bug");
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());

        List<DomainEvent> e4 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e4), "c skip 後問 d");
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty(), "輪詢結束、stack 清空");
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("南蠻：遺計 SKIP → 直接 resume 輪詢問 c")
    @Test
    public void yiJiSkipInBarbarianInvasion_thenNextAskedIsC() {
        Game game = createGame(General.甘寧, General.郭嘉, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        List<DomainEvent> e3 = game.playerUseSkillEffect("player-b", "遺計", "SKIP", null, null);

        assertEquals(0, game.getPlayer("player-b").getHandSize(), "放棄遺計不摸牌");
        assertEquals(List.of("player-c"), askKillTargets(e3), "放棄遺計後問 c");
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());

        List<DomainEvent> e4 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e4));
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("南蠻：遺計 GIVE 令尚未被問的 d 獲得一張 → resume 輪詢仍問 c")
    @Test
    public void yiJiGiveInBarbarianInvasion_thenNextAskedIsStillC() {
        Game game = createGame(General.甘寧, General.郭嘉, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));
        game.setDeck(new Deck(List.of(new Peach(BH3029))));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        List<DomainEvent> e3 = game.playerUseSkillEffect("player-b", "遺計", "GIVE", null, "player-d");

        assertEquals(0, game.getPlayer("player-b").getHandSize(), "GIVE 時郭嘉自己不摸牌");
        assertEquals(1, game.getPlayer("player-d").getHandSize(), "d 從牌堆獲得一張");
        assertEquals(List.of("player-c"), askKillTargets(e3), "GIVE 結算後仍照座位順序問 c");

        List<DomainEvent> e4 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e4));
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("萬箭：b=郭嘉 skip 受傷 → 遺計 ACCEPT → resume 輪詢問 c 出閃")
    @Test
    public void yiJiAcceptInArrowBarrage_thenNextAskedIsC() {
        Game game = createGame(General.甘寧, General.郭嘉, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new ArrowBarrage(SHA040));
        game.setDeck(new Deck(List.of(new Peach(BH3029), new Dodge(BH2028))));

        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", "active");
        List<DomainEvent> e2 = game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());

        assertTrue(hasSkillAsk(e2, "遺計", "player-b"), "萬箭詢問輪中受傷後應詢問遺計");
        assertTrue(askDodgeTargets(e2).isEmpty(), "遺計詢問中，不應先問下一個 reactor");

        List<DomainEvent> e3 = game.playerUseSkillEffect("player-b", "遺計", "ACCEPT", null, null);

        assertEquals(2, game.getPlayer("player-b").getHandSize());
        assertEquals(List.of("player-c"), askDodgeTargets(e3), "遺計結算後 resume 問 c 出閃");

        List<DomainEvent> e4 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askDodgeTargets(e4));
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("南蠻：最後一個 reactor d=郭嘉 → 遺計 ACCEPT → 輪詢乾淨收尾、activePlayer 回出牌者")
    @Test
    public void yiJiOnLastReactor_pollingEndsCleanly() {
        Game game = createGame(General.甘寧, General.甘寧, General.甘寧, General.郭嘉);
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));
        game.setDeck(new Deck(List.of(new Peach(BH3029), new Dismantle(SS3003))));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        List<DomainEvent> e4 = game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());

        assertTrue(hasSkillAsk(e4, "遺計", "player-d"), "最後一個 reactor 受傷也應詢問遺計");
        assertFalse(game.getTopBehavior().isEmpty(), "遺計詢問中，南蠻 behavior 需保留");

        game.playerUseSkillEffect("player-d", "遺計", "ACCEPT", null, null);

        assertEquals(2, game.getPlayer("player-d").getHandSize());
        assertTrue(game.getTopBehavior().isEmpty(), "遺計結算後輪詢結束、stack 清空");
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId(),
                "輪詢結束 activePlayer 回到出牌者");
    }

    @DisplayName("南蠻：b、c 都是郭嘉 → 兩次遺計詢問各自收鏈 resume，輪詢不跳人")
    @Test
    public void twoGuoJiaInSamePolling_bothResumeCorrectly() {
        Game game = createGame(General.甘寧, General.郭嘉, General.郭嘉, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));
        game.setDeck(new Deck(List.of(
                new Peach(BH3029), new Dismantle(SS3003), new Dodge(BH2028), new Peach(BH4030))));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        List<DomainEvent> e3 = game.playerUseSkillEffect("player-b", "遺計", "ACCEPT", null, null);
        assertEquals(List.of("player-c"), askKillTargets(e3), "b 的遺計收鏈後問 c");

        List<DomainEvent> e4 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(hasSkillAsk(e4, "遺計", "player-c"), "c 受傷後也應詢問遺計");
        assertTrue(askKillTargets(e4).isEmpty(), "c 的遺計詢問中，不應先問 d");

        List<DomainEvent> e5 = game.playerUseSkillEffect("player-c", "遺計", "SKIP", null, null);
        assertEquals(List.of("player-d"), askKillTargets(e5), "c 的遺計收鏈後問 d");

        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
        assertEquals(3, game.getPlayer("player-b").getHP());
        assertEquals(3, game.getPlayer("player-c").getHP());
    }

    @DisplayName("南蠻：郭嘉被 AOE 打到瀕死 → 不詢問遺計（維持既有規則），輪詢照常")
    @Test
    public void yiJiNotTriggeredWhenDyingInPolling() {
        Game game = createGame(General.甘寧, General.郭嘉, General.甘寧, General.甘寧);
        Player b = game.getPlayer("player-b");
        b.damage(3); // HP 4 → 1，南蠻再扣 1 即瀕死
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        List<DomainEvent> e2 = game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());

        assertEquals(0, b.getHP());
        assertFalse(hasSkillAsk(e2, "遺計", "player-b"), "瀕死不觸發遺計");
    }
}

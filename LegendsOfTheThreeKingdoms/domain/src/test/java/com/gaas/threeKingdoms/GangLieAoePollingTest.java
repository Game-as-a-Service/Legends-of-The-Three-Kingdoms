package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.Deck;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 剛烈 × AOE polling（南蠻入侵 / 萬箭齊發）— wiki「當你受到傷害後」應含 AOE 傷害（issue #165）。
 * 先前 GangLieSkill 在 polling caller 上直接跳過（Batch 12 註記的同型缺口）；本測試驗證：
 * 受傷 → 剛烈詢問（polling-advance defer）→ 兩段式詢問鏈（判定 → 問來源）
 * 甚至鬼才巢狀介入後，收鏈 resume 輪詢（不跳人、不卡住）。
 */
public class GangLieAoePollingTest {

    private Game createGame(General gA, General gB, General gC, General gD) {
        Game game = new Game();
        game.initDeck();
        Player a = build("player-a", gA, Role.MONARCH);
        Player b = build("player-b", gB, Role.MINISTER);
        Player c = build("player-c", gC, Role.REBEL);
        Player d = build("player-d", gD, Role.TRAITOR);
        game.setPlayers(asList(a, b, c, d));
        game.setCurrentRound(new Round(a));
        game.enterPhase(new Normal(game));
        return game;
    }

    private Player build(String id, General general, Role role) {
        return PlayerBuilder.construct().withId(id)
                .withBloodCard(new BloodCard(4))
                .withGeneralCard(new GeneralCard(general))
                .withHealthStatus(HealthStatus.ALIVE)
                .withRoleCard(new RoleCard(role))
                .withHand(new Hand()).withEquipment(new Equipment()).build();
    }

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

    @DisplayName("南蠻：b=夏侯惇 skip 受傷 → 剛烈 ACCEPT 判定黑桃生效 → 來源 a 棄兩張 → resume 問 c")
    @Test
    public void gangLieDiscardInBarbarianInvasion_thenNextAskedIsC() {
        Game game = createGame(General.甘寧, General.夏侯惇, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(List.of(new BarbarianInvasion(SS7007), new Peach(BH3029), new Kill(BS8008)));
        game.setDeck(new Deck(List.of(new Dismantle(SS3003)))); // 判定牌：黑桃 3 → 剛烈生效

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        List<DomainEvent> e2 = game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());

        assertTrue(hasSkillAsk(e2, "剛烈", "player-b"), "受傷後應詢問剛烈");
        assertTrue(askKillTargets(e2).isEmpty(), "剛烈詢問中，不應先問下一個 reactor");
        assertEquals(3, game.getPlayer("player-b").getHP());

        List<DomainEvent> e3 = game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);
        assertTrue(hasSkillAsk(e3, "剛烈", "player-a"), "判定生效後應問來源選擇");
        assertTrue(askKillTargets(e3).isEmpty(), "來源選擇中，仍不應問下一個 reactor");
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());

        List<DomainEvent> e4 = game.playerUseSkillEffect("player-a", "剛烈", "DISCARD",
                List.of(BH3029.getCardId(), BS8008.getCardId()), null);

        assertEquals(0, a.getHandSize(), "來源棄兩張手牌");
        assertTrue(game.getGraveyard().contains(BH3029.getCardId()));
        assertEquals(List.of("player-c"), askKillTargets(e4), "剛烈鏈收斂後 resume 輪詢問 c — 跳到 d 或卡住即為 bug");
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());

        List<DomainEvent> e5 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e5), "c skip 後問 d");
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("南蠻：剛烈 SKIP → 直接 resume 輪詢問 c")
    @Test
    public void gangLieSkipInBarbarianInvasion_thenNextAskedIsC() {
        Game game = createGame(General.甘寧, General.夏侯惇, General.孫權, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new BarbarianInvasion(SS7007)));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        List<DomainEvent> e3 = game.playerUseSkillEffect("player-b", "剛烈", "SKIP", null, null);

        assertEquals(List.of("player-c"), askKillTargets(e3), "放棄剛烈後問 c");
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());

        List<DomainEvent> e4 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e4));
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("萬箭：剛烈 ACCEPT 判定紅桃 → 未生效（不問來源）→ resume 問 c 出閃")
    @Test
    public void gangLieHeartJudgementInArrowBarrage_thenNextAskedIsC() {
        Game game = createGame(General.甘寧, General.夏侯惇, General.孫權, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(new ArrowBarrage(SHA040));
        game.setDeck(new Deck(List.of(new Peach(BH3029)))); // 判定牌：紅心 3 → 剛烈不生效

        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", "active");
        List<DomainEvent> e2 = game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());

        assertTrue(hasSkillAsk(e2, "剛烈", "player-b"), "受傷後應詢問剛烈");
        assertTrue(askDodgeTargets(e2).isEmpty(), "剛烈詢問中，不應先問下一個 reactor");

        List<DomainEvent> e3 = game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        assertFalse(hasSkillAsk(e3, "剛烈", "player-a"), "判定紅桃未生效，不應問來源");
        assertEquals(4, game.getPlayer("player-a").getHP(), "來源不受傷");
        assertEquals(List.of("player-c"), askDodgeTargets(e3), "判定未生效直接 resume 問 c 出閃");

        List<DomainEvent> e4 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askDodgeTargets(e4));
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("南蠻：最後一個 reactor d=夏侯惇 → 剛烈 ACCEPT + 來源選 DAMAGE → 輪詢收尾、a 扣 1 血")
    @Test
    public void gangLieDamageOnLastReactor_pollingEndsCleanly() {
        Game game = createGame(General.甘寧, General.孫權, General.孫權, General.夏侯惇);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(List.of(new BarbarianInvasion(SS7007)));
        game.setDeck(new Deck(List.of(new Dismantle(SS3003)))); // 黑桃 → 生效

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        List<DomainEvent> e4 = game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());

        assertTrue(hasSkillAsk(e4, "剛烈", "player-d"), "最後一個 reactor 受傷也應詢問剛烈");
        assertFalse(game.getTopBehavior().isEmpty(), "剛烈詢問中");

        game.playerUseSkillEffect("player-d", "剛烈", "ACCEPT", null, null);
        game.playerUseSkillEffect("player-a", "剛烈", "DAMAGE", null, null);

        assertEquals(3, a.getHP(), "來源受剛烈 1 點傷害");
        assertTrue(game.getTopBehavior().isEmpty(), "剛烈鏈收斂後輪詢結束、stack 清空");
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId(),
                "輪詢結束 activePlayer 回到出牌者");
    }

    @DisplayName("南蠻 + 鬼才：司馬懿換紅桃判定牌 → 剛烈未生效 → resume 問 c（三層巢狀收鏈）")
    @Test
    public void gangLieWithGuiCaiReplacementToHeart_thenNextAskedIsC() {
        Game game = createGame(General.甘寧, General.夏侯惇, General.司馬懿, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new BarbarianInvasion(SS7007)));
        game.getPlayer("player-c").getHand().addCardToHand(List.of(new Peach(BH3029))); // 司馬懿手牌（紅心）
        game.setDeck(new Deck(List.of(new Dismantle(SS3003)))); // 原判定黑桃（若無鬼才會生效）

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        List<DomainEvent> e3 = game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        assertTrue(hasSkillAsk(e3, "鬼才", "player-c"), "判定牌抽出後應詢問司馬懿鬼才");
        assertTrue(askKillTargets(e3).isEmpty(), "鬼才詢問中，不應先問下一個 reactor");

        List<DomainEvent> e4 = game.playerUseSkillEffect("player-c", "鬼才", "ACCEPT",
                List.of(BH3029.getCardId()), null);

        assertFalse(hasSkillAsk(e4, "剛烈", "player-a"), "換成紅桃 → 剛烈未生效，不問來源");
        assertEquals(4, game.getPlayer("player-a").getHP());
        assertEquals(List.of("player-c"), askKillTargets(e4), "鬼才巢狀收鏈後 resume 問 c");

        List<DomainEvent> e5 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e5));
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("南蠻 + 鬼才 SKIP：原判定黑桃生效 → 來源棄兩張 → resume 問 c（鬼才不換牌路徑）")
    @Test
    public void gangLieWithGuiCaiSkip_thenSourceDiscardsAndPollingResumes() {
        Game game = createGame(General.甘寧, General.夏侯惇, General.司馬懿, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(List.of(new BarbarianInvasion(SS7007), new Peach(BH3029), new Kill(BS8008)));
        game.getPlayer("player-c").getHand().addCardToHand(List.of(new Peach(BH4030))); // 司馬懿手牌
        game.setDeck(new Deck(List.of(new Dismantle(SS3003)))); // 黑桃 → 生效

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        List<DomainEvent> e3 = game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);
        assertTrue(hasSkillAsk(e3, "鬼才", "player-c"));

        List<DomainEvent> e4 = game.playerUseSkillEffect("player-c", "鬼才", "SKIP", null, null);
        assertTrue(hasSkillAsk(e4, "剛烈", "player-a"), "原判定黑桃生效 → 問來源選擇");
        assertTrue(askKillTargets(e4).isEmpty(), "來源選擇中，不應先問下一個 reactor");

        List<DomainEvent> e5 = game.playerUseSkillEffect("player-a", "剛烈", "DISCARD",
                List.of(BH3029.getCardId(), BS8008.getCardId()), null);
        assertEquals(0, a.getHandSize());
        assertEquals(List.of("player-c"), askKillTargets(e5), "三層鏈全部收斂後 resume 問 c");

        List<DomainEvent> e6 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e6));
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
    }
}

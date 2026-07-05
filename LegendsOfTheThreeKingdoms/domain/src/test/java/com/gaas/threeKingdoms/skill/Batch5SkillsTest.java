package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Batch 5：激將（劉備主公技）/ 救援（孫權主公技）/ 流離（大喬）。
 */
public class Batch5SkillsTest extends PassiveSkillTestBase {

    // ===== 激將 =====

    @DisplayName("南蠻入侵問主公劉備出殺 → 激將改問蜀將；關羽 ACCEPT 交殺替劉備擋下")
    @Test
    public void jiJiangShuGeneralKillsForLiuBei() {
        // 座位：a 攻擊者（孫權），b = 劉備主公，c = 關羽（蜀），d = 孫權
        Game game = new Game();
        game.initDeck();
        Player a = buildPlayer("player-a", General.孫權, com.gaas.threeKingdoms.rolecard.Role.TRAITOR);
        Player b = buildPlayer("player-b", General.劉備, com.gaas.threeKingdoms.rolecard.Role.MONARCH);
        Player c = buildPlayer("player-c", General.關羽, com.gaas.threeKingdoms.rolecard.Role.MINISTER);
        Player d = buildPlayer("player-d", General.孫權, com.gaas.threeKingdoms.rolecard.Role.REBEL);
        game.setPlayers(java.util.Arrays.asList(a, b, c, d));
        game.setCurrentRound(new com.gaas.threeKingdoms.round.Round(a));
        game.enterPhase(new com.gaas.threeKingdoms.gamephase.Normal(game));

        a.getHand().addCardToHand(new BarbarianInvasion(SS7007));
        c.getHand().addCardToHand(new Kill(BS8008));

        List<DomainEvent> events = game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");

        // b（劉備主公）是第一個 reactor → 激將攔截
        AskSkillEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(e -> (AskSkillEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals("激將", ask.getSkillName());
        assertEquals("player-c", ask.getPlayerId(), "問座位順序第一位蜀將關羽");
        assertFalse(events.stream().anyMatch(e -> e instanceof AskKillEvent));

        // 關羽 ACCEPT 交殺
        game.playerUseSkillEffect("player-c", "激將", "ACCEPT", List.of(BS8008.getCardId()), null);

        assertEquals(4, game.getPlayer("player-b").getHP(), "劉備不扣血");
        assertEquals(0, c.getHandSize(), "關羽的殺被打出");
        assertTrue(game.getGraveyard().contains(BS8008.getCardId()));
    }

    @DisplayName("激將全部拒絕 → fallback AskKillEvent(劉備)")
    @Test
    public void jiJiangAllDeclineFallsBackToLiuBei() {
        Game game = new Game();
        game.initDeck();
        Player a = buildPlayer("player-a", General.孫權, com.gaas.threeKingdoms.rolecard.Role.TRAITOR);
        Player b = buildPlayer("player-b", General.劉備, com.gaas.threeKingdoms.rolecard.Role.MONARCH);
        Player c = buildPlayer("player-c", General.關羽, com.gaas.threeKingdoms.rolecard.Role.MINISTER);
        Player d = buildPlayer("player-d", General.孫權, com.gaas.threeKingdoms.rolecard.Role.REBEL);
        game.setPlayers(java.util.Arrays.asList(a, b, c, d));
        game.setCurrentRound(new com.gaas.threeKingdoms.round.Round(a));
        game.enterPhase(new com.gaas.threeKingdoms.gamephase.Normal(game));
        a.getHand().addCardToHand(new BarbarianInvasion(SS7007));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        List<DomainEvent> events = game.playerUseSkillEffect("player-c", "激將", "DECLINE", null, null);

        assertTrue(events.stream().anyMatch(e -> e instanceof AskKillEvent
                && ((AskKillEvent) e).getPlayerId().equals("player-b")));
        // 劉備自己 skip → 扣血
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(3, game.getPlayer("player-b").getHP());
    }

    @DisplayName("劉備非主公 → 激將不觸發")
    @Test
    public void jiJiangNotTriggeredWhenLiuBeiNotMonarch() {
        Game game = createGame(General.孫權, General.劉備, General.關羽, General.孫權);
        // createGame 的 a 是 MONARCH；b 劉備 = MINISTER
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new BarbarianInvasion(SS7007));

        List<DomainEvent> events = game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");

        assertFalse(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof AskKillEvent
                && ((AskKillEvent) e).getPlayerId().equals("player-b")));
    }

    // ===== 救援 =====

    @DisplayName("主公孫權瀕死、吳將（甘寧）出桃 → 救援效果 +1（回 2）")
    @Test
    public void jiuYuanWuPeachHealsPlusOne() {
        Game game = createGame(General.孫權, General.甘寧, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a"); // 孫權主公（currentRoundPlayer 同時為 a — 模擬自傷瀕死較難，用殺）
        Player b = game.getPlayer("player-b");
        // 用 c 殺 a？currentRound 是 a 的回合 — checkIsCurrentRoundValid 擋。改設 a HP=1 然後 b 殺 a 需 b 回合...
        // 簡化：直接呼叫 getDamagedEvent 模擬 a 受致命傷
        a.getBloodCard().setHp(1);
        Kill kill = new Kill(BS8008);
        game.getGraveyard().add(kill);
        game.getDamagedEvent("player-a", "player-c", kill.getId(), kill, "inactive",
                1, a, game.getCurrentRound(), java.util.Optional.empty());
        assertEquals(0, a.getHP());

        // 輪詢順序：先問瀕死者 a 自己（skip），再到 b（甘寧/吳）出桃
        game.playerPlayCard("player-a", "", "player-a", PlayType.SKIP.getPlayType());
        b.getHand().addCardToHand(new Peach(BH3029));
        game.playerPlayCard("player-b", BH3029.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());

        assertEquals(2, a.getHP(), "救援：吳將的桃回 2（0 + 1 + 1）");
    }

    @DisplayName("主公孫權瀕死、非吳將（劉備）出桃 → 不加成（回 1）")
    @Test
    public void jiuYuanNonWuPeachHealsNormal() {
        Game game = createGame(General.孫權, General.甘寧, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        Player c = game.getPlayer("player-c");
        a.getBloodCard().setHp(1);
        Kill kill = new Kill(BS8008);
        game.getGraveyard().add(kill);
        game.getDamagedEvent("player-a", "player-c", kill.getId(), kill, "inactive",
                1, a, game.getCurrentRound(), java.util.Optional.empty());

        // 輪詢順序：先問 a 自己（skip），再 b（skip），到 c（劉備）出桃
        game.playerPlayCard("player-a", "", "player-a", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        c.getHand().addCardToHand(new Peach(BH3029));
        game.playerPlayCard("player-c", BH3029.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());

        assertEquals(1, a.getHP(), "非吳將的桃只回 1");
    }

    // ===== 流離 =====

    @DisplayName("大喬被殺 → 流離詢問；ACCEPT 棄牌轉移給距離 1 內的 c → c 被問閃")
    @Test
    public void liuLiRedirectsKillToAdjacentPlayer() {
        // 座位 a → b（大喬）→ c → d；c 是 b 下家距離 1
        Game game = createGame(General.劉備, General.大喬, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Peach(BH3029)); // 可棄的牌

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        AskSkillEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(e -> (AskSkillEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals("流離", ask.getSkillName());

        List<DomainEvent> redirectEvents = game.playerUseSkillEffect(
                "player-b", "流離", "ACCEPT", List.of(BH3029.getCardId()), "player-c");

        assertTrue(redirectEvents.stream().anyMatch(e -> e instanceof AskDodgeEvent
                && ((AskDodgeEvent) e).getPlayerId().equals("player-c")), "轉移後問 c 出閃");
        assertEquals(0, b.getHandSize(), "大喬棄了一張牌");

        // c skip → c 扣血、大喬無傷
        game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertEquals(4, b.getHP());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("大喬流離 SKIP → 自己面對 AskDodge")
    @Test
    public void liuLiSkipFallsBackToSelf() {
        Game game = createGame(General.劉備, General.大喬, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Peach(BH3029));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "流離", "SKIP", null, null);

        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent
                && ((AskDodgeEvent) e).getPlayerId().equals("player-b")));
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(3, b.getHP());
    }

    @DisplayName("大喬無手牌 → 流離不觸發，直接問閃")
    @Test
    public void liuLiNotTriggeredWithoutHandCards() {
        Game game = createGame(General.劉備, General.大喬, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertFalse(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent));
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent
                && ((AskDodgeEvent) e).getPlayerId().equals("player-b")));
    }

    @DisplayName("流離不能轉移給攻擊者")
    @Test
    public void liuLiCannotRedirectToAttacker() {
        Game game = createGame(General.劉備, General.大喬, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Peach(BH3029));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseSkillEffect("player-b", "流離", "ACCEPT", List.of(BH3029.getCardId()), "player-a"));
    }
}

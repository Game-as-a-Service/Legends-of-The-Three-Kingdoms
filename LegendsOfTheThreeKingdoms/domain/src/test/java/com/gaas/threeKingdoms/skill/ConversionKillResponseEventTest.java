package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.NormalActiveKillBehavior;
import com.gaas.threeKingdoms.behavior.behavior.ViperSpearKillBehavior;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.GameStatusEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.events.ViperSpearKillTriggerEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.BorrowedSword;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.rolecard.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 被要求出殺（南蠻入侵 / 決鬥 / 借刀殺人）時以武聖、龍膽轉化或激將代出：
 * 事件要是實際打出那張牌的 PlayCardEvent，不能寫成丈八蛇矛（ViperSpearKillTriggerEvent /「用丈八蛇矛出殺」）。
 * 使用者回報：趙雲（沒有丈八蛇矛）龍膽閃當殺回應南蠻，log 出現「YangJun用丈八蛇矛出殺」與「丈八蛇矛發動」。
 */
public class ConversionKillResponseEventTest extends PassiveSkillTestBase {

    private static PlayCardEvent onlyPlayCardEvent(List<DomainEvent> events) {
        List<PlayCardEvent> plays = events.stream()
                .filter(e -> e instanceof PlayCardEvent).map(e -> (PlayCardEvent) e).toList();
        assertEquals(1, plays.size(), "應有一個出殺的 PlayCardEvent：" + plays);
        return plays.get(0);
    }

    private static void assertNoViperSpear(List<DomainEvent> events) {
        assertTrue(events.stream().noneMatch(e -> e instanceof ViperSpearKillTriggerEvent), "不該有 ViperSpearKillTriggerEvent");
        assertTrue(events.stream().noneMatch(e -> e.getMessage() != null && e.getMessage().contains("丈八蛇矛")),
                "訊息不該提到丈八蛇矛");
    }

    private static void assertStatusMessage(List<DomainEvent> events, String message) {
        assertTrue(events.stream().anyMatch(e -> e instanceof GameStatusEvent && message.equals(e.getMessage())),
                "應有遊戲訊息「" + message + "」");
    }

    private static void assertPlayCard(PlayCardEvent play, String playerId, String cardId, String targetPlayerId) {
        assertEquals(playerId, play.getPlayerId());
        assertEquals(cardId, play.getCardId());
        assertEquals(targetPlayerId, play.getTargetPlayerId());
        assertEquals(PlayType.ACTIVE.getPlayType(), play.getPlayType());
    }

    @DisplayName("龍膽回應南蠻：PlayCardEvent(趙雲, 閃 → 南蠻使用者)、「趙雲 發動龍膽出殺」，輪詢推進到下一位")
    @Test
    public void longDanAnswersBarbarianInvasion() {
        Game game = createGame(General.孫權, General.趙雲, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));
        Player b = game.getPlayer("player-b");
        b.getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-b", "龍膽", "KILL", List.of(BH2028.getCardId()), null);

        assertNoViperSpear(events);
        assertPlayCard(onlyPlayCardEvent(events), "player-b", BH2028.getCardId(), "player-a");
        assertStatusMessage(events, "趙雲 發動龍膽出殺");
        assertEquals(4, b.getHP());
        assertTrue(events.stream().anyMatch(e -> e instanceof AskKillEvent k && k.getPlayerId().equals("player-c")),
                "輪詢推進到 player-c");
        assertEquals("player-c", game.getActivePlayer().getId());
    }

    @DisplayName("武聖回應南蠻：PlayCardEvent(關羽, 紅色牌 → 南蠻使用者)、「關羽 發動武聖出殺」")
    @Test
    public void wuShengAnswersBarbarianInvasion() {
        Game game = createGame(General.孫權, General.關羽, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));
        Player b = game.getPlayer("player-b");
        b.getHand().addCardToHand(new Peach(BH3029));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-b", "武聖", "KILL", List.of(BH3029.getCardId()), null);

        assertNoViperSpear(events);
        assertPlayCard(onlyPlayCardEvent(events), "player-b", BH3029.getCardId(), "player-a");
        assertStatusMessage(events, "關羽 發動武聖出殺");
        assertEquals(4, b.getHP());
        assertEquals("player-c", game.getActivePlayer().getId());
    }

    @DisplayName("龍膽回應決鬥：PlayCardEvent(趙雲, 閃 → 決鬥對手)，換對手被問殺")
    @Test
    public void longDanAnswersDuel() {
        Game game = createGame(General.甘寧, General.趙雲, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(Arrays.asList(new Duel(SSA001), new Kill(BS8008)));
        // 手上也有殺才會被問出殺（只有閃時決鬥直接結算，與本次修正無關）
        b.getHand().addCardToHand(Arrays.asList(new Dodge(BH2028), new Kill(BS9009)));

        game.playerPlayCard("player-a", SSA001.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-b", "龍膽", "KILL", List.of(BH2028.getCardId()), null);

        assertNoViperSpear(events);
        assertPlayCard(onlyPlayCardEvent(events), "player-b", BH2028.getCardId(), "player-a");
        assertTrue(events.stream().anyMatch(e -> e instanceof AskKillEvent k && k.getPlayerId().equals("player-a")));
        assertEquals("player-a", game.getActivePlayer().getId());
        assertEquals(4, a.getHP());
        assertEquals(4, b.getHP());
    }

    @DisplayName("龍膽回應借刀殺人：PlayCardEvent(趙雲, 閃 → 攻擊目標)、殺 behavior 不是丈八蛇矛；目標不出閃扣 1 血")
    @Test
    public void longDanAnswersBorrowedSword() {
        Game game = createGame(General.甘寧, General.趙雲, General.甘寧, General.甘寧);
        Player b = game.getPlayer("player-b");
        Player c = game.getPlayer("player-c");
        game.getPlayer("player-a").getHand().addCardToHand(new BorrowedSword(SCK065));
        b.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066));
        b.getHand().addCardToHand(Arrays.asList(new Dodge(BH2028), new Kill(BS9009)));

        game.playerPlayCard("player-a", SCK065.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.useBorrowedSwordEffect("player-a", "player-b", "player-c");
        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-b", "龍膽", "KILL", List.of(BH2028.getCardId()), "player-c");

        assertNoViperSpear(events);
        assertPlayCard(onlyPlayCardEvent(events), "player-b", BH2028.getCardId(), "player-c");
        assertStatusMessage(events, "趙雲 發動龍膽出殺");
        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent d && d.getPlayerId().equals("player-c")));
        assertTrue(game.peekTopBehavior() instanceof NormalActiveKillBehavior);
        assertFalse(game.peekTopBehavior() instanceof ViperSpearKillBehavior);
        assertTrue(game.getGraveyard().contains(BH2028.getCardId()));

        game.playerPlayCard("player-c", "", "player-b", PlayType.SKIP.getPlayType());
        assertEquals(3, c.getHP(), "轉化殺照常結算");
        assertEquals(1, b.getHandSize(), "只用掉龍膽的閃");
    }

    @DisplayName("激將回應南蠻：蜀將的殺記為劉備打出 PlayCardEvent(劉備, 殺 → 南蠻使用者)、「劉備 以激將打出 殺」")
    @Test
    public void jiJiangAnswersBarbarianInvasion() {
        Game game = new Game();
        game.initDeck();
        Player a = buildPlayer("player-a", General.孫權, Role.TRAITOR);
        Player b = buildPlayer("player-b", General.劉備, Role.MONARCH);
        Player c = buildPlayer("player-c", General.關羽, Role.MINISTER);
        Player d = buildPlayer("player-d", General.孫權, Role.REBEL);
        game.setPlayers(Arrays.asList(a, b, c, d));
        game.setCurrentRound(new com.gaas.threeKingdoms.round.Round(a));
        game.enterPhase(new com.gaas.threeKingdoms.gamephase.Normal(game));
        a.getHand().addCardToHand(new BarbarianInvasion(SS7007));
        c.getHand().addCardToHand(new Kill(BS8008));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-c", "激將", "ACCEPT", List.of(BS8008.getCardId()), null);

        assertNoViperSpear(events);
        assertPlayCard(onlyPlayCardEvent(events), "player-b", BS8008.getCardId(), "player-a");
        assertStatusMessage(events, "劉備 以激將打出 殺");
        assertEquals(4, b.getHP(), "劉備不扣血");
        assertEquals("player-c", game.getActivePlayer().getId(), "輪詢推進到關羽");
    }
}

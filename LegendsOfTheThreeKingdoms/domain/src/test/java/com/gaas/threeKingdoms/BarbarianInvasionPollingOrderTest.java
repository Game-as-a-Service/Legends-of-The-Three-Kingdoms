package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.events.AskJianXiongEffectEvent;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.player.*;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.rolecard.RoleCard;
import com.gaas.threeKingdoms.round.Round;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 南蠻入侵輪詢順序驗證（使用者回報：最新版本疑似跳過一人）。
 * 座位 a → b → c → d（a 出南蠻）；正確順序 = 從出牌者下家起逆時針：b → c → d。
 * 每步斷言 AskKillEvent 目標與 activePlayer。
 */
public class BarbarianInvasionPollingOrderTest {

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
                .map(e -> ((AskKillEvent) e).getPlayerId()).collect(Collectors.toList());
    }

    @DisplayName("全部 skip：詢問順序必須是 b → c → d，無跳過")
    @Test
    public void allSkip_orderIsBCD() {
        Game game = createGame(General.甘寧, General.甘寧, General.孫權, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));

        List<DomainEvent> e1 = game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        assertEquals(List.of("player-b"), askKillTargets(e1), "第一個被問的是 b");
        assertEquals("player-b", game.getCurrentRound().getActivePlayer().getId());

        List<DomainEvent> e2 = game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-c"), askKillTargets(e2), "b skip 後問 c（不可跳到 d）");
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());

        List<DomainEvent> e3 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e3), "c skip 後問 d");
        assertEquals("player-d", game.getCurrentRound().getActivePlayer().getId());

        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
        assertEquals(3, game.getPlayer("player-b").getHP());
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertEquals(3, game.getPlayer("player-d").getHP());
    }

    @DisplayName("全部出殺：詢問順序 b → c → d，無人扣血")
    @Test
    public void allKill_orderIsBCD() {
        Game game = createGame(General.甘寧, General.甘寧, General.孫權, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));
        game.getPlayer("player-b").getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-c").getHand().addCardToHand(new Kill(BS9009));
        game.getPlayer("player-d").getHand().addCardToHand(new Kill(BS0010));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        List<DomainEvent> e2 = game.playerPlayCard("player-b", BS8008.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        assertEquals(List.of("player-c"), askKillTargets(e2), "b 出殺後問 c");
        List<DomainEvent> e3 = game.playerPlayCard("player-c", BS9009.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e3), "c 出殺後問 d");
        game.playerPlayCard("player-d", BS0010.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());

        assertTrue(game.getTopBehavior().isEmpty());
        assertEquals(4, game.getPlayer("player-b").getHP());
        assertEquals(4, game.getPlayer("player-c").getHP());
        assertEquals(4, game.getPlayer("player-d").getHP());
    }

    @DisplayName("混合（b 殺、c skip、d 殺）：順序 b → c → d")
    @Test
    public void mixed_orderIsBCD() {
        Game game = createGame(General.甘寧, General.甘寧, General.孫權, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));
        game.getPlayer("player-b").getHand().addCardToHand(new Kill(BS8008));
        game.getPlayer("player-d").getHand().addCardToHand(new Kill(BS0010));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        List<DomainEvent> e2 = game.playerPlayCard("player-b", BS8008.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());
        assertEquals(List.of("player-c"), askKillTargets(e2));
        List<DomainEvent> e3 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e3), "c skip（扣血）後問 d");
        game.playerPlayCard("player-d", BS0010.getCardId(), "player-a", PlayType.ACTIVE.getPlayType());

        assertTrue(game.getTopBehavior().isEmpty());
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertEquals(4, game.getPlayer("player-d").getHP());
    }

    @DisplayName("b=曹操 skip 受傷 → 奸雄 ACCEPT → 下一個被問的必須是 c（不可跳到 d）")
    @Test
    public void jianXiongAccept_nextAskedIsC() {
        Game game = createGame(General.甘寧, General.曹操, General.孫權, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        List<DomainEvent> e2 = game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(e2.stream().anyMatch(e -> e instanceof AskJianXiongEffectEvent));
        assertTrue(askKillTargets(e2).isEmpty(), "奸雄詢問中，不應先問下一個 reactor");

        List<DomainEvent> e3 = game.playerUseJianXiongEffect("player-b", AskJianXiongEffectEvent.Choice.ACCEPT);
        assertEquals(List.of("player-c"), askKillTargets(e3), "奸雄解決後問 c — 跳到 d 即為 bug");
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());

        List<DomainEvent> e4 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e4), "c skip 後問 d");
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("b=曹操 奸雄 SKIP → 下一個被問的也必須是 c")
    @Test
    public void jianXiongSkip_nextAskedIsC() {
        Game game = createGame(General.甘寧, General.曹操, General.孫權, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        List<DomainEvent> e3 = game.playerUseJianXiongEffect("player-b", AskJianXiongEffectEvent.Choice.SKIP);

        assertEquals(List.of("player-c"), askKillTargets(e3), "奸雄放棄後問 c");
        List<DomainEvent> e4 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e4), "接著問 d");
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("c=陸遜（謙遜免疫）→ 只問 b、d；b 回應後直接問 d，且陸遜不被問")
    @Test
    public void qianXunImmuneMiddle_asksOnlyBandD() {
        Game game = createGame(General.甘寧, General.甘寧, General.陸遜, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));

        List<DomainEvent> e1 = game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        assertEquals(List.of("player-b"), askKillTargets(e1));

        List<DomainEvent> e2 = game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e2),
                "陸遜免疫應被跳過 — 問 c 或直接結束皆為 bug");
        assertEquals("player-d", game.getCurrentRound().getActivePlayer().getId());

        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty(), "d 回應後南蠻應結束");
        assertEquals(4, game.getPlayer("player-c").getHP(), "陸遜不受傷");
        assertEquals(3, game.getPlayer("player-d").getHP());
    }

    @DisplayName("b=劉備主公（激將）關羽代殺 → 下一個被問的必須是 c")
    @Test
    public void jiJiangAccept_nextAskedIsC() {
        // 激將是主公技：b=劉備 必須是 MONARCH
        Game game = new Game();
        game.initDeck();
        Player pa = build("player-a", General.甘寧, Role.TRAITOR);
        Player pb = build("player-b", General.劉備, Role.MONARCH);
        Player pc = build("player-c", General.關羽, Role.MINISTER);
        Player pd = build("player-d", General.孫權, Role.REBEL);
        game.setPlayers(asList(pa, pb, pc, pd));
        game.setCurrentRound(new Round(pa));
        game.enterPhase(new Normal(game));
        game.getPlayer("player-a").getHand().addCardToHand(new BarbarianInvasion(SS7007));
        game.getPlayer("player-c").getHand().addCardToHand(new Kill(BS8008));

        List<DomainEvent> e1 = game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        // 激將攔截：問蜀將關羽（c）而非直接 AskKill(b)
        assertTrue(e1.stream().anyMatch(e -> e instanceof AskSkillEffectEvent
                && ((AskSkillEffectEvent) e).getSkillName().equals("激將")));

        List<DomainEvent> e2 = game.playerUseSkillEffect("player-c", "激將", "ACCEPT",
                List.of(BS8008.getCardId()), null);
        assertEquals(List.of("player-c"), askKillTargets(e2),
                "劉備（第一 reactor）被代殺後，輪到 c 被問南蠻 — 跳到 d 即為 bug");
        assertEquals(4, game.getPlayer("player-b").getHP(), "劉備被代殺不扣血");

        List<DomainEvent> e3 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e3));
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
    }
}

package com.gaas.threeKingdoms;

import com.gaas.threeKingdoms.builders.PlayerBuilder;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskKillEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.gamephase.Normal;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.generalcard.GeneralCard;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
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
 * 反饋 × AOE polling（南蠻入侵 / 萬箭齊發）— wiki「當你受到傷害後」應含 AOE 傷害。
 * 先前 FanKuiSkill 在 polling caller 上直接跳過（follow-up 註記）；本測試驗證：
 * 受傷 → 反饋詢問（polling-advance defer）→ resolve 後 resume 輪詢（不跳人）。
 * Mirror 奸雄 #209 的 reload-safe 樣板（param flag + resume hook，無 transient callback）。
 */
public class FanKuiAoePollingTest {

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

    private boolean hasFanKuiAsk(List<DomainEvent> events) {
        return events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent
                && ((AskSkillEffectEvent) e).getSkillName().equals("反饋"));
    }

    @DisplayName("南蠻：b=司馬懿 skip 受傷 → 反饋詢問（polling 暫停），ACCEPT 拿 a 手牌後問 c")
    @Test
    public void fanKuiAcceptInBarbarianInvasion_thenNextAskedIsC() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(List.of(new BarbarianInvasion(SS7007), new Peach(BH3029)));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        List<DomainEvent> e2 = game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());

        assertTrue(hasFanKuiAsk(e2), "受傷後應詢問反饋");
        assertTrue(askKillTargets(e2).isEmpty(), "反饋詢問中，不應先問下一個 reactor");
        assertEquals(3, b.getHP());

        List<DomainEvent> e3 = game.playerUseSkillEffect("player-b", "反饋", "ACCEPT", null, null);

        assertTrue(b.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BH3029.getCardId())),
                "司馬懿拿走攻擊者手牌");
        assertEquals(0, a.getHandSize());
        assertEquals(List.of("player-c"), askKillTargets(e3), "反饋解決後 resume 輪詢問 c — 跳到 d 即為 bug");
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId());

        List<DomainEvent> e4 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e4), "c skip 後問 d");
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("南蠻：反饋 SKIP → 一樣 resume 輪詢問 c")
    @Test
    public void fanKuiSkipInBarbarianInvasion_thenNextAskedIsC() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        game.getPlayer("player-a").getHand()
                .addCardToHand(List.of(new BarbarianInvasion(SS7007), new Peach(BH3029)));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        List<DomainEvent> e3 = game.playerUseSkillEffect("player-b", "反饋", "SKIP", null, null);

        assertEquals(1, game.getPlayer("player-a").getHandSize(), "SKIP 不拿牌");
        assertEquals(List.of("player-c"), askKillTargets(e3), "反饋放棄後問 c");

        List<DomainEvent> e4 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askKillTargets(e4));
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("萬箭：b=司馬懿 skip 受傷 → 反饋 ACCEPT 指定拿 a 的裝備 → resume 問 c 出閃")
    @Test
    public void fanKuiTakesEquipmentInArrowBarrage_thenNextAskedIsC() {
        Game game = createGame(General.甘寧, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new ArrowBarrage(SHA040));
        a.getEquipment().setWeapon(new RepeatingCrossbowCard(EH5031));

        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", "active");
        List<DomainEvent> e2 = game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());

        assertTrue(hasFanKuiAsk(e2), "受傷後應詢問反饋");
        assertTrue(askDodgeTargets(e2).isEmpty(), "反饋詢問中，不應先問下一個 reactor");

        List<DomainEvent> e3 = game.playerUseSkillEffect(
                "player-b", "反饋", "ACCEPT", List.of(EH5031.getCardId()), null);

        assertTrue(b.getHand().getCards().stream().anyMatch(c -> c.getId().equals(EH5031.getCardId())),
                "司馬懿拿走攻擊者裝備");
        assertFalse(a.getEquipment().hasAnyEquipment());
        assertEquals(List.of("player-c"), askDodgeTargets(e3), "反饋解決後 resume 輪詢問 c 出閃");

        List<DomainEvent> e4 = game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        assertEquals(List.of("player-d"), askDodgeTargets(e4));
        game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("南蠻：最後一個 reactor d=司馬懿 受傷 → 反饋 resolve 後輪詢正常結束")
    @Test
    public void fanKuiOnLastReactor_pollingEndsCleanly() {
        Game game = createGame(General.甘寧, General.孫權, General.孫權, General.司馬懿);
        Player a = game.getPlayer("player-a");
        Player d = game.getPlayer("player-d");
        a.getHand().addCardToHand(List.of(new BarbarianInvasion(SS7007), new Peach(BH3029)));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        game.playerPlayCard("player-c", "", "player-a", PlayType.SKIP.getPlayType());
        List<DomainEvent> e4 = game.playerPlayCard("player-d", "", "player-a", PlayType.SKIP.getPlayType());

        assertTrue(hasFanKuiAsk(e4), "最後一個 reactor 受傷也應詢問反饋");
        assertFalse(game.getTopBehavior().isEmpty(), "反饋詢問中");

        game.playerUseSkillEffect("player-d", "反饋", "ACCEPT", null, null);

        assertTrue(d.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BH3029.getCardId())));
        assertTrue(game.getTopBehavior().isEmpty(), "反饋解決後輪詢結束、stack 清空");
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId(),
                "輪詢結束 activePlayer 回到出牌者");
    }
}

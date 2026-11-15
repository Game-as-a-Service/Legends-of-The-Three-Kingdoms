package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.Behavior;
import com.gaas.threeKingdoms.behavior.behavior.HeavenlyDoubleHalberdKillBehavior;
import com.gaas.threeKingdoms.events.*;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.HandCard;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.EighteenSpanViperSpearCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.HeavenlyDoubleHalberdCard;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 流離適用所有殺（FAQ：當你成為【殺】的目標時…），不只手上那張普通殺：
 * 丈八蛇矛（原本直接問防具／閃，流離、鐵騎、護駕都被跳過）、方天畫戟（原本不問流離）、
 * 轉化殺（武聖，本來就有走流離，這裡補測試鎖住）。
 * 座位 a → b → c → d；相鄰距離 1，對角距離 2。
 */
public class LiuLiOtherKillsTest extends PassiveSkillTestBase {

    private static boolean askedLiuLi(List<DomainEvent> events, String playerId) {
        return events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent a
                && "流離".equals(a.getSkillName()) && playerId.equals(a.getPlayerId()));
    }

    private static boolean askedDodge(List<DomainEvent> events, String playerId) {
        return events.stream().anyMatch(e -> e instanceof AskDodgeEvent a && playerId.equals(a.getPlayerId()));
    }

    private static void giveDaQiaoCards(Player daQiao) {
        daQiao.getHand().addCardToHand(List.of(new Peach(BH3029), new Dodge(BH2028)));
    }

    // ---------- 丈八蛇矛 ----------

    private Game givenViperSpearOnDaQiao(General attacker) {
        Game game = createGame(attacker, General.大喬, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        a.getHand().addCardToHand(List.of(new Kill(BS8008), new Kill(BS9009)));
        giveDaQiaoCards(game.getPlayer("player-b"));
        return game;
    }

    @DisplayName("丈八蛇矛殺大喬 → 先問流離（原本直接問閃）")
    @Test
    public void viperSpearKillAsksLiuLi() {
        Game game = givenViperSpearOnDaQiao(General.甘寧);

        List<DomainEvent> events = game.playerUseViperSpearKill("player-a", "player-b",
                List.of(BS8008.getCardId(), BS9009.getCardId()));

        assertTrue(askedLiuLi(events, "player-b"), "丈八蛇矛的殺也要能流離：" + events);
        assertFalse(askedDodge(events, "player-b"));
    }

    @DisplayName("丈八蛇矛殺大喬 → 流離轉給 c → c 被問閃，不出 → c 扣血、大喬不扣")
    @Test
    public void viperSpearKillRedirectedByLiuLi() {
        Game game = givenViperSpearOnDaQiao(General.甘寧);
        game.playerUseViperSpearKill("player-a", "player-b", List.of(BS8008.getCardId(), BS9009.getCardId()));

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "流離", "ACCEPT",
                List.of(BH3029.getCardId()), "player-c");
        assertTrue(askedDodge(events, "player-c"), "轉移後問新目標出閃：" + events);

        game.playerPlayCard("player-c", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertEquals(4, game.getPlayer("player-b").getHP());
    }

    @DisplayName("丈八蛇矛殺大喬 → 放棄流離 → 問大喬出閃")
    @Test
    public void viperSpearKillLiuLiDeclined() {
        Game game = givenViperSpearOnDaQiao(General.甘寧);
        game.playerUseViperSpearKill("player-a", "player-b", List.of(BS8008.getCardId(), BS9009.getCardId()));

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "流離", "SKIP", null, null);

        assertTrue(askedDodge(events, "player-b"), "放棄流離後問大喬出閃：" + events);
    }

    @DisplayName("馬超用丈八蛇矛殺 → 也會判鐵騎（紅色 → 不能閃，直接扣血）")
    @Test
    public void viperSpearKillTriggersTieQi() {
        Game game = createGame(General.馬超, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new EighteenSpanViperSpearCard(ESQ025));
        a.getHand().addCardToHand(List.of(new Kill(BS8008), new Kill(BS9009)));
        game.getDeck().add(List.of((HandCard) new Peach(BH4030)));

        List<DomainEvent> events = game.playerUseViperSpearKill("player-a", "player-b",
                List.of(BS8008.getCardId(), BS9009.getCardId()));

        assertTrue(events.stream().anyMatch(e -> e instanceof SkillEffectEvent se && "鐵騎".equals(se.getSkillName())),
                "丈八蛇矛的殺也要判鐵騎：" + events);
        assertFalse(askedDodge(events, "player-b"));
        assertEquals(3, game.getPlayer("player-b").getHP());
    }

    // ---------- 方天畫戟 ----------

    private Game givenHalberd(General b, General c) {
        Game game = createGame(General.甘寧, b, c, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new HeavenlyDoubleHalberdCard(EDQ103));
        a.getHand().addCardToHand(List.of(new Kill(BS8008)));
        return game;
    }

    private static HeavenlyDoubleHalberdKillBehavior halberdOf(Game game) {
        for (Behavior behavior : game.getTopBehavior()) {
            if (behavior instanceof HeavenlyDoubleHalberdKillBehavior h) return h;
        }
        throw new AssertionError("no halberd behavior");
    }

    @DisplayName("方天畫戟殺 [大喬, d] → 輪到大喬先問流離 → 轉給 c：c 取代大喬的位置，結算完再輪到 d")
    @Test
    public void halberdFirstTargetRedirected() {
        Game game = givenHalberd(General.大喬, General.甘寧);
        giveDaQiaoCards(game.getPlayer("player-b"));

        List<DomainEvent> events = game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(),
                List.of("player-b", "player-d"));
        assertTrue(askedLiuLi(events, "player-b"), "方天畫戟的殺也要能流離：" + events);
        assertFalse(askedDodge(events, "player-b"));

        events = game.playerUseSkillEffect("player-b", "流離", "ACCEPT", List.of(BH3029.getCardId()), "player-c");
        assertTrue(askedDodge(events, "player-c"), "轉移後問新目標：" + events);
        assertEquals(List.of("player-c", "player-d"), halberdOf(game).getReactionPlayers());

        events = game.playerPlayCard("player-c", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertTrue(askedDodge(events, "player-d"), "c 結算完繼續輪到 d：" + events);

        game.playerPlayCard("player-d", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-d").getHP());
        assertEquals(4, game.getPlayer("player-b").getHP());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("方天畫戟殺 [b, 大喬] → b 結算後輪到大喬才問流離 → 轉給 d")
    @Test
    public void halberdLaterTargetRedirected() {
        Game game = givenHalberd(General.甘寧, General.大喬);
        giveDaQiaoCards(game.getPlayer("player-c"));

        List<DomainEvent> events = game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(),
                List.of("player-b", "player-c"));
        assertTrue(askedDodge(events, "player-b"));
        assertFalse(askedLiuLi(events, "player-c"), "還沒輪到大喬");

        events = game.playerPlayCard("player-b", "", "player-a", "skip");
        assertTrue(askedLiuLi(events, "player-c"), "輪到大喬時問流離：" + events);

        events = game.playerUseSkillEffect("player-c", "流離", "ACCEPT", List.of(BH3029.getCardId()), "player-d");
        assertTrue(askedDodge(events, "player-d"));

        game.playerPlayCard("player-d", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-b").getHP());
        assertEquals(4, game.getPlayer("player-c").getHP());
        assertEquals(3, game.getPlayer("player-d").getHP());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("方天畫戟：已是本殺目標的角色不能被流離指定（大喬身邊只有攻擊者和另一個目標 → 不問流離）")
    @Test
    public void halberdCannotRedirectToExistingTarget() {
        Game game = givenHalberd(General.大喬, General.甘寧);
        giveDaQiaoCards(game.getPlayer("player-b"));

        List<DomainEvent> events = game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(),
                List.of("player-b", "player-c"));

        assertFalse(askedLiuLi(events, "player-b"), "c 已是目標、a 是攻擊者 → 沒有可轉移對象");
        assertTrue(askedDodge(events, "player-b"));
    }

    @DisplayName("方天畫戟殺 [大喬, d] → 放棄流離 → 問大喬出閃 → 接著輪到 d")
    @Test
    public void halberdLiuLiDeclined() {
        Game game = givenHalberd(General.大喬, General.甘寧);
        giveDaQiaoCards(game.getPlayer("player-b"));
        game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(), List.of("player-b", "player-d"));

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "流離", "SKIP", null, null);
        assertTrue(askedDodge(events, "player-b"), "放棄流離後問大喬出閃：" + events);

        events = game.playerPlayCard("player-b", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-b").getHP());
        assertTrue(askedDodge(events, "player-d"));
        assertFalse(askedLiuLi(events, "player-b"));
    }

    // ---------- 轉化殺 ----------

    @DisplayName("關羽武聖（紅牌當殺）殺大喬 → 也會問流離")
    @Test
    public void wuShengKillAsksLiuLi() {
        Game game = createGame(General.關羽, General.大喬, General.甘寧, General.甘寧);
        game.getPlayer("player-a").getHand().addCardToHand(List.of(new Dodge(BH2028)));
        game.getPlayer("player-b").getHand().addCardToHand(List.of(new Peach(BH3029)));

        List<DomainEvent> events = game.playerUseSkillEffect("player-a", "武聖", "KILL",
                List.of(BH2028.getCardId()), "player-b");

        assertTrue(askedLiuLi(events, "player-b"), "轉化殺也要能流離：" + events);
    }
}

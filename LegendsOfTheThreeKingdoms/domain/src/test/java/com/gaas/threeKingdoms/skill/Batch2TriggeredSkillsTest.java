package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.SkillEffectEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Batch 2：反饋 / 遺計 / 剛烈 / 天妒 / 洛神 / 鐵騎 / 梟姬。
 * 判定牌以 deck.add 控制（Stack — list 最後一個元素最先被抽）。
 */
public class Batch2TriggeredSkillsTest extends PassiveSkillTestBase {

    private List<DomainEvent> killAndSkip(Game game, String attacker, String target) {
        game.playerPlayCard(attacker, BS8008.getCardId(), target, "active");
        return game.playerPlayCard(target, "", attacker, "skip");
    }

    // ===== 反饋 =====

    @DisplayName("司馬懿受殺傷 → AskSkillEffectEvent(反饋)；ACCEPT 取走攻擊者第一張手牌")
    @Test
    public void fanKuiAcceptTakesAttackerHandCard() {
        Game game = createGame(General.劉備, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029)));

        List<DomainEvent> events = killAndSkip(game, "player-a", "player-b");

        AskSkillEffectEvent ask = events.stream()
                .filter(e -> e instanceof AskSkillEffectEvent).map(e -> (AskSkillEffectEvent) e)
                .findFirst().orElseThrow();
        assertEquals("反饋", ask.getSkillName());
        assertEquals("player-b", ask.getPlayerId());

        game.playerUseSkillEffect("player-b", "反饋", "ACCEPT", null, null);

        assertTrue(b.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BH3029.getCardId())),
                "司馬懿應取走攻擊者剩下的那張手牌");
        assertEquals(0, a.getHandSize());
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("反饋 SKIP → 不取牌")
    @Test
    public void fanKuiSkipTakesNothing() {
        Game game = createGame(General.劉備, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029)));

        killAndSkip(game, "player-a", "player-b");
        game.playerUseSkillEffect("player-b", "反饋", "SKIP", null, null);

        assertEquals(1, a.getHandSize());
        assertEquals(0, game.getPlayer("player-b").getHandSize());
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("攻擊者無手牌無裝備 → 反饋不觸發")
    @Test
    public void fanKuiNotTriggeredWhenAttackerHasNothing() {
        Game game = createGame(General.劉備, General.司馬懿, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008)); // 出殺後空手且無裝備

        List<DomainEvent> events = killAndSkip(game, "player-a", "player-b");

        assertFalse(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent));
        assertTrue(game.isTopBehaviorEmpty());
    }

    // ===== 遺計 =====

    @DisplayName("郭嘉受傷 → 遺計 ACCEPT 摸兩張")
    @Test
    public void yiJiAcceptDrawsTwo() {
        Game game = createGame(General.劉備, General.郭嘉, General.孫權, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));

        List<DomainEvent> events = killAndSkip(game, "player-a", "player-b");
        assertTrue(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent
                && ((AskSkillEffectEvent) e).getSkillName().equals("遺計")));

        game.playerUseSkillEffect("player-b", "遺計", "ACCEPT", null, null);

        assertEquals(2, game.getPlayer("player-b").getHandSize());
    }

    @DisplayName("郭嘉受傷 → 遺計 GIVE 令另一角色獲得一張")
    @Test
    public void yiJiGiveLetsAnotherPlayerDraw() {
        Game game = createGame(General.劉備, General.郭嘉, General.孫權, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));

        killAndSkip(game, "player-a", "player-b");
        game.playerUseSkillEffect("player-b", "遺計", "GIVE", null, "player-c");

        assertEquals(0, game.getPlayer("player-b").getHandSize());
        assertEquals(1, game.getPlayer("player-c").getHandSize());
    }

    // ===== 剛烈 =====

    @DisplayName("夏侯惇剛烈 ACCEPT、判定非紅桃 → 來源選 DAMAGE 受 1 傷")
    @Test
    public void gangLieJudgementSuccessSourceTakesDamage() {
        Game game = createGame(General.劉備, General.夏侯惇, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        killAndSkip(game, "player-a", "player-b");
        // 判定牌：黑桃 → 剛烈生效
        game.getDeck().add(List.of(new Kill(BS9009)));
        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        assertTrue(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent
                        && ((AskSkillEffectEvent) e).getPlayerId().equals("player-a")),
                "判定生效應詢問傷害來源");

        game.playerUseSkillEffect("player-a", "剛烈", "DAMAGE", null, null);
        assertEquals(3, a.getHP(), "來源選擇受 1 點傷害");
    }

    @DisplayName("夏侯惇剛烈、判定生效 → 來源選 DISCARD 棄兩張手牌")
    @Test
    public void gangLieSourceDiscardsTwoCards() {
        Game game = createGame(General.劉備, General.夏侯惇, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(Arrays.asList(new Kill(BS8008), new Peach(BH3029), new Peach(BH4030)));

        killAndSkip(game, "player-a", "player-b");
        game.getDeck().add(List.of(new Kill(BS9009)));
        game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        game.playerUseSkillEffect("player-a", "剛烈", "DISCARD",
                List.of(BH3029.getCardId(), BH4030.getCardId()), null);

        assertEquals(0, a.getHandSize());
        assertEquals(4, a.getHP(), "棄牌則不受傷");
        assertTrue(game.getGraveyard().contains(BH3029.getCardId()));
    }

    @DisplayName("剛烈判定紅桃 → 不生效，不問來源")
    @Test
    public void gangLieJudgementHeartFails() {
        Game game = createGame(General.劉備, General.夏侯惇, General.孫權, General.孫權);
        game.getPlayer("player-a").getHand().addCardToHand(new Kill(BS8008));

        killAndSkip(game, "player-a", "player-b");
        // 判定牌：紅心 → 不生效
        game.getDeck().add(List.of(new Peach(BH3029)));
        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "剛烈", "ACCEPT", null, null);

        assertFalse(events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent));
        assertTrue(game.isTopBehaviorEmpty());
        assertEquals(4, game.getPlayer("player-a").getHP());
    }

    // ===== 天妒 =====

    @DisplayName("郭嘉閃電判定未中 → 天妒收判定牌入手")
    @Test
    public void tianDuTakesJudgementCard() {
        Game game = createGame(General.郭嘉, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Lightning lightning = new Lightning(SSA014);
        a.addDelayScrollCard(lightning);

        // 判定牌紅心 → 閃電不中 → 天妒收牌
        game.getDeck().add(List.of(new Peach(BH3029)));
        game.handleLightningJudgement(lightning, a);

        assertTrue(a.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BH3029.getCardId())),
                "天妒：判定牌應入手");
        assertFalse(game.getGraveyard().contains(BH3029.getCardId()));
    }

    // ===== 洛神 =====

    @DisplayName("甄姬回合開始洛神 → 黑色判定牌全收，紅色停")
    @Test
    public void luoShenCollectsBlackCardsUntilRed() {
        Game game = createGame(General.甄姬, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        // Stack：後 add 的先抽 → 抽序 = BS9009(黑) → BS8008(黑) → BH3029(紅停)
        game.getDeck().add(List.of(new Peach(BH3029), new Kill(BS8008), new Kill(BS9009)));

        List<DomainEvent> events = game.playerTakeTurnStartInJudgement(a);

        assertTrue(a.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BS9009.getCardId())));
        assertTrue(a.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BS8008.getCardId())));
        assertFalse(a.getHand().getCards().stream().anyMatch(c -> c.getId().equals(BH3029.getCardId())),
                "紅色判定牌不收");
        long luoShenEvents = events.stream().filter(e -> e instanceof SkillEffectEvent
                && ((SkillEffectEvent) e).getSkillName().equals("洛神")).count();
        assertEquals(3, luoShenEvents, "兩黑一紅共三次判定事件");
    }

    @DisplayName("非甄姬回合開始 → 洛神不觸發")
    @Test
    public void luoShenNotTriggeredForOthers() {
        Game game = createGame(General.劉備, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");

        List<DomainEvent> events = game.playerTakeTurnStartInJudgement(a);

        assertFalse(events.stream().anyMatch(e -> e instanceof SkillEffectEvent
                && ((SkillEffectEvent) e).getSkillName().equals("洛神")));
    }

    // ===== 鐵騎 =====

    @DisplayName("馬超出殺、鐵騎判定非紅桃 → 目標不能閃，直接扣血")
    @Test
    public void tieQiSuccessSkipsDodge() {
        Game game = createGame(General.馬超, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Dodge(BH2028)); // 有閃也沒用

        // 判定牌：黑桃 → 鐵騎生效
        game.getDeck().add(List.of(new Kill(BS9009)));
        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertFalse(events.stream().anyMatch(e -> e instanceof AskDodgeEvent),
                "鐵騎生效不問閃");
        assertEquals(3, b.getHP(), "直接扣血");
        assertEquals(1, b.getHandSize(), "閃未被消耗");
    }

    @DisplayName("馬超出殺、鐵騎判定紅桃 → 照常問閃")
    @Test
    public void tieQiHeartFailsAsksDodge() {
        Game game = createGame(General.馬超, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        // 判定牌：紅心 → 鐵騎不生效
        game.getDeck().add(List.of(new Peach(BH3029)));
        List<DomainEvent> events = game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
        assertEquals(4, game.getPlayer("player-b").getHP());
    }

    // ===== 梟姬 =====

    @DisplayName("孫尚香被拆裝備 → 梟姬摸兩張")
    @Test
    public void xiaoJiDrawsTwoAfterEquipmentDismantled() {
        Game game = createGame(General.劉備, General.孫尚香, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Dismantle(SS3003));
        b.getEquipment().setWeapon(new RepeatingCrossbowCard(EH5031));

        game.playerPlayCard("player-a", SS3003.getCardId(), "player-b", "active");
        game.useDismantleEffect("player-a", "player-b", EH5031.getCardId(), null);

        assertEquals(2, b.getHandSize(), "梟姬：失去裝備摸兩張");
        assertFalse(b.getEquipment().hasAnyEquipment());
    }

    @DisplayName("孫尚香被順走裝備 → 梟姬摸兩張")
    @Test
    public void xiaoJiDrawsTwoAfterEquipmentSnatched() {
        Game game = createGame(General.劉備, General.孫尚香, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new com.gaas.threeKingdoms.handcard.scrollcard.Snatch(SS3016));
        b.getEquipment().setWeapon(new RepeatingCrossbowCard(EH5031));

        game.playerPlayCard("player-a", SS3016.getCardId(), "player-b", "active");
        game.useSnatchEffect("player-a", "player-b", EH5031.getCardId(), null);

        assertEquals(2, b.getHandSize(), "梟姬：失去裝備摸兩張");
    }
}

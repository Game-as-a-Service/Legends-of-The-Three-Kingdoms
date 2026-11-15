package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.RedRabbitHorse;
import com.gaas.threeKingdoms.handcard.equipmentcard.armorcard.EightDiagramTactic;
import com.gaas.threeKingdoms.handcard.equipmentcard.mountscard.VioletStallion;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard;
import com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.RepeatingCrossbowCard;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Batch 4：武聖 / 龍膽 / 傾國 / 奇襲 / 國色（轉化牌技，走通用 useSkillEffect）。
 */
public class Batch4ConversionSkillsTest extends PassiveSkillTestBase {

    @DisplayName("關羽武聖：紅色牌（桃）當殺主動使用 → 目標被問閃、skip 扣 1 血")
    @Test
    public void wuShengRedCardAsActiveKill() {
        Game game = createGame(General.關羽, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Peach(BH3029)); // 紅心

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-a", "武聖", "KILL", List.of(BH3029.getCardId()), "player-b");

        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent
                && ((AskDodgeEvent) e).getPlayerId().equals("player-b")));
        assertEquals(0, a.getHandSize());
        assertTrue(game.getGraveyard().contains(BH3029.getCardId()));

        game.playerPlayCard("player-b", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-b").getHP(), "視為殺：扣 1 血（非桃回血）");
    }

    @DisplayName("武聖殺計入出殺次數：第二次武聖殺 → 拋例外")
    @Test
    public void wuShengKillCountsTowardsKillLimit() {
        Game game = createGame(General.關羽, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(java.util.Arrays.asList(new Peach(BH3029), new Peach(BH4030)));

        game.playerUseSkillEffect("player-a", "武聖", "KILL", List.of(BH3029.getCardId()), "player-b");
        game.playerPlayCard("player-b", "", "player-a", "skip");

        assertThrows(IllegalStateException.class, () ->
                game.playerUseSkillEffect("player-a", "武聖", "KILL", List.of(BH4030.getCardId()), "player-b"));
    }

    @DisplayName("武聖不能轉黑色牌")
    @Test
    public void wuShengRejectsBlackCard() {
        Game game = createGame(General.關羽, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008)); // 黑桃（已是殺但測 canConvert 拒非紅）

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseSkillEffect("player-a", "武聖", "KILL", List.of(BS8008.getCardId()), "player-b"));
    }

    @DisplayName("趙雲龍膽：被殺時把殺當閃打出 → 不扣血")
    @Test
    public void longDanKillAsDodgeResponse() {
        Game game = createGame(General.劉備, General.趙雲, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerUseSkillEffect("player-b", "龍膽", "DODGE", List.of(BS9009.getCardId()), null);

        assertEquals(4, b.getHP(), "龍膽當閃 → 不扣血");
        assertEquals(0, b.getHandSize());
        assertTrue(game.getGraveyard().contains(BS9009.getCardId()));
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("趙雲龍膽：閃當殺主動使用")
    @Test
    public void longDanDodgeAsActiveKill() {
        Game game = createGame(General.趙雲, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Dodge(BH2028));

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-a", "龍膽", "KILL", List.of(BH2028.getCardId()), "player-b");

        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent));
        game.playerPlayCard("player-b", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-b").getHP());
    }

    @DisplayName("趙雲龍膽：南蠻入侵需殺時把閃當殺打出")
    @Test
    public void longDanDodgeAsKillResponseToBarbarianInvasion() {
        Game game = createGame(General.劉備, General.趙雲, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new BarbarianInvasion(SS7007));
        b.getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        // b 是第一個 reactor，用龍膽把閃當殺
        game.playerUseSkillEffect("player-b", "龍膽", "KILL", List.of(BH2028.getCardId()), null);

        assertEquals(4, b.getHP(), "龍膽當殺回應南蠻 → 不扣血");
        assertEquals(0, b.getHandSize());
    }

    @DisplayName("甄姬傾國：被殺時黑色手牌當閃")
    @Test
    public void qingGuoBlackCardAsDodge() {
        Game game = createGame(General.劉備, General.甄姬, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Kill(BS9009)); // 黑桃手牌

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerUseSkillEffect("player-b", "傾國", "DODGE", List.of(BS9009.getCardId()), null);

        assertEquals(4, b.getHP());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("傾國擋普通殺後遊戲可繼續：activePlayer 回攻擊者、可結束回合")
    @Test
    public void qingGuoDodgeThenGameContinues() {
        Game game = createGame(General.劉備, General.甄姬, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerUseSkillEffect("player-b", "傾國", "DODGE", List.of(BS9009.getCardId()), null);

        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId(),
                "傾國後 activePlayer 應回攻擊者");
        assertTrue(game.getTopBehavior().isEmpty());

        // 遊戲可繼續：A 結束回合 → 輪到 B（甄姬）回合開始詢問洛神
        List<DomainEvent> events = game.finishAction("player-a");
        assertEquals("player-b", game.getCurrentRound().getCurrentRoundPlayer().getId(), "回合正常輪替");
    }

    @DisplayName("傾國回應萬箭齊發（非最後 reactor）→ 輪詢推進到下一家")
    @Test
    public void qingGuoAgainstArrowBarrageAdvancesPolling() {
        Game game = createGame(General.劉備, General.甄姬, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new ArrowBarrage(SHA040));
        b.getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", "active");
        game.playerUseSkillEffect("player-b", "傾國", "DODGE", List.of(BS9009.getCardId()), null);

        assertEquals(4, b.getHP(), "傾國當閃 → 不扣血");
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId(),
                "輪詢應推進到 C");

        game.playerPlayCard("player-c", "", "player-a", "skip");
        game.playerPlayCard("player-d", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertEquals(3, game.getPlayer("player-d").getHP());
        assertTrue(game.getTopBehavior().isEmpty(), "萬箭結算完畢");
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("傾國回應萬箭齊發（最後 reactor）→ 結算收尾、activePlayer 回出牌者")
    @Test
    public void qingGuoAsLastReactorInArrowBarrage() {
        Game game = createGame(General.劉備, General.孫權, General.孫權, General.甄姬);
        Player a = game.getPlayer("player-a");
        Player d = game.getPlayer("player-d");
        a.getHand().addCardToHand(new ArrowBarrage(SHA040));
        d.getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");
        game.playerPlayCard("player-c", "", "player-a", "skip");
        game.playerUseSkillEffect("player-d", "傾國", "DODGE", List.of(BS9009.getCardId()), null);

        assertEquals(4, d.getHP());
        assertTrue(game.getTopBehavior().isEmpty(), "最後 reactor 傾國 → 萬箭收尾");
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("傾國回應方天畫戟多目標殺 → 推進到下一個目標")
    @Test
    public void qingGuoAgainstHeavenlyDoubleHalberd() {
        Game game = createGame(General.劉備, General.甄姬, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getEquipment().setWeapon(new com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.HeavenlyDoubleHalberdCard(EDQ103));
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Kill(BS9009));

        game.playerUseHeavenlyDoubleHalberdKill("player-a", BS8008.getCardId(),
                List.of("player-b", "player-c"));
        game.playerUseSkillEffect("player-b", "傾國", "DODGE", List.of(BS9009.getCardId()), null);

        assertEquals(4, b.getHP(), "第一目標傾國擋下");
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId(),
                "推進到第二目標");

        game.playerPlayCard("player-c", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-c").getHP());
        assertTrue(game.getTopBehavior().isEmpty());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("攻擊者裝青龍偃月刀時傾國擋殺 → 觸發 GDCB 詢問，SKIP 後遊戲可繼續")
    @Test
    public void qingGuoDodgeTriggersGreenDragonAsk() {
        Game game = createGame(General.劉備, General.甄姬, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getEquipment().setWeapon(new com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.GreenDragonCrescentBladeCard(ES5005));
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-b", "傾國", "DODGE", List.of(BS9009.getCardId()), null);

        assertTrue(events.stream().anyMatch(e ->
                        e instanceof com.gaas.threeKingdoms.events.AskGreenDragonCrescentBladeEffectEvent),
                "閃被擋 → 詢問青龍偃月刀");

        game.playerUseGreenDragonCrescentBladeEffect("player-a",
                com.gaas.threeKingdoms.events.AskGreenDragonCrescentBladeEffectEvent.Choice.SKIP, null);
        assertEquals(4, b.getHP());
        assertTrue(game.getTopBehavior().isEmpty());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("issue #229：被問閃時直接以 playCard 打出黑牌 → 自動視為發動傾國")
    @Test
    public void qingGuoViaPlainPlayCardAutoConverts() {
        Game game = createGame(General.劉備, General.甄姬, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        // 前端行為：不走 useSkillEffect，直接把黑牌當一般出牌打出
        List<DomainEvent> events = game.playerPlayCard("player-b", BS9009.getCardId(), "player-a", "active");

        assertTrue(events.stream().anyMatch(e -> e instanceof com.gaas.threeKingdoms.events.SkillEffectEvent
                        && ((com.gaas.threeKingdoms.events.SkillEffectEvent) e).getSkillName().equals("傾國")
                        && ((com.gaas.threeKingdoms.events.SkillEffectEvent) e).isAccepted()),
                "自動轉化為傾國並廣播 SkillEffectEvent");
        assertEquals(4, b.getHP(), "視為出閃 → 不扣血");
        assertTrue(game.getGraveyard().contains(BS9009.getCardId()));
        assertTrue(game.getTopBehavior().isEmpty());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("issue #229：萬箭齊發被問閃時以 playCard 打出黑牌 → 自動傾國、輪詢推進")
    @Test
    public void qingGuoViaPlainPlayCardInArrowBarrage() {
        Game game = createGame(General.劉備, General.甄姬, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new ArrowBarrage(SHA040));
        b.getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", "active");
        game.playerPlayCard("player-b", BS9009.getCardId(), "player-a", "active");

        assertEquals(4, b.getHP());
        assertEquals("player-c", game.getCurrentRound().getActivePlayer().getId(), "輪詢推進到 C");
    }

    @DisplayName("issue #229：趙雲被問閃時以 playCard 打出殺 → 自動視為發動龍膽")
    @Test
    public void longDanViaPlainPlayCardAutoConverts() {
        Game game = createGame(General.劉備, General.趙雲, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        List<DomainEvent> events = game.playerPlayCard("player-b", BS9009.getCardId(), "player-a", "active");

        assertTrue(events.stream().anyMatch(e -> e instanceof com.gaas.threeKingdoms.events.SkillEffectEvent
                        && ((com.gaas.threeKingdoms.events.SkillEffectEvent) e).getSkillName().equals("龍膽")),
                "殺當閃 → 自動龍膽");
        assertEquals(4, b.getHP());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("issue #229：無轉化技的玩家被問閃時打出非閃牌 → 明確報錯而非默默吞掉")
    @Test
    public void nonConvertibleCardAsDodgeResponseThrows() {
        Game game = createGame(General.劉備, General.孫權, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertThrows(IllegalArgumentException.class, () ->
                game.playerPlayCard("player-b", BS9009.getCardId(), "player-a", "active"));
        assertFalse(game.getTopBehavior().isEmpty(), "報錯後仍在等 B 回應");
        assertEquals(4, b.getHP(), "未結算");
    }

    @DisplayName("傾國不能轉紅色牌")
    @Test
    public void qingGuoRejectsRedCard() {
        Game game = createGame(General.劉備, General.甄姬, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Peach(BH3029));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseSkillEffect("player-b", "傾國", "DODGE", List.of(BH3029.getCardId()), null));
    }

    @DisplayName("甘寧奇襲：黑色牌當過河拆橋 → 拆目標裝備")
    @Test
    public void qiXiBlackCardAsDismantle() {
        Game game = createGame(General.甘寧, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008)); // 黑桃
        b.getEquipment().setWeapon(new com.gaas.threeKingdoms.handcard.equipmentcard.weaponcard.QilinBowCard(EH5031));

        game.playerUseSkillEffect("player-a", "奇襲", "DISMANTLE", List.of(BS8008.getCardId()), "player-b");
        game.useDismantleEffect("player-a", "player-b", EH5031.getCardId(), null);

        assertFalse(b.getEquipment().hasAnyEquipment(), "裝備被拆");
        assertEquals(0, a.getHandSize());
        assertTrue(game.getGraveyard().contains(BS8008.getCardId()));
    }

    @DisplayName("大喬國色：方塊牌當樂不思蜀 → 進目標判定區")
    @Test
    public void guoSeDiamondCardAsContentment() {
        Game game = createGame(General.大喬, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Dodge(BD2080)); // 方塊

        game.playerUseSkillEffect("player-a", "國色", "CONTENTMENT", List.of(BD2080.getCardId()), "player-b");

        assertTrue(b.getDelayScrollCardIds().contains(BD2080.getCardId()), "方塊牌以樂不思蜀身份進判定區");
        assertEquals(0, a.getHandSize());
    }

    @DisplayName("國色不能對已有樂不思蜀的目標使用")
    @Test
    public void guoSeRejectsTargetWithContentment() {
        Game game = createGame(General.大喬, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(java.util.Arrays.asList(new Dodge(BD2080), new Dodge(BDK091)));

        game.playerUseSkillEffect("player-a", "國色", "CONTENTMENT", List.of(BD2080.getCardId()), "player-b");
        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseSkillEffect("player-a", "國色", "CONTENTMENT", List.of(BDK091.getCardId()), "player-b"));
    }

    // ===== 裝備區來源（標準版：武聖「紅色牌」、奇襲「黑色牌」、國色「方塊牌」含裝備區） =====

    @DisplayName("武聖：裝備區的紅色武器（麒麟弓）當殺 → 離開裝備區進棄牌堆、目標被問閃，skip 扣 1 血")
    @Test
    public void wuShengEquippedRedWeaponAsActiveKill() {
        Game game = createGame(General.關羽, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new QilinBowCard(EH5031)); // 紅心

        List<DomainEvent> events = game.playerUseSkillEffect(
                "player-a", "武聖", "KILL", List.of(EH5031.getCardId()), "player-b");

        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent
                && ((AskDodgeEvent) e).getPlayerId().equals("player-b")));
        assertNull(a.getEquipmentWeaponCard(), "麒麟弓離開裝備區");
        assertEquals(0, a.getHandSize(), "來源牌不會留在手牌");
        assertTrue(game.getGraveyard().contains(EH5031.getCardId()));

        game.playerPlayCard("player-b", "", "player-a", "skip");
        assertEquals(3, game.getPlayer("player-b").getHP());
        assertTrue(game.getTopBehavior().isEmpty());
    }

    @DisplayName("武聖：把武器本身當殺 → 攻擊距離以失去武器後計算；超出距離時拋例外且武器留在裝備區")
    @Test
    public void wuShengEquippedWeaponAsKillUsesRangeWithoutWeapon() {
        Game game = createGame(General.關羽, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new QilinBowCard(EH5031)); // 攻擊距離 5；拿掉後只剩 1

        // player-c 距離 2：有麒麟弓時在範圍內，但麒麟弓本身被轉化掉後就不在了
        assertThrows(IllegalStateException.class, () -> game.playerUseSkillEffect(
                "player-a", "武聖", "KILL", List.of(EH5031.getCardId()), "player-c"));

        assertNotNull(a.getEquipmentWeaponCard(), "驗證失敗要把武器裝回去");
        assertEquals(EH5031.getCardId(), a.getEquipmentWeaponCard().getId());
        assertFalse(game.getGraveyard().contains(EH5031.getCardId()));
        assertEquals(0, a.getHandSize());
        assertFalse(game.getCurrentRound().isShowKill(), "失敗不算出過殺");
    }

    @DisplayName("武聖：已出過殺時把諸葛連弩本身當殺 → 連弩已離開，不能再出殺；連弩留在裝備區")
    @Test
    public void wuShengEquippedCrossbowAsSecondKillRejected() {
        Game game = createGame(General.關羽, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getEquipment().setWeapon(new RepeatingCrossbowCard(EDA092)); // 方塊
        a.getHand().addCardToHand(new Peach(BH3029));

        game.playerUseSkillEffect("player-a", "武聖", "KILL", List.of(BH3029.getCardId()), "player-b");
        game.playerPlayCard("player-b", "", "player-a", "skip");

        assertThrows(IllegalStateException.class, () -> game.playerUseSkillEffect(
                "player-a", "武聖", "KILL", List.of(EDA092.getCardId()), "player-b"));
        assertEquals(EDA092.getCardId(), a.getEquipmentWeaponCard().getId());
    }

    @DisplayName("武聖：南蠻入侵需殺時把裝備區的紅色 -1 馬（赤兔）當殺打出 → 不扣血、馬進棄牌堆")
    @Test
    public void wuShengEquippedRedHorseAsKillResponseToBarbarianInvasion() {
        Game game = createGame(General.甘寧, General.關羽, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new BarbarianInvasion(SS7007));
        b.getEquipment().setMinusOne(new RedRabbitHorse(EH5044)); // 紅心

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");
        game.playerUseSkillEffect("player-b", "武聖", "KILL", List.of(EH5044.getCardId()), null);

        assertEquals(4, b.getHP(), "武聖當殺回應南蠻 → 不扣血");
        assertNull(b.getEquipmentMinusOneMountsCard(), "赤兔離開裝備區");
        assertTrue(game.getGraveyard().contains(EH5044.getCardId()));
    }

    @DisplayName("奇襲：裝備區的黑色武器（諸葛連弩）當過河拆橋 → 拆目標裝備、連弩進棄牌堆")
    @Test
    public void qiXiEquippedBlackWeaponAsDismantle() {
        Game game = createGame(General.甘寧, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getEquipment().setWeapon(new RepeatingCrossbowCard(ECA066)); // 梅花
        b.getEquipment().setWeapon(new QilinBowCard(EH5031));

        game.playerUseSkillEffect("player-a", "奇襲", "DISMANTLE", List.of(ECA066.getCardId()), "player-b");
        game.useDismantleEffect("player-a", "player-b", EH5031.getCardId(), null);

        assertFalse(b.getEquipment().hasAnyEquipment(), "目標裝備被拆");
        assertNull(a.getEquipmentWeaponCard(), "連弩離開裝備區");
        assertEquals(0, a.getHandSize());
        assertTrue(game.getGraveyard().contains(ECA066.getCardId()));
        assertTrue(game.getGraveyard().contains(EH5031.getCardId()));
    }

    @DisplayName("國色：裝備區的方塊 -1 馬（紫騂）當樂不思蜀 → 離開裝備區、進目標判定區（不進棄牌堆）")
    @Test
    public void guoSeEquippedDiamondHorseAsContentment() {
        Game game = createGame(General.大喬, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getEquipment().setMinusOne(new VioletStallion(EDK104)); // 方塊

        game.playerUseSkillEffect("player-a", "國色", "CONTENTMENT", List.of(EDK104.getCardId()), "player-b");

        assertNull(a.getEquipmentMinusOneMountsCard(), "紫騂離開裝備區");
        assertTrue(b.getDelayScrollCardIds().contains(EDK104.getCardId()), "以樂不思蜀身份進判定區");
        assertEquals(0, a.getHandSize());
        assertFalse(game.getGraveyard().contains(EDK104.getCardId()), "在判定區時不可同時在棄牌堆");
    }

    @DisplayName("傾國限黑色手牌：裝備區的黑色牌不能當閃（行為不變）")
    @Test
    public void qingGuoRejectsEquippedBlackCard() {
        Game game = createGame(General.甘寧, General.甄姬, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getEquipment().setArmor(new EightDiagramTactic(ES2015)); // 黑桃 八卦陣

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertThrows(IllegalArgumentException.class, () ->
                game.playerUseSkillEffect("player-b", "傾國", "DODGE", List.of(ES2015.getCardId()), null));
        assertEquals(ES2015.getCardId(), b.getEquipment().getArmor().getId(), "八卦陣仍在裝備區");
    }
}

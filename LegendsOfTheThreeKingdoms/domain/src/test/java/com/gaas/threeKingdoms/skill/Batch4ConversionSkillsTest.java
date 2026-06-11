package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.PlayCardEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
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
}

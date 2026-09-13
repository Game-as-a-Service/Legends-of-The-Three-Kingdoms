package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.behavior.behavior.BarbarianInvasionBehavior;
import com.gaas.threeKingdoms.events.AskDodgeEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.LightningTransferredEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.ArrowBarrage;
import com.gaas.threeKingdoms.handcard.scrollcard.BarbarianInvasion;
import com.gaas.threeKingdoms.handcard.scrollcard.Contentment;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Lightning;
import com.gaas.threeKingdoms.handcard.scrollcard.Snatch;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 陸遜 謙遜（標準版）：鎖定技，不能成為【順手牽羊】和【樂不思蜀】的目標。
 * 其餘牌（殺 / 過河拆橋 / 南蠻入侵 / 萬箭齊發 / 閃電）對陸遜照常結算。
 */
public class QianXunSkillTest extends PassiveSkillTestBase {

    @DisplayName("順手牽羊指定陸遜 → 拋例外（謙遜免疫）")
    @Test
    public void snatchTargetingLuXunThrows() {
        Game game = createGame(General.劉備, General.陸遜, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Snatch(SS3016));
        b.getHand().addCardToHand(new Peach(BH3029));

        assertThrows(IllegalStateException.class, () ->
                game.playerPlayCard("player-a", SS3016.getCardId(), "player-b", "active"));
        assertEquals(1, b.getHandSize(), "陸遜的手牌不該被拿走");
        assertEquals(1, a.getHandSize(), "順手牽羊不該被打出");
        assertTrue(game.getTopBehavior().isEmpty(), "不該產生 SnatchBehavior");
    }

    @DisplayName("順手牽羊指定非陸遜（距離 1 的 d）→ 正常結算，不受謙遜影響")
    @Test
    public void snatchTargetingOtherPlayerWorks() {
        Game game = createGame(General.劉備, General.陸遜, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player d = game.getPlayer("player-d");
        a.getHand().addCardToHand(new Snatch(SS3016));
        d.getHand().addCardToHand(new Peach(BH3029));

        assertDoesNotThrow(() ->
                game.playerPlayCard("player-a", SS3016.getCardId(), "player-d", "active"));
    }

    @DisplayName("樂不思蜀指定陸遜 → 拋例外（謙遜免疫）")
    @Test
    public void contentmentTargetingLuXunThrows() {
        Game game = createGame(General.劉備, General.陸遜, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Contentment(SS6006));

        assertThrows(IllegalStateException.class, () ->
                game.playerPlayCard("player-a", SS6006.getCardId(), "player-b", "active"));
        assertFalse(game.getPlayer("player-b").hasAnyContentmentCard());
    }

    @DisplayName("大喬國色（方塊牌當樂不思蜀）指定陸遜 → 一樣被謙遜擋下")
    @Test
    public void guoSeContentmentTargetingLuXunThrows() {
        Game game = createGame(General.大喬, General.陸遜, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new com.gaas.threeKingdoms.handcard.basiccard.Dodge(BD2080));

        assertThrows(IllegalStateException.class, () -> game.playerUseSkillEffect(
                "player-a", "國色", "CONTENTMENT", List.of(BD2080.getCardId()), "player-b"));
        assertFalse(game.getPlayer("player-b").hasAnyContentmentCard());
    }

    @DisplayName("過河拆橋指定陸遜 → 正常結算（謙遜只擋順手牽羊/樂不思蜀）")
    @Test
    public void dismantleTargetingLuXunWorks() {
        Game game = createGame(General.劉備, General.陸遜, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Dismantle(SS3003));
        b.getHand().addCardToHand(new Peach(BH3029));

        assertDoesNotThrow(() ->
                game.playerPlayCard("player-a", SS3003.getCardId(), "player-b", "active"));
    }

    @DisplayName("殺指定陸遜 → 正常詢問閃")
    @Test
    public void killTargetingLuXunAsksDodge() {
        Game game = createGame(General.劉備, General.陸遜, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        List<DomainEvent> events =
                game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertTrue(events.stream().anyMatch(e -> e instanceof AskDodgeEvent
                        && ((AskDodgeEvent) e).getPlayerId().equals("player-b")),
                "謙遜不擋殺，陸遜應被問閃");
    }

    @DisplayName("南蠻入侵 → 陸遜照標準版成為目標（在 reactionPlayers 中）")
    @Test
    public void luXunIsBarbarianInvasionTarget() {
        Game game = createGame(General.劉備, General.陸遜, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new BarbarianInvasion(SS7007));

        game.playerPlayCard("player-a", SS7007.getCardId(), "player-a", "active");

        BarbarianInvasionBehavior bi = (BarbarianInvasionBehavior) game.peekTopBehavior();
        assertEquals(List.of("player-b", "player-c", "player-d"), bi.getReactionPlayers(),
                "標準版謙遜不免疫南蠻入侵");
    }

    @DisplayName("萬箭齊發 → 陸遜照標準版成為目標（在 reactionPlayers 中）")
    @Test
    public void luXunIsArrowBarrageTarget() {
        Game game = createGame(General.劉備, General.陸遜, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new ArrowBarrage(SHA040));

        game.playerPlayCard("player-a", SHA040.getCardId(), "player-a", "active");

        assertEquals(List.of("player-b", "player-c", "player-d"),
                game.peekTopBehavior().getReactionPlayers(),
                "標準版謙遜不免疫萬箭齊發");
    }

    @DisplayName("閃電判定未中 → 照座位轉移給陸遜（標準版謙遜不擋閃電）")
    @Test
    public void lightningTransfersToLuXun() {
        Game game = createGame(General.劉備, General.陸遜, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Lightning lightning = new Lightning(SSA014);
        a.addDelayScrollCard(lightning);

        // 控制判定牌為非黑桃 2~9（紅心）→ 判定不中 → 轉移
        game.getDeck().add(List.of(new Peach(BH3029)));

        List<DomainEvent> events = game.handleLightningJudgement(lightning, a);

        LightningTransferredEvent transferred = events.stream()
                .filter(e -> e instanceof LightningTransferredEvent)
                .map(e -> (LightningTransferredEvent) e)
                .findFirst().orElseThrow();
        assertEquals("player-b", transferred.getTargetPlayerId(), "應轉移到下家陸遜");
        assertTrue(game.getPlayer("player-b").getDelayScrollCardIds().contains(lightning.getId()));
    }
}

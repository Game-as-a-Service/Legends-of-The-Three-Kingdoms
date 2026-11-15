package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.PlayType;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.basiccard.Peach;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 連營（陸遜，鎖定技）：當你失去最後一張手牌時，摸一張牌。
 * 「失去」不限於自己出牌／棄牌 — 被他人拆掉（過河拆橋）、拿走（反饋、突襲）也要觸發。
 * （順手牽羊因謙遜無法指定陸遜，不會發生。）
 */
public class LianYingLoseByOthersTest extends PassiveSkillTestBase {

    // ===== 過河拆橋 =====

    @DisplayName("過河拆橋拆掉陸遜最後一張手牌 → 連營摸一張")
    @Test
    public void dismantleLastHandCardTriggersLianYing() {
        Game game = createGame(General.甘寧, General.陸遜, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Dismantle(SS4004));
        b.getHand().addCardToHand(new Kill(BS8008));
        game.getDeck().putBackOnTop(List.of(new Peach(BH4030)));

        game.playerPlayCard("player-a", SS4004.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.useDismantleEffect("player-a", "player-b", "", 0);

        assertEquals(1, b.getHandSize(), "被拆光 → 連營摸 1");
        assertTrue(b.getHand().getCard(BH4030.getCardId()).isPresent(), "摸到牌堆頂的牌");
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("過河拆橋拆掉陸遜一張手牌但仍有手牌 → 連營不觸發")
    @Test
    public void dismantleNonLastHandCardDoesNotTrigger() {
        Game game = createGame(General.甘寧, General.陸遜, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Dismantle(SS4004));
        b.getHand().addCardToHand(List.of(new Kill(BS8008), new Dodge(BH2028)));

        game.playerPlayCard("player-a", SS4004.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.useDismantleEffect("player-a", "player-b", "", 0);

        assertEquals(1, b.getHandSize(), "剩 1 張，連營不觸發");
    }

    @DisplayName("過河拆橋拆掉非陸遜最後一張手牌 → 不摸牌")
    @Test
    public void dismantleNonLuXunDoesNotDraw() {
        Game game = createGame(General.甘寧, General.甘寧, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Dismantle(SS4004));
        b.getHand().addCardToHand(new Kill(BS8008));

        game.playerPlayCard("player-a", SS4004.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.useDismantleEffect("player-a", "player-b", "", 0);

        assertEquals(0, b.getHandSize());
    }

    // ===== 反饋 =====

    @DisplayName("陸遜殺司馬懿，反饋拿走陸遜最後一張手牌 → 連營摸一張")
    @Test
    public void fanKuiTakingLastHandCardTriggersLianYing() {
        Game game = createGame(General.陸遜, General.司馬懿, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(List.of(new Kill(BS8008), new Peach(BH3029)));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        game.getDeck().putBackOnTop(List.of(new Peach(BH4030)));
        game.playerUseSkillEffect("player-b", "反饋", "ACCEPT", null, null);

        assertTrue(b.getHand().getCard(BH3029.getCardId()).isPresent(), "司馬懿拿走陸遜的桃");
        assertEquals(1, a.getHandSize(), "被反饋拿光 → 連營摸 1");
        assertTrue(a.getHand().getCard(BH4030.getCardId()).isPresent());
        assertTrue(game.isTopBehaviorEmpty());
    }

    @DisplayName("反饋拿走陸遜一張手牌但仍有手牌 → 連營不觸發")
    @Test
    public void fanKuiTakingNonLastHandCardDoesNotTrigger() {
        Game game = createGame(General.陸遜, General.司馬懿, General.甘寧, General.甘寧);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(List.of(new Kill(BS8008), new Peach(BH3029), new Dodge(BH2028)));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", PlayType.ACTIVE.getPlayType());
        game.playerPlayCard("player-b", "", "player-a", PlayType.SKIP.getPlayType());
        game.playerUseSkillEffect("player-b", "反饋", "ACCEPT", null, null);

        assertEquals(1, a.getHandSize(), "剩 1 張，連營不觸發");
    }

    // ===== 突襲 =====

    @DisplayName("突襲拿走陸遜最後一張手牌 → 連營摸一張；另一目標（非陸遜）不摸")
    @Test
    public void tuXiTakingLastHandCardTriggersLianYing() {
        Game game = createGame(General.甘寧, General.張遼, General.陸遜, General.甘寧);
        Player c = game.getPlayer("player-c");
        Player d = game.getPlayer("player-d");
        c.getHand().addCardToHand(new Kill(BS8008));
        d.getHand().addCardToHand(new Dodge(BH2028));
        game.finishAction("player-a");
        game.getDeck().putBackOnTop(List.of(new Peach(BH4030)));

        List<DomainEvent> events = game.playerUseSkillEffect("player-b", "突襲", "ACCEPT",
                List.of("player-c", "player-d"), null);

        Player b = game.getPlayer("player-b");
        assertEquals(2, b.getHandSize(), "張遼只拿到兩張（不摸牌）");
        assertEquals(1, c.getHandSize(), "陸遜被拿光 → 連營摸 1");
        assertTrue(c.getHand().getCard(BH4030.getCardId()).isPresent());
        assertEquals(0, d.getHandSize(), "甘寧沒有連營");
        assertNotNull(events);
    }

    @DisplayName("突襲拿走陸遜一張手牌但仍有手牌 → 連營不觸發")
    @Test
    public void tuXiTakingNonLastHandCardDoesNotTrigger() {
        Game game = createGame(General.甘寧, General.張遼, General.陸遜, General.甘寧);
        Player c = game.getPlayer("player-c");
        c.getHand().addCardToHand(List.of(new Kill(BS8008), new Dodge(BH2028)));
        game.finishAction("player-a");

        game.playerUseSkillEffect("player-b", "突襲", "ACCEPT", List.of("player-c"), null);

        assertEquals(1, c.getHandSize(), "剩 1 張，連營不觸發");
    }
}

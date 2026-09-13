package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.events.AskSkillEffectEvent;
import com.gaas.threeKingdoms.events.DomainEvent;
import com.gaas.threeKingdoms.events.DrawCardEvent;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 許褚 裸衣（#167）— 主動觸發版：回合開始判定後、摸牌前詢問；
 * ACCEPT → 少摸一張 + 本回合殺/決鬥傷害 +1；SKIP → 正常摸牌無加成。
 */
public class LuoYiSkillTest extends PassiveSkillTestBase {

    private boolean hasLuoYiAsk(List<DomainEvent> events) {
        return events.stream().anyMatch(e -> e instanceof AskSkillEffectEvent
                && ((AskSkillEffectEvent) e).getSkillName().equals("裸衣"));
    }

    @DisplayName("許褚回合開始 → 判定後、摸牌前詢問裸衣（尚未摸牌）")
    @Test
    public void xuChuAskedBeforeDrawPhase() {
        Game game = createGame(General.許褚, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");

        List<DomainEvent> events = game.playerTakeTurnStartInJudgement(a);

        assertTrue(hasLuoYiAsk(events), "應詢問裸衣");
        assertFalse(events.stream().anyMatch(e -> e instanceof DrawCardEvent), "詢問中尚未摸牌");
        assertEquals(0, a.getHandSize());
        assertEquals("player-a", game.getCurrentRound().getActivePlayer().getId());
    }

    @DisplayName("裸衣 ACCEPT → 少摸一張（共 1）且本回合殺傷害 +1")
    @Test
    public void luoYiAcceptDrawsOneAndBoostsKill() {
        Game game = createGame(General.許褚, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");

        game.playerTakeTurnStartInJudgement(a);
        game.playerUseSkillEffect("player-a", "裸衣", "ACCEPT", null, null);

        assertEquals(1, a.getHandSize(), "裸衣：2 - 1 = 1");
        assertTrue(game.isTopBehaviorEmpty());

        a.getHand().addCardToHand(new Kill(BS8008));
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");

        assertEquals(2, game.getPlayer("player-b").getHP(), "裸衣發動：殺傷害 1+1=2");
    }

    @DisplayName("裸衣 ACCEPT → 本回合決鬥傷害 +1")
    @Test
    public void luoYiAcceptBoostsDuel() {
        Game game = createGame(General.許褚, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");

        game.playerTakeTurnStartInJudgement(a);
        game.playerUseSkillEffect("player-a", "裸衣", "ACCEPT", null, null);

        a.getHand().addCardToHand(new Duel(SSA001));
        // b 手中無殺 → 決鬥自動結算，b 受 a 的決鬥傷害
        game.playerPlayCard("player-a", SSA001.getCardId(), "player-b", "active");

        assertEquals(2, game.getPlayer("player-b").getHP(), "裸衣發動：決鬥傷害 1+1=2");
    }

    @DisplayName("裸衣 SKIP → 正常摸兩張，殺傷害不加成")
    @Test
    public void luoYiSkipDrawsTwoNoBoost() {
        Game game = createGame(General.許褚, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");

        game.playerTakeTurnStartInJudgement(a);
        game.playerUseSkillEffect("player-a", "裸衣", "SKIP", null, null);

        assertEquals(2, a.getHandSize(), "SKIP：正常摸 2");
        assertTrue(game.isTopBehaviorEmpty());

        a.getHand().addCardToHand(new Kill(BS8008));
        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");

        assertEquals(3, game.getPlayer("player-b").getHP(), "未發動：殺傷害 1");
    }

    @DisplayName("別人對許褚出殺（非許褚回合、未發動）→ 傷害不加成（1 點）")
    @Test
    public void killAgainstXuChuDealsNormalDamage() {
        Game game = createGame(General.劉備, General.許褚, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");

        assertEquals(3, game.getPlayer("player-b").getHP(), "裸衣只影響許褚自己發動的回合");
    }

    @DisplayName("非許褚回合開始 → 不詢問裸衣（迴歸保護）")
    @Test
    public void nonXuChuTurnStartNotAsked() {
        Game game = createGame(General.劉備, General.許褚, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");

        List<DomainEvent> events = game.playerTakeTurnStartInJudgement(a);

        assertFalse(hasLuoYiAsk(events));
        assertEquals(2, a.getHandSize(), "正常摸 2");
    }
}

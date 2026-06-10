package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.Dismantle;
import com.gaas.threeKingdoms.handcard.scrollcard.Snatch;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 黃月英 集智（#181）+ 奇才（#182）。
 */
public class HuangYueYingSkillTest extends PassiveSkillTestBase {

    @DisplayName("黃月英使用錦囊（過河拆橋）→ 集智摸一張")
    @Test
    public void huangYueYingDrawsOneAfterUsingScroll() {
        Game game = createGame(General.黃月英, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Dismantle(SS3003));
        b.getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", SS3003.getCardId(), "player-b", "active");

        // 拆牌互動尚未結束（需 useDismantleEffect），但集智摸牌在使用當下已發生
        assertEquals(1, a.getHandSize(), "出錦囊後 0 + 集智 1 = 1");
    }

    @DisplayName("黃月英出殺（非錦囊）→ 集智不觸發")
    @Test
    public void huangYueYingDoesNotDrawAfterBasicCard() {
        Game game = createGame(General.黃月英, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertEquals(0, a.getHandSize());
    }

    @DisplayName("非黃月英使用錦囊 → 不摸牌")
    @Test
    public void nonHuangYueYingDoesNotDrawAfterScroll() {
        Game game = createGame(General.劉備, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Dismantle(SS3003));
        b.getHand().addCardToHand(new Kill(BS9009));

        game.playerPlayCard("player-a", SS3003.getCardId(), "player-b", "active");

        assertEquals(0, a.getHandSize());
    }

    @DisplayName("黃月英對距離 2 的玩家出順手牽羊 → 奇才無距離限制")
    @Test
    public void huangYueYingSnatchesDistanceTwoTarget() {
        Game game = createGame(General.黃月英, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player c = game.getPlayer("player-c");
        a.getHand().addCardToHand(new Snatch(SS3016));
        c.getHand().addCardToHand(new Kill(BS9009));

        assertDoesNotThrow(() ->
                game.playerPlayCard("player-a", SS3016.getCardId(), "player-c", "active"));
    }
}

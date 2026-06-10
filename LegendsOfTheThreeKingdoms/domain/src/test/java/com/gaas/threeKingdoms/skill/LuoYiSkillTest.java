package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.handcard.scrollcard.Duel;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

public class LuoYiSkillTest extends PassiveSkillTestBase {

    @DisplayName("許褚摸牌階段 → 裸衣少摸一張（共 1 張）")
    @Test
    public void xuChuDrawsOneCard() {
        Game game = createGame(General.許褚, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");

        game.drawCardToPlayer(a, false);

        assertEquals(1, a.getHandSize(), "裸衣：2 - 1 = 1");
    }

    @DisplayName("許褚自己回合出殺命中 → 傷害 +1（共 2 點）")
    @Test
    public void xuChuKillDealsTwoDamageInOwnRound() {
        Game game = createGame(General.許褚, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");

        assertEquals(2, game.getPlayer("player-b").getHP(), "裸衣：殺傷害 1+1=2");
    }

    @DisplayName("許褚自己回合決鬥獲勝 → 傷害 +1（共 2 點）")
    @Test
    public void xuChuDuelDealsTwoDamageInOwnRound() {
        Game game = createGame(General.許褚, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Duel(SSA001));

        // b 手中無殺 → 決鬥自動結算，b 受 a 的決鬥傷害
        game.playerPlayCard("player-a", SSA001.getCardId(), "player-b", "active");

        assertEquals(2, game.getPlayer("player-b").getHP(), "裸衣：決鬥傷害 1+1=2");
    }

    @DisplayName("別人對許褚出殺（非許褚回合）→ 傷害不加成（1 點）")
    @Test
    public void killAgainstXuChuDealsNormalDamage() {
        Game game = createGame(General.劉備, General.許褚, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");

        assertEquals(3, game.getPlayer("player-b").getHP(), "裸衣只影響許褚自己造成的傷害");
    }
}

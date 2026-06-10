package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Dodge;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.player.Player;
import com.gaas.threeKingdoms.round.Round;
import com.gaas.threeKingdoms.round.RoundPhase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

public class LianYingSkillTest extends PassiveSkillTestBase {

    @DisplayName("陸遜出掉最後一張手牌（殺）→ 連營摸一張")
    @Test
    public void luXunDrawsAfterPlayingLastHandCard() {
        Game game = createGame(General.陸遜, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertEquals(1, a.getHandSize(), "失去最後手牌 → 連營摸 1");
    }

    @DisplayName("陸遜出牌後仍有手牌 → 連營不觸發")
    @Test
    public void luXunDoesNotDrawWhenHandNotEmpty() {
        Game game = createGame(General.陸遜, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(java.util.Arrays.asList(new Kill(BS8008), new Kill(BS9009)));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertEquals(1, a.getHandSize(), "剩 1 張，連營不觸發");
    }

    @DisplayName("陸遜 response 出閃用掉最後一張手牌 → 連營摸一張")
    @Test
    public void luXunDrawsAfterDodgingWithLastHandCard() {
        Game game = createGame(General.劉備, General.陸遜, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        Player b = game.getPlayer("player-b");
        a.getHand().addCardToHand(new Kill(BS8008));
        b.getHand().addCardToHand(new Dodge(BH2028));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", BH2028.getCardId(), "player-a", "active");

        assertEquals(1, b.getHandSize(), "出閃失去最後手牌 → 連營摸 1");
        assertEquals(4, b.getHP());
    }

    @DisplayName("陸遜棄牌階段棄光手牌 → 連營摸一張")
    @Test
    public void luXunDrawsAfterDiscardingAllHandCards() {
        Game game = createGame(General.陸遜, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getBloodCard().setHp(1);
        a.getHand().addCardToHand(java.util.Arrays.asList(new Kill(BS8008), new Kill(BS9009)));
        game.getCurrentRound().setRoundPhase(RoundPhase.Discard);

        // 棄光兩張（需棄 1，多棄允許）
        game.playerDiscardCard(List.of(BS8008.getCardId(), BS9009.getCardId()));

        assertEquals(1, a.getHandSize(), "棄光手牌 → 連營摸 1");
    }

    @DisplayName("非陸遜出掉最後一張手牌 → 不摸牌")
    @Test
    public void nonLuXunDoesNotDrawAfterPlayingLastHandCard() {
        Game game = createGame(General.劉備, General.劉備, General.孫權, General.孫權);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(new Kill(BS8008));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");

        assertEquals(0, a.getHandSize());
    }
}

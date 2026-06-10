package com.gaas.threeKingdoms.skill;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.generalcard.General;
import com.gaas.threeKingdoms.handcard.basiccard.Kill;
import com.gaas.threeKingdoms.player.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gaas.threeKingdoms.handcard.PlayCard.*;
import static org.junit.jupiter.api.Assertions.*;

public class PaoXiaoSkillTest extends PassiveSkillTestBase {

    @DisplayName("張飛同回合出第二張殺 → 咆哮無次數限制")
    @Test
    public void zhangFeiPlaysTwoKillsInOneTurn() {
        Game game = createGame(General.張飛, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(java.util.Arrays.asList(new Kill(BS8008), new Kill(BS9009)));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");

        assertDoesNotThrow(() ->
                game.playerPlayCard("player-a", BS9009.getCardId(), "player-b", "active"));
        game.playerPlayCard("player-b", "", "player-a", "skip");
        assertEquals(2, game.getPlayer("player-b").getHP());
    }

    @DisplayName("非張飛同回合出第二張殺 → 拋例外")
    @Test
    public void nonZhangFeiCannotPlaySecondKill() {
        Game game = createGame(General.劉備, General.劉備, General.劉備, General.劉備);
        Player a = game.getPlayer("player-a");
        a.getHand().addCardToHand(java.util.Arrays.asList(new Kill(BS8008), new Kill(BS9009)));

        game.playerPlayCard("player-a", BS8008.getCardId(), "player-b", "active");
        game.playerPlayCard("player-b", "", "player-a", "skip");

        assertThrows(IllegalStateException.class, () ->
                game.playerPlayCard("player-a", BS9009.getCardId(), "player-b", "active"));
    }
}

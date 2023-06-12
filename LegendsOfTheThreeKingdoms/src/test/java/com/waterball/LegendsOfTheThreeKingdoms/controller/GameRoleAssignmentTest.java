package com.waterball.LegendsOfTheThreeKingdoms.controller;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GameRoleAssignmentTest {

    @Test
    public void testAssignRoles() {
        Game game = new Game();
        List<Player> players = new ArrayList<>();
        players.add(new Player());
        players.add(new Player());
        players.add(new Player());
        players.add(new Player());
        players.add(new Player());
        game.setPlayers(players);

        game.assignRoles();

        for (Player player : game.getPlayers()) {
            assertNotNull(player.getRole());
        }
    }
}

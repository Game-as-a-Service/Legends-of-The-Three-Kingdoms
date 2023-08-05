package com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;

import java.util.List;

public class GameOver implements GamePhaseAction {
    @Override
    public void execute(Game game) {
        List<Player> players = game.getPlayers();

        List<Player> winners = players.stream().filter(player ->
                player.getRoleCard().getRole().equals(Role.REBEL)
        ).toList();
        game.setWinners(winners);
    }
}

package com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;
import com.waterball.LegendsOfTheThreeKingdoms.domain.rolecard.Role;

import java.util.List;

public class GameOver extends GamePhase {

    public GameOver(Game game) {
        super(game);
    }

    @Override
    public void execute() {
        List<Player> players = game.getPlayers();
        List<Player> winners = players.stream().filter(player ->
                player.getRoleCard().getRole().equals(Role.REBEL)
        ).toList();
        game.setWinners(winners);
    }
}

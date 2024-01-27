package com.gaas.threeKingdoms.gamephase;

import com.gaas.threeKingdoms.Game;
import com.gaas.threeKingdoms.rolecard.Role;
import com.gaas.threeKingdoms.player.Player;

import java.util.List;

public class GameOver extends GamePhase {

    public GameOver(Game game) {
        super(game);
        this.phaseName  = "GameOver";
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

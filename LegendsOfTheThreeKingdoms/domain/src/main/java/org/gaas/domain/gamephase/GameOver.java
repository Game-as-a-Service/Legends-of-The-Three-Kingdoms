package org.gaas.domain.gamephase;

import org.gaas.domain.Game;
import org.gaas.domain.player.Player;
import org.gaas.domain.rolecard.Role;

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

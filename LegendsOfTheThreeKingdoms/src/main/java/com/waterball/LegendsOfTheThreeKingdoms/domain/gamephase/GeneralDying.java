package com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;
import com.waterball.LegendsOfTheThreeKingdoms.domain.player.Player;

public class GeneralDying implements GamePhaseAction {

    private Player DyingPlayer;

    public void setPlayer(Player player) {
        this.DyingPlayer = player;
    }

    public Player getDyingPlayer() {
        return DyingPlayer;
    }

    @Override
    public void execute(Game game) {
        game.askActivePlayerPlayPeachCard();
    }
}

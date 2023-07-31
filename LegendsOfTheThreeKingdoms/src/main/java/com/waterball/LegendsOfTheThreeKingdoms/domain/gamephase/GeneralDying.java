package com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;

public class GeneralDying implements GamePhaseAction{
    @Override
    public void execute(Game game) {
        game.askActivePlayerPlayPeachCard();
    }
}

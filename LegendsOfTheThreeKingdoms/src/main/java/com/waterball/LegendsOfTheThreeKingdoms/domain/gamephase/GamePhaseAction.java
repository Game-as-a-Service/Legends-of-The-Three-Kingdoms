package com.waterball.LegendsOfTheThreeKingdoms.domain.gamephase;

import com.waterball.LegendsOfTheThreeKingdoms.domain.Game;

public interface GamePhaseAction {
    void execute(Game game);
}

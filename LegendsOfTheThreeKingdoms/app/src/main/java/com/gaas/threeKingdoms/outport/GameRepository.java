package com.gaas.threeKingdoms.outport;


import com.gaas.threeKingdoms.Game;

public interface GameRepository {

    Game save(Game game);

    Game findById(String gameId);
}

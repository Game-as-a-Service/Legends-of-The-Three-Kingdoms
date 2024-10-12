package com.gaas.threeKingdoms.outport;


import com.gaas.threeKingdoms.Game;

import java.util.Optional;

public interface GameRepository {

    Game save(Game game);

    Optional<Game> findById(String gameId);
}

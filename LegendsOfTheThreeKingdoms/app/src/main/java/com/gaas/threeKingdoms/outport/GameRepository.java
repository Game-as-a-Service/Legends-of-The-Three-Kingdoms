package com.gaas.threeKingdoms.outport;


import com.gaas.threeKingdoms.Game;
import org.springframework.stereotype.Repository;

public interface GameRepository {

    Game save(Game game);

    Game findById(String gameId);
}

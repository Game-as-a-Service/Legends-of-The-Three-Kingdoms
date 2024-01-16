package org.gaas.service;

import org.gaas.domain.Game;

public interface GameRepository {

    Game save(Game game);

    Game findGameById(String gameId);
}

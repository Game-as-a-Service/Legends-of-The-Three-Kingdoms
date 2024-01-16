package org.gaas.service.repository;

import org.gaas.domain.Game;

public interface GameRepository {

    Game save(Game game);

    Game findGameById(String gameId);
}
